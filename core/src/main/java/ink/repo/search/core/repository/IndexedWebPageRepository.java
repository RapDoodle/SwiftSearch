package ink.repo.search.core.repository;

import ink.repo.search.common.model.IndexedWebPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Component
@Repository
public interface IndexedWebPageRepository extends MongoRepository<IndexedWebPage, String> {

    Optional<IndexedWebPage> findIndexedWebPageByUrl(String url);

    @Query(fields = "{ 'id': 1, 'wordFrequencies': 1, 'stemmedWordCount': 1 }")
    List<IndexedWebPage> findIndexedWebPagesByIdIn(List<String> ids);

}
