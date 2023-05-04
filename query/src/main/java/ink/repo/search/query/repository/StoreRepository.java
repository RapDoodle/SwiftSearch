package ink.repo.search.query.repository;

import ink.repo.search.common.model.StoreEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StoreRepository extends MongoRepository<StoreEntry, String> {
}
