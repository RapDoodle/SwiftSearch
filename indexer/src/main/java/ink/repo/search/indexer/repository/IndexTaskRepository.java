package ink.repo.search.indexer.repository;

import ink.repo.search.indexer.model.IndexTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexTaskRepository extends MongoRepository<IndexTask, String> {
}
