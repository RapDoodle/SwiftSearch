package ink.repo.search.indexer.repository;

import ink.repo.search.indexer.model.IndexerTask;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndexTaskRepository extends MongoRepository<IndexerTask, String> {
}
