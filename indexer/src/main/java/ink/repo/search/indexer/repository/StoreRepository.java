package ink.repo.search.indexer.repository;

import ink.repo.search.common.model.StoreEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StoreRepository extends MongoRepository<StoreEntry, String> {

    Optional<StoreEntry> findStoreEntryByKey(String key);

}
