package ink.repo.search.indexer.repository;

import ink.repo.search.common.model.InvertedIndexEntry;
import ink.repo.search.common.model.WebPageInvertedIndexEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Component
@Repository
public interface WebPageInvertedIndexEntryRepository extends MongoRepository<WebPageInvertedIndexEntry, String> {

    Optional<WebPageInvertedIndexEntry> findInvertedIndexEntriesByWord(String word);

}
