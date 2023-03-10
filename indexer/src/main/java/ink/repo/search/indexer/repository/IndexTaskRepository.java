package ink.repo.search.indexer.repository;

import ink.repo.search.indexer.model.IndexTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IndexTaskRepository extends MongoRepository<IndexTask, String> {
}
