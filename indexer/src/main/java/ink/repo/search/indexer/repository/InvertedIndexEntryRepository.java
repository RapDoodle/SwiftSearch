package ink.repo.search.indexer.repository;

import ink.repo.search.indexer.model.InvertedIndexEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InvertedIndexEntryRepository extends MongoRepository<InvertedIndexEntry, String> {

    Optional<InvertedIndexEntry> findInvertedIndexEntriesByWord(String word);

}
