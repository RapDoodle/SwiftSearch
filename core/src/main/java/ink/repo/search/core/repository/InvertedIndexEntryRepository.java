package ink.repo.search.core.repository;

import ink.repo.search.common.model.InvertedIndexEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Component
@Repository
public interface InvertedIndexEntryRepository extends MongoRepository<InvertedIndexEntry, String> {

    Optional<InvertedIndexEntry> findInvertedIndexEntriesByWord(String word);

    List<InvertedIndexEntry> findInvertedIndexEntriesByWordIn(List<String> words);

}
