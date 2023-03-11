package ink.repo.search.indexer.thread;

import ink.repo.search.indexer.model.IndexedWebPage;
import ink.repo.search.indexer.repository.IndexedWebPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
@Scope("prototype")
public class PageRankUpdateThread implements Runnable {
    @Autowired
    private IndexedWebPageRepository indexedWebPageRepository;
    private double[] pageRankA;
    private double[] pageRankB;
    private int[] linkCounts;
    private HashMap<String, Integer> url2IdMapping;
    private List<IndexedWebPage> pages;

    @Override
    public void run() {
        // Timer
        long startTime = System.currentTimeMillis();

        // Fetch all entries
        this.pages = indexedWebPageRepository.findAll();
        int n = this.pages.size();

        // Initialize arrays for page rank
        this.pageRankA = new double[n];
        this.pageRankB = new double[n];
        for (int i = 0; i < n; ++i) {
            this.pageRankA[i] = 1;
            this.pageRankB[i] = 1;
        }

        // Create a mapping from url to an id between 0 and n-1
        int i = 0;
        this.url2IdMapping = new HashMap<>();
        for (IndexedWebPage page : pages)
            this.url2IdMapping.put(page.getUrl(), i++);

        // TODO: Clean URL
        // Count the number of out-going links
        this.linkCounts = new int[n];
        pages.stream().parallel().forEach((page) -> {
            int cnt = 0;
            for (String link : page.getReferencesTo())
                if (this.url2IdMapping.containsKey(link))
                    ++cnt;
            this.linkCounts[this.url2IdMapping.get(page.getUrl())] = cnt;
        });

        int iter = 0;
        for (iter = 0; iter < 100; ++iter) {
            if (iter % 2 == 0) {
                iterate(this.pageRankA, this.pageRankB);
            } else {
                iterate(this.pageRankB, this.pageRankA);
            }
            if (isConverged(this.pageRankA, this.pageRankB))
                break;
        }

        double[] pageRanks;
        if (iter % 2 == 0)
            pageRanks = this.pageRankA;
        else
            pageRanks = this.pageRankB;
        for (i = 0; i < n; ++i) {
            this.pages.get(i).setPageRank(pageRanks[i]);
        }
        indexedWebPageRepository.saveAll(this.pages);

        System.out.println("iter = " + iter);
        System.out.println("Completed in " + (System.currentTimeMillis() - startTime) + " ms");
    }

    void iterate(double[] curr, double[] prev) {
        final int n = curr.length;
        assert n == prev.length;
        this.pages.stream().parallel().forEach((page) -> {
            int i = this.url2IdMapping.get(page.getUrl());
            double currPageRank = prev[i];
            for (String link : page.getReferencedBy()) {
                Integer j = url2IdMapping.get(link);
                if (j == null || this.linkCounts[j] == 0)
                    continue;
                currPageRank += (prev[j] / this.linkCounts[j]);
            }
            curr[i] = 0.15 + 0.85 * currPageRank;
        });

        // Normalize
        double sum = 0;
        for (double val : curr)
            sum += val;
        for (int i = 0; i < n; ++i)
            curr[i] = (curr[i] / sum) * n;
    }

    boolean isConverged(double[] arr1, double[] arr2) {
        for (int i = 0; i < arr1.length; ++i) {
            if (Math.abs(arr1[i] - arr2[i]) > 1e-5)
                return false;
        }
        return true;
    }
}
