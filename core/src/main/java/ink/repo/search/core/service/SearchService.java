package ink.repo.search.core.service;

import ink.repo.search.common.model.IndexedWebPage;
import ink.repo.search.common.model.InvertedIndexEntry;
import ink.repo.search.common.model.StemmedText;
import ink.repo.search.common.util.TextPreprocessing;
import ink.repo.search.core.model.SearchResponse;
import ink.repo.search.core.model.SearchResultEntry;
import ink.repo.search.core.repository.IndexedWebPageRepository;
import ink.repo.search.core.repository.InvertedIndexEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    private static final int PAGE_SIZE = 10;
    @Autowired
    private IndexedWebPageRepository indexedWebPageRepository;
    @Autowired
    private InvertedIndexEntryRepository invertedIndexEntryRepository;

    private static class PageScoreTuple {
        PageScoreTuple(String pageId, double score) {
            this.pageId = pageId;
            this.score = score;
        }
        public String pageId;
        public double score;
    }

    public SearchResponse search(String query, int page) {
        StemmedText stemmedTextObj = TextPreprocessing.preprocessTextAndCount(query);
        Map<String, Integer> queryWordCount = stemmedTextObj.getWordFrequencies();
        List<String> queryWords = queryWordCount.keySet().stream().toList();

        // Get the inverted index for each word
        List<InvertedIndexEntry> indexEntries = invertedIndexEntryRepository.findInvertedIndexEntriesByWordIn(queryWords);

        // Precalculated IDF scores
        Map<String, Double> wordIdfs = new HashMap<>();
        indexEntries.forEach((entry) -> {
            wordIdfs.put(entry.getWord(), entry.getIdf());
        });

        // Calculate the term frequency for each word in the query
        int queryLength = queryWordCount.values().stream().filter(Objects::nonNull).mapToInt(freq -> freq).sum();
        Map<String, Double> queryTfIdfScores = new HashMap<>();
        double queryNorm = 0;
        for (String word : queryWordCount.keySet()) {
            double tfidf = ((double) queryWordCount.get(word) / queryLength) * wordIdfs.get(word);
            queryTfIdfScores.put(word, tfidf);
            queryNorm += (tfidf * tfidf);
        }
        queryNorm = Math.sqrt(queryNorm);
        final double queryNormFinal = queryNorm;

        // Get all pages according to the ids in the inverted index
        Set<String> indexEntriesIds = new HashSet<>();
        for (InvertedIndexEntry entry : indexEntries)
            indexEntriesIds.addAll(entry.getWebPages());
        List<IndexedWebPage> webPages = indexedWebPageRepository.findIndexedWebPagesByIdIn(indexEntriesIds.stream().toList());

        // Get the TF-IDF scores for all retrieved web pages
        int webPagesSize = webPages.size();
        double[] tfidfScores = new double[webPagesSize];
        List<Integer> positions = new ArrayList<>(webPagesSize);
        for (int i = 0; i < webPagesSize; ++i)
            positions.add(i);

        // Calculate the cosine similarity between each document and the query
        positions.stream().parallel().forEach((i) -> {
            IndexedWebPage webPage = webPages.get(i);
            Map<String, Integer> wordFrequencies = webPage.getWordFrequencies();
            // TODO: Handle when stemmedWordCount is null
            int stemmedWordCount = webPage.getStemmedWordCount();
            double dotProd = 0;
            double docNorm = 0;
            for (String word : queryWords) {
                if (!wordFrequencies.containsKey(word))
                    continue;
                double currTfidf = ((double) wordFrequencies.get(word) / webPage.getStemmedWordCount()) * wordIdfs.get(word);
                dotProd += currTfidf * queryTfIdfScores.get(word);
                docNorm += (currTfidf * currTfidf);
            }
            docNorm = Math.sqrt(docNorm);
            tfidfScores[i] = dotProd / (docNorm * queryNormFinal);
        });

        List<PageScoreTuple> pageScoreTupleList = new ArrayList<>(webPagesSize);
        for (int i = 0; i < webPagesSize; ++i) {
            pageScoreTupleList.add(new PageScoreTuple(webPages.get(i).getId(), tfidfScores[i]));
        }
        // Sort by score in descending order
        pageScoreTupleList.sort((o1, o2) -> Double.compare(o2.score, o1.score));

        int offset = PAGE_SIZE * (page - 1);

        // Response
        SearchResponse response = new SearchResponse();
        response.setQuery(query);
        response.setResults(new ArrayList<>());
        response.setResultsCount(webPagesSize);
        response.setPage(page);
        for (int i = 0; i < PAGE_SIZE; ++i) {
            int order = offset + i;
            if (order > webPagesSize - 1)
                break;
            Optional<IndexedWebPage> indexedWebPageOpt = indexedWebPageRepository.findById(pageScoreTupleList.get(order).pageId);
            if (indexedWebPageOpt.isEmpty())
                continue;
            IndexedWebPage indexedWebPage = indexedWebPageOpt.get();

            SearchResultEntry searchResultEntry = new SearchResultEntry();
            searchResultEntry.setUrl(indexedWebPage.getUrl());
            searchResultEntry.setScore(pageScoreTupleList.get(i).score);
            searchResultEntry.setTitle(indexedWebPage.getTitle());
            searchResultEntry.setReferencesTo(indexedWebPage.getReferencesTo());
            searchResultEntry.setReferencedBy(indexedWebPage.getReferencedBy());
            searchResultEntry.setLastModifiedDate(indexedWebPage.getLastModifiedDate());

            Map<String, Integer> matchedWords = new HashMap<>();
            for (String word : queryWords) {
                if (!indexedWebPage.getWordFrequencies().containsKey(word))
                    continue;
                matchedWords.put(word, indexedWebPage.getWordFrequencies().get(word));
            }
            searchResultEntry.setMatchedWords(matchedWords);

            response.getResults().add(searchResultEntry);
        }

        return response;
    }
}
