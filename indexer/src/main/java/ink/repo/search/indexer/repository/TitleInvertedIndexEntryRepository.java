package ink.repo.search.indexer.repository;

import ink.repo.search.common.model.TitleInvertedIndexEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Component
@Repository
public interface TitleInvertedIndexEntryRepository extends MongoRepository<TitleInvertedIndexEntry, String> {
    Optional<TitleInvertedIndexEntry> findInvertedIndexEntriesByWord(String word);
}
