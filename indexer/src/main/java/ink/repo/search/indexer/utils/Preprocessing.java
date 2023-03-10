package ink.repo.search.indexer.utils;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class Preprocessing {
    private static final String STOP_WORDS_PATH = "assets/stopwords.txt";
    private static final Set<String> stopWords = new HashSet<>();

    static {
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(ResourceUtils.getFile("classpath:" + STOP_WORDS_PATH)));
            String word = reader.readLine();
            while (word != null) {
                stopWords.add(word.toLowerCase());
                word = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ImmutablePair<String, Map<String, Integer>> preprocessTextAndCount(String text) {
        Stemmer stemmer = new Stemmer();
        Map<String, Integer> wordFrequencies = new HashMap<>();
        StringBuilder stemmedTextSb = new StringBuilder();
        AtomicInteger lineCount = new AtomicInteger();
        text.lines().forEach(line -> {
            lineCount.incrementAndGet();
            String[] words = line.split(" ");
            int lineWordCount = 0;
            for (String word : words) {
                word = word.toLowerCase();
                // Remove non-alphanumeric characters
                word = word.replaceAll("[^a-z0-9]", "");
                if (word.length() == 0)
                    continue;
                // Skip if word is in the list of stop words
                if (stopWords.contains(word))
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
        return new ImmutablePair<>(stemmedTextSb.toString(), wordFrequencies);
    }
}
