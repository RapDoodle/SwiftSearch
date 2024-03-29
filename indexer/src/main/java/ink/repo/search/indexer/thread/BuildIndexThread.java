package ink.repo.search.indexer.thread;

import ink.repo.search.common.dto.CrawledWebPagesRequest;
import ink.repo.search.common.dto.CrawledWebPagesResponse;
import ink.repo.search.common.dto.CrawlerTaskResponse;
import ink.repo.search.common.exception.AttributeAlreadyDefinedException;
import ink.repo.search.common.exception.NotFoundException;
import ink.repo.search.common.model.*;
import ink.repo.search.common.util.HTMLUtils;
import ink.repo.search.common.util.TextPreprocessing;
import ink.repo.search.indexer.model.IndexerTask;
import ink.repo.search.indexer.repository.*;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@Component
@Scope("prototype")
public class BuildIndexThread implements Runnable {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private IndexTaskRepository indexTaskRepository;
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private IndexedWebPageRepository indexedWebPageRepository;
    @Autowired
    private WebPageInvertedIndexEntryRepository invertedIndexEntryRepository;
    @Autowired
    private TitleInvertedIndexEntryRepository titleInvertedIndexEntryRepository;
    private String taskId;
    private boolean isTerminated = false;

    public void setTaskId(String taskId) throws AttributeAlreadyDefinedException {
        if (this.taskId != null)
            throw new AttributeAlreadyDefinedException();
        this.taskId = taskId;
    }

