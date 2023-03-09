package ink.repo.search.indexer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "invertedIndex")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InvertedIndexEntry {
    @Id
    private String word;
    private List<String> webPages;
}
