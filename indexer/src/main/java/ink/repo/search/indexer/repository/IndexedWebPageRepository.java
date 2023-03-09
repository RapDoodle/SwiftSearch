package ink.repo.search.indexer.repository;

import ink.repo.search.indexer.model.IndexedWebPage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IndexedWebPageRepository extends MongoRepository<IndexedWebPage, String> {

    Optional<IndexedWebPage> findIndexedWebPageByUrl(String url);

}