    @Override
    public void run() {
        // Measure task duration
        System.out.println("Constructing search index...");
        long before = System.currentTimeMillis();

        // Get the index task
        Optional<IndexerTask> indexTaskOpt = indexTaskRepository.findById(this.taskId);
        if (indexTaskOpt.isEmpty())
            return;
        IndexerTask indexerTask = indexTaskOpt.get();
        final boolean forceUpdate = indexerTask.getForceUpdate() == null ? false : indexerTask.getForceUpdate();
        final boolean updateIDF = indexerTask.getUpdateIDF() == null ? true : indexerTask.getUpdateIDF();
        final boolean updatePageRank = indexerTask.getUpdatePageRank() == null ? false : indexerTask.getUpdatePageRank();

        try {
            // Server address
            String crawlerServerUrlBase = indexerTask.getCrawlerServerProtocol() + "://" + indexerTask.getCrawlerServerAddr();

            // Get the crawler task
            final String crawlerTaskId = indexerTask.getCrawlerTaskId();
            CrawlerTaskResponse crawlerTaskResponse = webClientBuilder.build().get()
                    .uri(crawlerServerUrlBase + "/api/v1/task",
                            uriBuilder -> uriBuilder.queryParam("taskId", crawlerTaskId).build())
                    .retrieve()
                    .bodyToMono(CrawlerTaskResponse.class)
                    .block();
            if (crawlerTaskResponse == null)
                return;

            // Create inverted index. Maps word to inverted index entries.
            ConcurrentMap<String, WebPageInvertedIndexEntry> webPageInvertedIndexEntryMap = new ConcurrentHashMap<>();
            ConcurrentMap<String, TitleInvertedIndexEntry> titleInvertedIndexEntryMap = new ConcurrentHashMap<>();

            CrawledWebPagesRequest crawledWebPagesRequest = new CrawledWebPagesRequest();
            crawledWebPagesRequest.setUrls(crawlerTaskResponse.getVisitedUrls());
            CrawledWebPagesResponse webPages = webClientBuilder.build().post()
                    .uri(crawlerServerUrlBase + "/api/v1/webpage")
                    .body(Mono.just(crawledWebPagesRequest), CrawledWebPagesRequest.class)
                    .retrieve()
                    .bodyToMono(CrawledWebPagesResponse.class)
                    .block();

            // Process the crawled content of each web page visited by the crawler
            webPages.getPages().stream().parallel().forEach((webPageResponse) -> {
                if (this.isTerminated)
                    return;

                String url = webPageResponse.getUrl();
                IndexedWebPage indexedWebPage = null;
                Optional<IndexedWebPage> indexedWebPageOpt = indexedWebPageRepository.findIndexedWebPageByUrl(url);
                if (indexedWebPageOpt.isPresent()) {
                    indexedWebPage = indexedWebPageOpt.get();
                    // Check if update is needed
                    // No actions needed when lastFetchedDate (of the indexer) >= lastFetchedDate (of the crawler)
                    if (!forceUpdate &&
                            indexedWebPage.getLastFetchedDate() != null &&
                            webPageResponse.getLastFetchedDate() != null &&
                            indexedWebPage.getLastFetchedDate().compareTo(webPageResponse.getLastFetchedDate()) >= 0) {
                        return;
                    }
                } else {
                    indexedWebPage = new IndexedWebPage();
                    indexedWebPage.setCreatedDate(new Date());
                }

                // Get the plain text HTML
                Document parsedHTML = HTMLUtils.parseHTML(webPageResponse.getContent(), webPageResponse.getUrl());
                String plainTextPage = parsedHTML.body().text();

                // Remove stop words and count word frequencies
                // For web content
                StemmedText stemmedBodyObj = TextPreprocessing.preprocessTextAndCount(plainTextPage);
                String stemmedBody = stemmedBodyObj.getStemmedText();
                // For web title
                StemmedText stemmedTitleObj = TextPreprocessing.preprocessTextAndCount(webPageResponse.getTitle());
                String stemmedTitle = stemmedTitleObj.getStemmedText();

                indexedWebPage.setTitle(webPageResponse.getTitle());
                indexedWebPage.setUrl(webPageResponse.getUrl());
                try {
                    indexedWebPage.setContentLength(Integer.parseInt(webPageResponse.getHeaders().getOrDefault("Content-Length", "0")));
                } catch (NumberFormatException e) {
                    indexedWebPage.setContentLength(webPageResponse.getContent().length());
                }

                indexedWebPage.setLastFetchedDate(new Date());
                indexedWebPage.setLastModifiedDate(webPageResponse.getLastModifiedDate());
                indexedWebPage.setHtml(webPageResponse.getContent());
                indexedWebPage.setReferencesTo(webPageResponse.getLinks());
                indexedWebPage.setReferencedBy(crawlerTaskResponse.getParentPointers().getOrDefault(webPageResponse.getId(), new ArrayList<>()));
                indexedWebPage.setPlainText(plainTextPage);
                // For web content
                indexedWebPage.setBodyWordFrequencies(stemmedBodyObj.getWordFrequencies());
                indexedWebPage.setBodyStemmedWordCount(stemmedBodyObj.getStemmedWordCount());
                indexedWebPage.setBodyMaxTf(stemmedBodyObj.getMaxTf());
                // For title
                indexedWebPage.setTitleWordFrequencies(stemmedTitleObj.getWordFrequencies());
                indexedWebPage.setTitleStemmedWordCount(stemmedTitleObj.getStemmedWordCount());
                indexedWebPage.setTitleMaxTf(stemmedTitleObj.getMaxTf());

                indexedWebPageRepository.save(indexedWebPage);

                // Store in inverted index
                // For web page content
                IndexedWebPage finalIndexedWebPage = indexedWebPage;
                for (String word : indexedWebPage.getBodyWordFrequencies().keySet()) {
                    webPageInvertedIndexEntryMap.computeIfAbsent(word, (key) -> {
                        WebPageInvertedIndexEntry invertedIndexEntry = new WebPageInvertedIndexEntry();
                        invertedIndexEntry.setWord(word);
                        invertedIndexEntry.setWebPagesConcurrent(new ConcurrentLinkedQueue<>());
                        return invertedIndexEntry;
                    });
                    webPageInvertedIndexEntryMap.get(word).getWebPagesConcurrent().add(finalIndexedWebPage.getId());
                }
                // For title
                for (String word : indexedWebPage.getTitleWordFrequencies().keySet()) {
                    titleInvertedIndexEntryMap.computeIfAbsent(word, (key) -> {
                        TitleInvertedIndexEntry invertedIndexEntry = new TitleInvertedIndexEntry();
                        invertedIndexEntry.setWord(word);
                        invertedIndexEntry.setWebPagesConcurrent(new ConcurrentLinkedQueue<>());
                        return invertedIndexEntry;
                    });
                    titleInvertedIndexEntryMap.get(word).getWebPagesConcurrent().add(finalIndexedWebPage.getId());
                }
            });

            // Migrate all InvertedIndexEntry's webPagesConcurrent (ConcurrentLinkedQueue)
            // to webPages (ArrayList)
            // For web pages
            webPageInvertedIndexEntryMap.keySet().stream().parallel().forEach((key) -> {
                WebPageInvertedIndexEntry indexEntry = webPageInvertedIndexEntryMap.get(key);
                indexEntry.setWebPages(new ArrayList<>(indexEntry.getWebPagesConcurrent().size()));
                indexEntry.getWebPages().addAll(indexEntry.getWebPagesConcurrent());
                indexEntry.setWebPagesConcurrent(null);
            });
            // For title
            titleInvertedIndexEntryMap.keySet().stream().parallel().forEach((key) -> {
                TitleInvertedIndexEntry indexEntry = titleInvertedIndexEntryMap.get(key);
                indexEntry.setWebPages(new ArrayList<>(indexEntry.getWebPagesConcurrent().size()));
                indexEntry.getWebPages().addAll(indexEntry.getWebPagesConcurrent());
                indexEntry.setWebPagesConcurrent(null);
            });

            // Merge with the master index
            // Assuming the urls have never been processed before
            // For web page contents
            List<WebPageInvertedIndexEntry> webPageItems = new ArrayList<>();
            for (String word : webPageInvertedIndexEntryMap.keySet()) {
                Optional<WebPageInvertedIndexEntry> invertedIndexEntryOpt =
                        invertedIndexEntryRepository.findInvertedIndexEntriesByWord(word);
                if (invertedIndexEntryOpt.isEmpty()) {
                    webPageItems.add(webPageInvertedIndexEntryMap.get(word));
                } else {
                    WebPageInvertedIndexEntry invertedIndexEntry = invertedIndexEntryOpt.get();
                    Set<String> webPageIdsSet = new HashSet<>();
                    for (String pageId : invertedIndexEntry.getWebPages())
                        webPageIdsSet.add(pageId);
                    for (String pageId : webPageInvertedIndexEntryMap.get(word).getWebPages())
                        if (!webPageIdsSet.contains(pageId))
                            invertedIndexEntry.getWebPages().add(pageId);
                    webPageItems.add(invertedIndexEntry);
                }
            }
            invertedIndexEntryRepository.saveAll(webPageItems);
            // For titles
            List<TitleInvertedIndexEntry> titleItems = new ArrayList<>();
            for (String word : titleInvertedIndexEntryMap.keySet()) {
                Optional<TitleInvertedIndexEntry> invertedIndexEntryOpt =
                        titleInvertedIndexEntryRepository.findInvertedIndexEntriesByWord(word);
                if (invertedIndexEntryOpt.isEmpty()) {
                    titleItems.add(titleInvertedIndexEntryMap.get(word));
                } else {
                    TitleInvertedIndexEntry invertedIndexEntry = invertedIndexEntryOpt.get();
                    Set<String> webPageIdsSet = new HashSet<>();
                    for (String pageId : invertedIndexEntry.getWebPages())
                        webPageIdsSet.add(pageId);
                    for (String pageId : titleInvertedIndexEntryMap.get(word).getWebPages())
                        if (!webPageIdsSet.contains(pageId))
                            invertedIndexEntry.getWebPages().add(pageId);
                    titleItems.add(invertedIndexEntry);
                }
            }
            titleInvertedIndexEntryRepository.saveAll(titleItems);

            // Save task
            indexerTask = getCurrentIndexTask();
            indexerTask.setUrls(crawlerTaskResponse.getVisitedUrls());
            indexerTask.setTaskStatus(IndexerTask.TASK_STATUS_FINISHED);
            indexerTask.setDuration(System.currentTimeMillis() - before);
            indexTaskRepository.save(indexerTask);

            // Update all inverted index's IDF
            if (updateIDF) {
                IDFUpdateThread idfUpdateThread = applicationContext.getBean(IDFUpdateThread.class);
                taskExecutor.execute(idfUpdateThread);
            }
            if (updatePageRank) {
                PageRankUpdateThread pageRankUpdateThread = applicationContext.getBean(PageRankUpdateThread.class);
                taskExecutor.execute(pageRankUpdateThread);
            }
        } catch (Exception e) {
            e.printStackTrace();
            indexerTask.setTaskStatus(IndexerTask.TASK_STATUS_ERROR);
            indexTaskRepository.save(indexerTask);
        }
        System.out.println("Index built in " + (System.currentTimeMillis() - before) + " ms");
    }

    private void checkStopped() throws NotFoundException {
        IndexerTask indexerTask = getCurrentIndexTask();
        if (indexerTask.getTaskStatus() == IndexerTask.TASK_STATUS_STOPPED ||
                indexerTask.getTaskStatus() == IndexerTask.TASK_STATUS_FINISHED ||
                indexerTask.getTaskStatus() == IndexerTask.TASK_STATUS_ERROR)
            this.isTerminated = true;
    }

    private IndexerTask getCurrentIndexTask() throws NotFoundException {
        Optional<IndexerTask> optional = indexTaskRepository.findById(this.taskId);
        if (optional.isEmpty())
            throw new NotFoundException();
        return optional.get();
    }
}
