package ink.repo.search.common.util;

import ink.repo.search.common.model.StemmedText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextPreprocessing {
    private static final String STOP_WORDS_PATH = "/assets/stopwords.txt";
    private static final Set<String> stopWords = new HashSet<>();
    public static final Pattern nonAlphaNumeric = Pattern.compile("[^a-zA-Z0-9]");

    static {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(TextPreprocessing.class.getResourceAsStream(STOP_WORDS_PATH)));
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

    public static StemmedText preprocessTextAndCount(String text) {
        Stemmer stemmer = new Stemmer();
        Map<String, Integer> wordFrequencies = new HashMap<>();
        StringBuilder stemmedTextSb = new StringBuilder();
        int lineCount = 0, stemmedWordCount = 0, maxTf = 0;
        for (final String line : text.lines().toList()) {
            ++lineCount;
            String[] words = line.split("[ /]");
            int lineWordCount = 0;
            for (String word : words) {
                word = word.toLowerCase();
                // Remove non-alphanumeric characters
                word = nonAlphaNumeric.matcher(word).replaceAll("");
                if (word.length() == 0)
                    continue;
                // Skip if word is in the list of stop words
                if (stopWords.contains(word))
                    continue;
                // Get word stem
                word = stemmer.stem(word);
                // Word count
                ++stemmedWordCount;
                int tf = wordFrequencies.getOrDefault(word, 0) + 1;
                maxTf = Math.max(maxTf, tf);
                wordFrequencies.put(word, tf);
                // Output
                stemmedTextSb.append(word);
                stemmedTextSb.append(" ");
                ++lineWordCount;
            }
            if (lineWordCount > 0)
                stemmedTextSb.deleteCharAt(stemmedTextSb.length() - 1);
            stemmedTextSb.append('\n');
        }
        // Should not be a new line for a document with an empty line
        if (lineCount == 1)
            stemmedTextSb.deleteCharAt(stemmedTextSb.length() - 1);

        StemmedText stemmedText = new StemmedText();
        stemmedText.setStemmedText(stemmedTextSb.toString());
        stemmedText.setWordFrequencies(wordFrequencies);
        stemmedText.setStemmedWordCount(stemmedWordCount);
        stemmedText.setMaxTf(maxTf);
        return stemmedText;
    }
}
