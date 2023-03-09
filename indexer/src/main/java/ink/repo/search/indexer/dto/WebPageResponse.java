package ink.repo.search.indexer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebPageResponse {
    private String id;
    private String url;
    private String title;
    private Date createdDate;
    private Date lastFetchedDate;
    private Date lastModifiedDate;
    private String content;
    private List<String> links;
    private Map<String, String> headers;
    private Map<String, List<String>> parentPointers;

    public void setParentPointers(Map<String, Set<String>> parentPointers) {
        this.parentPointers = new HashMap<>();
        parentPointers.forEach((key, value) -> {
            this.parentPointers.put(key, value.stream().toList());
        });
    }
}
