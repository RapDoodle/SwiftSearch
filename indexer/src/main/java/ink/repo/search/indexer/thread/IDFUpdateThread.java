package ink.repo.search.indexer.thread;

import ink.repo.search.indexer.model.InvertedIndexEntry;
import ink.repo.search.indexer.repository.InvertedIndexEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class IDFUpdateThread implements Runnable {
    @Autowired
    private InvertedIndexEntryRepository invertedIndexEntryRepository;

    @Override
    public void run() {
        // Timer
        long startTime = System.currentTimeMillis();

        // Fetch all entries
        List<InvertedIndexEntry> entries = invertedIndexEntryRepository.findAll();
        int n = entries.size();

        // Update IDF for each entry with idf = log2(N/df)
        entries.stream().parallel().forEach((entry) -> {
            entry.setIdf(Math.log((double) n / entry.getWebPages().size()) / Math.log(2));
        });
        System.out.println("Completed in " + (System.currentTimeMillis() - startTime) + " ms");
        invertedIndexEntryRepository.saveAll(entries);
    }
}
