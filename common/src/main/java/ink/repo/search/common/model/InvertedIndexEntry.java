package ink.repo.search.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Document(value = "invertedIndex")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class InvertedIndexEntry {
    @Id
    private String word;
    private List<String> webPages;
    private Double idf;
    private ConcurrentLinkedQueue<String> webPagesConcurrent;
}
