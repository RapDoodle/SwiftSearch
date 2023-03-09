package ink.repo.search.indexer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(value = "indexedWebpages")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IndexedWebPage {
    @Id
    private String id;
    @Indexed
    private String url;
    private String title;
    private Date createdDate;
    private Date lastFetchedDate;
    private Date lastModifiedDate;
    private String html;
    private String plainText;
    private String stemmedText;
    private Map<String, Integer> wordFrequencies;
    private List<String> referencedBy;
    private List<String> referencesTo;
}
