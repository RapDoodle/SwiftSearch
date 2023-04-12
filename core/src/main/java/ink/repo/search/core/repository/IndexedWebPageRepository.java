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

    @Query(fields = "{ 'id': 1, 'bodyWordFrequencies': 1, 'bodyStemmedWordCount': 1, " +
            "'titleWordFrequencies': 1, 'titleStemmedWordCount': 1, 'pageRank': 1," +
            "'titleMaxTf': 1, 'bodyMaxTf': 1 }")
    List<IndexedWebPage> findIndexedWebPagesByIdIn(List<String> ids);

    @Query(fields = "{ 'id': 1, 'plainText': 1 }")
    List<IndexedWebPage> findIndexedWebPagesIgnoreCaseByIdIn(List<String> ids);

}
