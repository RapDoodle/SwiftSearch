package ink.repo.search.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchResultEntry {
    private String url;
    private String title;
    private List<String> referencedBy;
    private List<String> referencesTo;
    private Date lastModifiedDate;
    private Double score;
    private Map<String, Integer> matchedWords;
}
