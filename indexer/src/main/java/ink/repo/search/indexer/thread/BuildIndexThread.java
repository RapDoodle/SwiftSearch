package ink.repo.search.indexer.thread;

import ink.repo.search.common.dto.CrawlerTaskResponse;
import ink.repo.search.common.dto.WebPageResponse;
import ink.repo.search.common.exception.AttributeAlreadyDefinedException;
import ink.repo.search.common.exception.NotFoundException;
import ink.repo.search.common.model.IndexedWebPage;
import ink.repo.search.common.model.InvertedIndexEntry;
import ink.repo.search.common.model.StemmedText;
import ink.repo.search.common.util.HTMLUtils;
import ink.repo.search.common.util.TextPreprocessing;
import ink.repo.search.indexer.model.IndexTask;
import ink.repo.search.indexer.repository.IndexTaskRepository;
import ink.repo.search.indexer.repository.IndexedWebPageRepository;
import ink.repo.search.indexer.repository.InvertedIndexEntryRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@Component
@Scope("prototype")
public class BuildIndexThread implements Runnable {
    @Autowired
    private IndexTaskRepository indexTaskRepository;
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private IndexedWebPageRepository indexedWebPageRepository;
    @Autowired
    private InvertedIndexEntryRepository invertedIndexEntryRepository;
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
        long before = System.currentTimeMillis();

        // Get the index task
        Optional<IndexTask> indexTaskOpt = indexTaskRepository.findById(this.taskId);
        if (indexTaskOpt.isEmpty())
            return;
        IndexTask indexTask = indexTaskOpt.get();
        final boolean forceUpdate = indexTask.getForceUpdate() == null ? false : indexTask.getForceUpdate();

