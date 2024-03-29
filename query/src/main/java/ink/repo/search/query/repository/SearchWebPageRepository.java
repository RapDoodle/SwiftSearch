package ink.repo.search.query.repository;

import ink.repo.search.common.model.IndexedWebPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface SearchWebPageRepository extends MongoRepository<IndexedWebPage, String> {

    @Query(fields = "{ 'id': 1, 'url': 1, 'plainText': 1 }")
    List<IndexedWebPage> findIndexedWebPagesByIdIn(List<String> ids);

}
