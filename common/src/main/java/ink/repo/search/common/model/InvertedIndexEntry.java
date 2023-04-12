package ink.repo.search.common.model;

import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InvertedIndexEntry {
    @Id
    private String word;
    private List<String> webPages;
    private Double idf;
    private ConcurrentLinkedQueue<String> webPagesConcurrent;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public List<String> getWebPages() {
        return webPages;
    }

    public void setWebPages(List<String> webPages) {
        this.webPages = webPages;
    }

    public Double getIdf() {
        return idf;
    }

    public void setIdf(Double idf) {
        this.idf = idf;
    }

    public ConcurrentLinkedQueue<String> getWebPagesConcurrent() {
        return webPagesConcurrent;
    }

    public void setWebPagesConcurrent(ConcurrentLinkedQueue<String> webPagesConcurrent) {
        this.webPagesConcurrent = webPagesConcurrent;
    }
}
