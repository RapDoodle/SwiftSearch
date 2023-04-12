package ink.repo.search.core.repository;

import ink.repo.search.common.model.InvertedIndexEntry;
import ink.repo.search.common.model.TitleInvertedIndexEntry;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Component
@Repository
public interface TitleInvertedIndexEntryRepository extends MongoRepository<TitleInvertedIndexEntry, String> {

    Optional<InvertedIndexEntry> findTitleInvertedIndexEntryByWord(String word);

    List<InvertedIndexEntry> findTitleInvertedIndexEntriesByWordIn(List<String> words);

}
