package ink.repo.search.indexer.repository;

import ink.repo.search.common.model.IndexedWebPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Component
@Repository
public interface IndexedWebPageRepository extends MongoRepository<IndexedWebPage, String> {

    Optional<IndexedWebPage> findIndexedWebPageByUrl(String url);

}
