package ink.repo.search.query.service;

import ink.repo.search.common.dto.EvaluateResponse;
import ink.repo.search.common.dto.SearchResponse;
import ink.repo.search.common.model.*;
import ink.repo.search.common.util.Stemmer;
import ink.repo.search.common.util.TextPreprocessing;
import ink.repo.search.query.model.PageRankingResult;
import ink.repo.search.query.model.PageScoreTuple;
import ink.repo.search.query.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {
    @Value("${search.page-size}")
    private int PAGE_SIZE;
    @Value("${search.summary-window-size}")
    private int SUMMARY_WINDOW_SIZE;
    private static final Pattern pharseExtractPattern = Pattern.compile("[\"“”]([\\w\\s]+)[\"“”]");
    @Autowired
    private IndexedWebPageRepository indexedWebPageRepository;
    @Autowired
    private WebPageInvertedIndexEntryRepository webPageInvertedIndexEntryRepository;
    @Autowired
    private TitleInvertedIndexEntryRepository titleInvertedIndexEntryRepository;
    @Autowired
    private PhraseSearchWebPageRepository phraseSearchWebPageRepository;
    @Autowired
    private SearchWebPageRepository searchWebPageRepository;
    private static final Pattern nonAlphaNumeric = Pattern.compile("[^a-zA-Z0-9 /]");

    public SearchResponse search(final String query, final int page) {
        return this.search(query, page, PAGE_SIZE);
    }

    public SearchResponse search(final String query, final int page, final int pageSize) {
        PageRankingResult pageRankingResult = calculateRanking(query);
        List<PageScoreTuple> pageScoreTupleList = pageRankingResult.getScores();
        List<String> queryWords = pageRankingResult.getQueryWords();
        Set<String> queryWordsSet = new HashSet<>(queryWords);
        final int webPagesSize = pageScoreTupleList.size();

        final int offset = pageSize * (page - 1);

        // Response
        SearchResponse searchResponse = new SearchResponse();
        searchResponse.setQuery(query);
        searchResponse.setResults(new ArrayList<>());
        searchResponse.setResultsCount(webPagesSize);
        searchResponse.setPage(page);

        SearchResultEntry[] resultEntries = new SearchResultEntry[pageSize];
        IntStream.range(0, pageSize).parallel().forEach((i) -> {
            int order = offset + i;
            if (order > webPagesSize - 1)
                return;
            Optional<IndexedWebPage> indexedWebPageOpt = searchWebPageRepository.findById(
                    pageScoreTupleList.get(order).pageId);
            if (indexedWebPageOpt.isEmpty())
                return;
            IndexedWebPage indexedWebPage = indexedWebPageOpt.get();

            SearchResultEntry searchResultEntry = new SearchResultEntry();
            searchResultEntry.setUrl(indexedWebPage.getUrl());
            searchResultEntry.setTitle(indexedWebPage.getTitle());
            Stemmer stemmer = new Stemmer();
            String[] plainTextWordsArr =
                    nonAlphaNumeric.matcher(indexedWebPage.getPlainText())
                            .replaceAll("")
                            .split("[ /]");

            int n = plainTextWordsArr.length;
            int bestLo = 0, bestHi = Math.min(n, SUMMARY_WINDOW_SIZE), maxMatch = 0, currMatch = 0;
            // Check the initial window
            boolean[] isMatch = new boolean[bestHi];
            for (int pos = 0; pos < bestHi; ++pos) {
                if (queryWordsSet.contains(stemmer.stem(plainTextWordsArr[pos]))) {
                    ++currMatch;
                    isMatch[pos] = true;
                }
            }
            maxMatch = currMatch;
            for (int pos = bestHi; pos < n; ++pos) {
                int prevIsMatchIdx = pos % SUMMARY_WINDOW_SIZE;
                if (isMatch[prevIsMatchIdx]) {
                    --currMatch;
                    isMatch[prevIsMatchIdx] = false;
                }
                if (queryWordsSet.contains(stemmer.stem(plainTextWordsArr[pos].toLowerCase()))) {
                    ++currMatch;
                    isMatch[pos % SUMMARY_WINDOW_SIZE] = true;
                }
                if (currMatch > maxMatch) {
                    maxMatch = currMatch;
                    if (maxMatch == 1) {
                        bestLo = pos;
                        bestHi = pos + SUMMARY_WINDOW_SIZE;
                    } else {
                        bestLo = pos - SUMMARY_WINDOW_SIZE;
                        bestHi = pos;
                    }
                }
            }
            StringBuilder summaryHTMLSb = new StringBuilder();
            for (int pos = bestLo; pos <= Math.min(n - 1, bestHi); ++pos) {
                if (queryWordsSet.contains(stemmer.stem(plainTextWordsArr[pos].toLowerCase()))) {
                    summaryHTMLSb.append("<b>");
                    summaryHTMLSb.append(plainTextWordsArr[pos]);
                    summaryHTMLSb.append("</b> ");
                } else {
                    summaryHTMLSb.append(plainTextWordsArr[pos]);
                    summaryHTMLSb.append(" ");
                }
            }
            if (summaryHTMLSb.length() > 0) {
                summaryHTMLSb.deleteCharAt(summaryHTMLSb.length() - 1);
            }
            if (bestHi < n - 1) {
                summaryHTMLSb.append("...");
            }
            searchResultEntry.setSummaryHTML(summaryHTMLSb.toString());
            resultEntries[i] = searchResultEntry;
        });
        for (int i = 0; i < pageSize; ++i)
            if (resultEntries[i] != null)
                searchResponse.getResults().add(resultEntries[i]);

        return searchResponse;
    }

    public EvaluateResponse evaluate(final String query) {
        PageRankingResult pageRankingResult = calculateRanking(query);
        List<PageScoreTuple> pageScoreTupleList = pageRankingResult.getScores();
        List<String> queryWords = pageRankingResult.getQueryWords();
        final int webPagesSize = pageScoreTupleList.size();

        int type = IndexedWebPage.BODY;
        int offset = 0;

        // Response
        EvaluateResponse evaluateResponse = new EvaluateResponse();
        evaluateResponse.setQuery(query);
        evaluateResponse.setResults(new ArrayList<>());
        evaluateResponse.setResultsCount(webPagesSize);
        for (int i = 0; i < 50; ++i) {
            if (i > webPagesSize - 1)
                break;
            Optional<IndexedWebPage> indexedWebPageOpt = searchWebPageRepository.findById(
                    pageScoreTupleList.get(i).pageId);
            if (indexedWebPageOpt.isEmpty())
                continue;
            IndexedWebPage indexedWebPage = indexedWebPageOpt.get();

            EvaluateResultEntry evaluateResultEntry = new EvaluateResultEntry();
            evaluateResultEntry.setUrl(indexedWebPage.getUrl());
            evaluateResultEntry.setScore(pageScoreTupleList.get(i).score);
            evaluateResultEntry.setTitle(indexedWebPage.getTitle());
            evaluateResultEntry.setReferencesTo(indexedWebPage.getReferencesTo());
            evaluateResultEntry.setReferencedBy(indexedWebPage.getReferencedBy());
            evaluateResultEntry.setLastModifiedDate(indexedWebPage.getLastModifiedDate());
            evaluateResultEntry.setContentLength(indexedWebPage.getContentLength());

            Map<String, Integer> matchedWords = new HashMap<>();
            for (String word : queryWords) {
                if (!indexedWebPage.getWordFrequencies(type).containsKey(word))
                    continue;
                matchedWords.put(word, indexedWebPage.getWordFrequencies(type).get(word));
            }
            evaluateResultEntry.setMatchedWords(matchedWords.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new)));

            evaluateResponse.getResults().add(evaluateResultEntry);
        }

        return evaluateResponse;
    }

    private PageRankingResult calculateRanking(String query) {
        StemmedText stemmedQueryObj = TextPreprocessing.preprocessTextAndCount(query);
        Map<String, Integer> queryWordCount = stemmedQueryObj.getWordFrequencies();
        List<String> queryWords = queryWordCount.keySet().stream().toList();

        // Get the inverted index for each word in the query (for both body and title)
        List<InvertedIndexEntry> webPageIndexEntries =
                webPageInvertedIndexEntryRepository.findWebPageInvertedIndexEntriesByWordIn(queryWords);
        List<InvertedIndexEntry> titlePageIndexEntries =
                titleInvertedIndexEntryRepository.findTitleInvertedIndexEntriesByWordIn(queryWords);

        // Get all pages according to the ids in the inverted index
        Set<String> matchedWebPagesIds = new HashSet<>();
        for (InvertedIndexEntry entry : webPageIndexEntries)
            matchedWebPagesIds.addAll(entry.getWebPages());
        for (InvertedIndexEntry entry : titlePageIndexEntries)
            matchedWebPagesIds.addAll(entry.getWebPages());

        // Phrase search
        Matcher pharseMatcher = pharseExtractPattern.matcher(query);
        if (pharseMatcher.find()) {
            // When double quote pairs are used
            List<IndexedWebPage> webPagesWithContent =
                    phraseSearchWebPageRepository.findIndexedWebPagesByIdIn(matchedWebPagesIds.stream().toList());
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
                if (containsPhrases(currPage.getTitle() + "\n" + currPage.getPlainText(), phrases, true)) {
                    phrasesMatchedWebPagesIds.put(currPage.getId(), 0);
                }
            });
            // Replace the original set of page ids
            matchedWebPagesIds = phrasesMatchedWebPagesIds.keySet();
        }

        // Fetch the matching web pages
        List<IndexedWebPage> webPages = indexedWebPageRepository.findIndexedWebPagesByIdIn(
                matchedWebPagesIds.stream().toList());

        // Calculate cosine similarity
        // For web page
        double[] webPagesCosineSimScores = this.getCosineSimilarityScores(stemmedQueryObj, webPageIndexEntries, webPages, IndexedWebPage.BODY, true);
        // For title
        double[] titlesCosineSimScores = this.getCosineSimilarityScores(stemmedQueryObj, titlePageIndexEntries, webPages, IndexedWebPage.TITLE, true);

        // Get page ranks
        double[] pageRanks = this.getPageRanks(webPages);

        // Calculate the final score
        final int webPagesSize = webPages.size();
        double[] scores = new double[webPagesSize];
        double maxPageRank = scores.length > 0 ? pageRanks[0] : 1;
        for (int i = 1; i < webPagesSize; ++i) {
            maxPageRank = Math.max(maxPageRank, pageRanks[i]);
        }
        for (int i = 0; i < webPagesSize; ++i) {
            scores[i] = (webPagesCosineSimScores[i] + 2 * titlesCosineSimScores[i]) * (pageRanks[i] / maxPageRank);
        }

        List<PageScoreTuple> pageScoreTupleList = new ArrayList<>(webPagesSize);
        for (int i = 0; i < webPagesSize; ++i) {
            pageScoreTupleList.add(new PageScoreTuple(webPages.get(i).getId(), scores[i]));
        }
        // Sort by score in descending order
        pageScoreTupleList.sort((o1, o2) -> Double.compare(o2.score, o1.score));

        return PageRankingResult.builder().scores(pageScoreTupleList).queryWords(queryWords).build();
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

    private double[] getCosineSimilarityScores(StemmedText stemmedQuery, List<InvertedIndexEntry> indexEntries,
                                               List<IndexedWebPage> webPages, int type, boolean parallel) {
        Map<String, Integer> queryWordCount = stemmedQuery.getWordFrequencies();
        List<String> queryWords = queryWordCount.keySet().stream().toList();

        // Get the precalculated IDF scores
        Map<String, Double> wordIdfs = new HashMap<>();
        indexEntries.forEach((entry) -> {
            wordIdfs.put(entry.getWord(), entry.getIdf());
        });

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
        double[] scores = new double[webPagesSize];
        List<Integer> positions = new ArrayList<>(webPagesSize);
        for (int i = 0; i < webPagesSize; ++i)
            positions.add(i);

        // Calculate the cosine similarity between each document and the query
        Stream<Integer> positionStream = positions.stream();
        if (parallel)
            positionStream = positionStream.parallel();
        positionStream.forEach((i) -> {
            IndexedWebPage webPage = webPages.get(i);
            Map<String, Integer> wordFrequencies = webPage.getWordFrequencies(type);
            double dotProd = 0;
            double docNorm = 0;
            for (String word : queryWords) {
                if (!wordFrequencies.containsKey(word))
                    continue;
                double currTfidf = ((double) wordFrequencies.get(word) / webPage.getMaxTf(type)) * wordIdfs.get(word);
                dotProd += currTfidf * queryTfIdfScores.get(word);
                docNorm += (currTfidf * currTfidf);
            }
            docNorm = Math.sqrt(docNorm);
            scores[i] = dotProd / (docNorm * queryNorm);
            // In case there's no matching word
            if (Double.isNaN(scores[i]))
                scores[i] = 0;
        });

        return scores;
    }

    private double[] getPageRanks(List<IndexedWebPage> webPages) {
        int webPagesSize = webPages.size();
        double[] scores = new double[webPagesSize];
        List<Integer> positions = new ArrayList<>(webPagesSize);
        for (int i = 0; i < webPagesSize; ++i)
            positions.add(i);
        positions.stream().parallel().forEach((i) -> {
            IndexedWebPage webPage = webPages.get(i);
            // Page rank could be null if the page is newly created
            scores[i] = webPage.getPageRank() == null ? 0 : webPage.getPageRank();
        });
        return scores;
    }
}
