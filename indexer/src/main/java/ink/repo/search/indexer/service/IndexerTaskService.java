package ink.repo.search.indexer.service;

import ink.repo.search.indexer.dto.CrawlerTaskResponse;
import ink.repo.search.indexer.dto.IndexerTaskRequest;
import ink.repo.search.indexer.dto.WebPageResponse;
import ink.repo.search.indexer.model.IndexedWebPage;
import ink.repo.search.indexer.model.InvertedIndexEntry;
import ink.repo.search.indexer.repository.InvertedIndexEntryRepository;
import ink.repo.search.indexer.utils.HTMLParser;
import ink.repo.search.indexer.repository.IndexedWebPageRepository;
import ink.repo.search.indexer.utils.Stemmer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexerTaskService {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private final WebClient.Builder webClientBuilder;
    @Autowired
    private final IndexedWebPageRepository indexedWebPageRepository;
    @Autowired
    private final InvertedIndexEntryRepository invertedIndexEntryRepository;

    public void createIndex(IndexerTaskRequest crawlerTaskRequest) {
        // TODO: Use discovery service
        // Get the crawler task
        CrawlerTaskResponse crawlerTaskResponse = webClientBuilder.build().get()
                .uri("http://127.0.0.1:8621/api/v1/task",
                        uriBuilder -> uriBuilder.queryParam("taskId", crawlerTaskRequest.getCrawlerTaskId()).build())
                .retrieve()
                .bodyToMono(CrawlerTaskResponse.class)
                .block();

        Set<String> stopwords = new HashSet<>();
        // Remove stop words
        try {
            BufferedReader reader = new BufferedReader(new FileReader(ResourceUtils.getFile("classpath:assets/stopwords.txt")));
            String word = reader.readLine();
            while (word != null) {
                stopwords.add(word.toLowerCase());
                word = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, InvertedIndexEntry> invertedIndexEntryMap = new HashMap<>();

        List<String> visitedUrls = crawlerTaskResponse.getVisitedUrls();
        for (String url : visitedUrls) {
            // Fetch web pages from crawler
            WebPageResponse webPageResponse = webClientBuilder.build().get()
                    .uri("http://127.0.0.1:8621/api/v1/webpage",
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
                if (indexedWebPage.getLastFetchedDate() != null &&
                        webPageResponse.getLastFetchedDate() != null &&
                        indexedWebPage.getLastFetchedDate().compareTo(webPageResponse.getLastFetchedDate()) >= 0) {
                    continue;
                }
            } else {
                indexedWebPage = new IndexedWebPage();
                indexedWebPage.setCreatedDate(new Date());
            }

            Stemmer stemmer = new Stemmer();
            Document parsedHTML = HTMLParser.parseHTML(webPageResponse.getContent(), webPageResponse.getUrl());
            Map<String, Integer> wordFrequencies = new HashMap<>();

            String plainTextPage = parsedHTML.body().text();
            StringBuilder stemmedTextSb = new StringBuilder();
            AtomicInteger lineCount = new AtomicInteger();
            plainTextPage.lines().forEach(line -> {
                lineCount.incrementAndGet();
                String[] words = line.split(" ");
                int lineWordCount = 0;
                for (String word : words) {
                    // Skip if word is in the list of stopwords
                    word = word.toLowerCase();
                    word = word.replaceAll("[^a-z0-9]", "");
                    if (word.length() == 0)
                        continue;
                    if (stopwords.contains(word))
                        continue;
                    // Get word stem
                    word = stemmer.stem(word);
                    // Word count
                    wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
                    // Output
                    stemmedTextSb.append(word);
                    stemmedTextSb.append(" ");
                    ++lineWordCount;
                }
                if (lineWordCount > 0)
                    stemmedTextSb.deleteCharAt(stemmedTextSb.length() - 1);
                stemmedTextSb.append('\n');
            });
            // Should not be a new line for a document with an empty line
            if (lineCount.get() == 1)
                stemmedTextSb.deleteCharAt(stemmedTextSb.length() - 1);

            indexedWebPage.setTitle(webPageResponse.getTitle());
            indexedWebPage.setUrl(webPageResponse.getUrl());

            indexedWebPage.setLastFetchedDate(new Date());
            indexedWebPage.setLastModifiedDate(webPageResponse.getLastModifiedDate());
            indexedWebPage.setHtml(webPageResponse.getContent());
            indexedWebPage.setReferencesTo(webPageResponse.getLinks());
            indexedWebPage.setReferencedBy(crawlerTaskResponse.getParentPointers().getOrDefault(webPageResponse.getId(), new LinkedList<>()));
            indexedWebPage.setPlainText(plainTextPage);
            indexedWebPage.setStemmedText(stemmedTextSb.toString());
            indexedWebPage.setWordFrequencies(wordFrequencies);

            indexedWebPageRepository.save(indexedWebPage);

            // Store in inverted index
            for (String word : indexedWebPage.getWordFrequencies().keySet()) {
                if (!invertedIndexEntryMap.containsKey(word)) {
                    InvertedIndexEntry invertedIndexEntry = new InvertedIndexEntry();
                    invertedIndexEntry.setWord(word);
                    invertedIndexEntry.setWebPages(new LinkedList<>());
                    invertedIndexEntryMap.put(word, invertedIndexEntry);
                }
                invertedIndexEntryMap.get(word).getWebPages().add(indexedWebPage.getId());
            }
        }

        // Merge with the master index
        // Assuming the urls have never been processed before
        List<InvertedIndexEntry> items = new LinkedList<>();
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
        invertedIndexEntryRepository.saveAll(items);
    }
}
