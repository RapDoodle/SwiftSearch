package ink.repo.search.core.service;

import ink.repo.search.common.model.IndexedWebPage;
import ink.repo.search.common.model.InvertedIndexEntry;
import ink.repo.search.common.model.StemmedText;
import ink.repo.search.common.util.TextPreprocessing;
import ink.repo.search.core.model.SearchResponse;
import ink.repo.search.core.model.SearchResultEntry;
import ink.repo.search.core.repository.IndexedWebPageRepository;
import ink.repo.search.core.repository.WebPageInvertedIndexEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    @Value("${search.page-size}")
    private int PAGE_SIZE;
    private static final Pattern pharseExtractPattern = Pattern.compile("[\"“”]([\\w\\s]+)[\"“”]");
    @Autowired
    private IndexedWebPageRepository indexedWebPageRepository;
    @Autowired
    private WebPageInvertedIndexEntryRepository webPageInvertedIndexEntryRepository;

    private static class PageScoreTuple {
        PageScoreTuple(String pageId, double score) {
            this.pageId = pageId;
            this.score = score;
        }
        public String pageId;
        public double score;
    }

    public SearchResponse search(final String query, final int page) {
        int type = IndexedWebPage.BODY;

        StemmedText stemmedTextObj = TextPreprocessing.preprocessTextAndCount(query);
        Map<String, Integer> queryWordCount = stemmedTextObj.getWordFrequencies();
        List<String> queryWords = queryWordCount.keySet().stream().toList();

        // Get the inverted index for each word in the query
        List<InvertedIndexEntry> indexEntries =
                webPageInvertedIndexEntryRepository.findWebPageInvertedIndexEntriesByWordIn(queryWords);

        // Get the precalculated IDF scores
        Map<String, Double> wordIdfs = new HashMap<>();
        indexEntries.forEach((entry) -> {
            wordIdfs.put(entry.getWord(), entry.getIdf());
        });

        // Get all pages according to the ids in the inverted index
        Set<String> matchedWebPagesIds = new HashSet<>();
        for (InvertedIndexEntry entry : indexEntries)
            matchedWebPagesIds.addAll(entry.getWebPages());

        // Phrase search
        Matcher pharseMatcher = pharseExtractPattern.matcher(query);
        if (pharseMatcher.find()) {
            // When double quote pairs are used
            List<IndexedWebPage> webPagesWithContent = indexedWebPageRepository.findIndexedWebPagesIgnoreCaseByIdIn(
                    matchedWebPagesIds.stream().toList());

            // Get all phrases
            pharseMatcher.reset();
            List<String> phrases = new ArrayList<>();
            while (pharseMatcher.find()) {
                phrases.add(pharseMatcher.group(1));
            }

            // Find all pages with matching phrases
            ConcurrentHashMap<String, Integer> phrasesMatchedWebPagesIds = new ConcurrentHashMap<>();
            webPagesWithContent.stream().parallel().forEach((currPage) -> {
                // The phrase should appear in the title or the text
                boolean matchBody = false, matchTitle = false;
                if (containsPhrases(currPage.getTitle() + "\n" + currPage.getPlainText(), phrases, true)) {
                    phrasesMatchedWebPagesIds.put(currPage.getId(), 0);
                }
            });
            matchedWebPagesIds = phrasesMatchedWebPagesIds.keySet();
        }

        // Fetch the matching web pages
        List<IndexedWebPage> webPages = indexedWebPageRepository.findIndexedWebPagesByIdIn(
                matchedWebPagesIds.stream().toList());

        // Calculate the term frequency for each word in the query
        int queryLength = queryWordCount.values().stream().filter(Objects::nonNull).mapToInt(freq -> freq).sum();
        Map<String, Double> queryTfIdfScores = new HashMap<>();
        double querySumSquared = 0;
        for (String word : queryWordCount.keySet()) {
            if (!wordIdfs.containsKey(word))
                continue;
            double tfidf = ((double) queryWordCount.get(word) / queryLength) * wordIdfs.get(word);
            queryTfIdfScores.put(word, tfidf);
            querySumSquared += (tfidf * tfidf);
        }
        final double queryNorm = Math.sqrt(querySumSquared);

        // Get the TF-IDF scores for all retrieved web pages
        int webPagesSize = webPages.size();
        double[] tfidfScores = new double[webPagesSize];
        List<Integer> positions = new ArrayList<>(webPagesSize);
        for (int i = 0; i < webPagesSize; ++i)
            positions.add(i);

        // Calculate the cosine similarity between each document and the query
        positions.stream().parallel().forEach((i) -> {
            IndexedWebPage webPage = webPages.get(i);
            Map<String, Integer> wordFrequencies = webPage.getWordFrequencies(type);
            // TODO: Handle when stemmedWordCount is null
            int stemmedWordCount = webPage.getStemmedWordCount(type);
            double dotProd = 0;
            double docNorm = 0;
            for (String word : queryWords) {
                if (!wordFrequencies.containsKey(word))
                    continue;
                double currTfidf = ((double) wordFrequencies.get(word) / stemmedWordCount) * wordIdfs.get(word);
                dotProd += currTfidf * queryTfIdfScores.get(word);
                docNorm += (currTfidf * currTfidf);
            }
            docNorm = Math.sqrt(docNorm);
            tfidfScores[i] = dotProd / (docNorm * queryNorm);
        });

        List<PageScoreTuple> pageScoreTupleList = new ArrayList<>(webPagesSize);
        for (int i = 0; i < webPagesSize; ++i) {
            pageScoreTupleList.add(new PageScoreTuple(webPages.get(i).getId(), tfidfScores[i]));
        }
        // Sort by score in descending order
        pageScoreTupleList.sort((o1, o2) -> Double.compare(o2.score, o1.score));

        int offset = PAGE_SIZE * (page - 1);

        // Response
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setQuery(query);
        searchResponse.setResults(new ArrayList<>());
        searchResponse.setResultsCount(webPagesSize);
        searchResponse.setPage(page);
        for (int i = 0; i < PAGE_SIZE; ++i) {
            int order = offset + i;
            if (order > webPagesSize - 1)
                break;
            Optional<IndexedWebPage> indexedWebPageOpt = indexedWebPageRepository.findById(
                    pageScoreTupleList.get(order).pageId);
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
                if (!indexedWebPage.getWordFrequencies(type).containsKey(word))
                    continue;
                matchedWords.put(word, indexedWebPage.getWordFrequencies(type).get(word));
            }
            searchResultEntry.setMatchedWords(matchedWords);

            searchResponse.getResults().add(searchResultEntry);
        }

        return searchResponse;
    }

    private static boolean containsPhrases(final String text, final List<String> phrases, boolean parallel) {
        Stream<String> textStream = text.lines();
        if (parallel)
            textStream = textStream.parallel();
        for (String phrase : phrases) {
            AtomicBoolean match = new AtomicBoolean(false);
            Pattern phrasePattern = Pattern.compile(Pattern.quote(phrase), Pattern.CASE_INSENSITIVE);
            textStream.forEach((line) -> {
                if (match.get())
                    return;
                if (!phrasePattern.matcher(line).find())
                    return;
                match.set(true);
            });
            if (!match.get())
                return false;
        }
        return true;
    }
}