        try {
            // Server address
            String crawlerServerUrlBase = indexTask.getCrawlerServerProtocol() + "://" + indexTask.getCrawlerServerAddr();

            // Get the crawler task
            final String crawlerTaskId = indexTask.getCrawlerTaskId();
            CrawlerTaskResponse crawlerTaskResponse = webClientBuilder.build().get()
                    .uri(crawlerServerUrlBase + "/api/v1/task",
                            uriBuilder -> uriBuilder.queryParam("taskId", crawlerTaskId).build())
                    .retrieve()
                    .bodyToMono(CrawlerTaskResponse.class)
                    .block();
            if (crawlerTaskResponse == null)
                return;

            // Create inverted index
            ConcurrentMap<String, InvertedIndexEntry> invertedIndexEntryMap = new ConcurrentHashMap<>();

            // Process the crawled content of each url visited by the crawler
            List<String> visitedUrls = crawlerTaskResponse.getVisitedUrls();
            visitedUrls.stream().parallel().forEach((url) -> {
                if (this.isTerminated)
                    return;

                // Fetch web pages from crawler
                WebPageResponse webPageResponse = webClientBuilder.build().get()
                        .uri(crawlerServerUrlBase + "/api/v1/webpage",
                                uriBuilder -> uriBuilder.queryParam("url", url).build())
                        .retrieve()
                        .bodyToMono(WebPageResponse.class)
                        .block();

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
                StemmedText stemmedTextObj = TextPreprocessing.preprocessTextAndCount(plainTextPage);
                String stemmedText = stemmedTextObj.getStemmedText();

                indexedWebPage.setTitle(webPageResponse.getTitle());
                indexedWebPage.setUrl(webPageResponse.getUrl());

                indexedWebPage.setLastFetchedDate(new Date());
                indexedWebPage.setLastModifiedDate(webPageResponse.getLastModifiedDate());
                indexedWebPage.setHtml(webPageResponse.getContent());
                indexedWebPage.setReferencesTo(webPageResponse.getLinks());
                indexedWebPage.setReferencedBy(crawlerTaskResponse.getParentPointers().getOrDefault(webPageResponse.getId(), new ArrayList<>()));
                indexedWebPage.setPlainText(plainTextPage);
                indexedWebPage.setStemmedText(stemmedText);
                indexedWebPage.setWordFrequencies(stemmedTextObj.getWordFrequencies());
                indexedWebPage.setStemmedWordCount(stemmedTextObj.getStemmedWordCount());

                indexedWebPageRepository.save(indexedWebPage);

                // Store in inverted index
                IndexedWebPage finalIndexedWebPage = indexedWebPage;
                for (String word : indexedWebPage.getWordFrequencies().keySet()) {
                    invertedIndexEntryMap.computeIfAbsent(word, (key) -> {
                        InvertedIndexEntry invertedIndexEntry = new InvertedIndexEntry();
                        invertedIndexEntry.setWord(word);
                        invertedIndexEntry.setWebPagesConcurrent(new ConcurrentLinkedQueue<>());
                        return invertedIndexEntry;
                    });
                    invertedIndexEntryMap.get(word).getWebPagesConcurrent().add(finalIndexedWebPage.getId());
                }
            });

            // Migrate all InvertedIndexEntry's webPagesConcurrent (ConcurrentLinkedQueue)
            // to webPages (ArrayList)
            invertedIndexEntryMap.keySet().stream().parallel().forEach((key) -> {
                InvertedIndexEntry indexEntry = invertedIndexEntryMap.get(key);
                indexEntry.setWebPages(new ArrayList<>(indexEntry.getWebPagesConcurrent().size()));
                indexEntry.getWebPages().addAll(indexEntry.getWebPagesConcurrent());
                indexEntry.setWebPagesConcurrent(null);
            });

            // Merge with the master index
            // Assuming the urls have never been processed before
            List<InvertedIndexEntry> items = new ArrayList<>();
            for (String word : invertedIndexEntryMap.keySet()) {
                Optional<InvertedIndexEntry> invertedIndexEntryOpt = invertedIndexEntryRepository.findInvertedIndexEntriesByWord(word);
                if (invertedIndexEntryOpt.isEmpty()) {
                    items.add(invertedIndexEntryMap.get(word));
                } else {
                    InvertedIndexEntry invertedIndexEntry = invertedIndexEntryOpt.get();
                    Set<String> webPageIdsSet = new HashSet<>();
                    for (String pageId : invertedIndexEntry.getWebPages())
                        webPageIdsSet.add(pageId);
                    for (String pageId : invertedIndexEntryMap.get(word).getWebPages())
                        if (!webPageIdsSet.contains(pageId))
                            invertedIndexEntry.getWebPages().add(pageId);
                    items.add(invertedIndexEntry);
                }
            }
            invertedIndexEntryRepository.saveAll(items);

            // Save task
            indexTask = getCurrentIndexTask();
            indexTask.setUrls(crawlerTaskResponse.getVisitedUrls());
            indexTask.setTaskStatus(IndexTask.TASK_STATUS_FINISHED);
            indexTask.setDuration(System.currentTimeMillis() - before);
            indexTaskRepository.save(indexTask);
        } catch (Exception e) {
            e.printStackTrace();
            indexTask.setTaskStatus(IndexTask.TASK_STATUS_ERROR);
            indexTaskRepository.save(indexTask);
        }
    }

    private void checkStopped() throws NotFoundException {
        IndexTask indexTask = getCurrentIndexTask();
        if (indexTask.getTaskStatus() == IndexTask.TASK_STATUS_STOPPED ||
                indexTask.getTaskStatus() == IndexTask.TASK_STATUS_FINISHED ||
                indexTask.getTaskStatus() == IndexTask.TASK_STATUS_ERROR)
            this.isTerminated = true;
    }

    private IndexTask getCurrentIndexTask() throws NotFoundException {
        Optional<IndexTask> optional = indexTaskRepository.findById(this.taskId);
        if (optional.isEmpty())
            throw new NotFoundException();
        return optional.get();
    }
}
