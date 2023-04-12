package ink.repo.search.indexer.thread;

import ink.repo.search.common.model.TitleInvertedIndexEntry;
import ink.repo.search.common.model.WebPageInvertedIndexEntry;
import ink.repo.search.indexer.repository.TitleInvertedIndexEntryRepository;
import ink.repo.search.indexer.repository.WebPageInvertedIndexEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class IDFUpdateThread implements Runnable {
    @Autowired
    private WebPageInvertedIndexEntryRepository webPageInvertedIndexEntryRepository;
    @Autowired
    private TitleInvertedIndexEntryRepository titleInvertedIndexEntryRepository;

    @Override
    public void run() {
        System.out.println("Updating IDF...");
        // Timer
        long startTime = System.currentTimeMillis();

        // Update IDF for each entry with idf = log2(N/df)
        // For body
        List<WebPageInvertedIndexEntry> webPageInvertedIndexEntries = webPageInvertedIndexEntryRepository.findAll();
        final int webPageIndexSize = webPageInvertedIndexEntries.size();
        webPageInvertedIndexEntries.stream().parallel().forEach((entry) -> {
            entry.setIdf(Math.log((double) webPageIndexSize / entry.getWebPages().size()) / Math.log(2));
        });
        webPageInvertedIndexEntryRepository.saveAll(webPageInvertedIndexEntries);
        // For title
        List<TitleInvertedIndexEntry> titleInvertedIndexEntries = titleInvertedIndexEntryRepository.findAll();
        final int titleIndexSize = titleInvertedIndexEntries.size();
        titleInvertedIndexEntries.stream().parallel().forEach((entry) -> {
            entry.setIdf(Math.log((double) titleIndexSize / entry.getWebPages().size()) / Math.log(2));
        });
        titleInvertedIndexEntryRepository.saveAll(titleInvertedIndexEntries);

        System.out.println("IDF update completed in " + (System.currentTimeMillis() - startTime) + " ms");
    }
}
