package ink.repo.search.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SearchResponse {
    public String query;
    public List<SearchResultEntry> results;
    public Long duration;  // Unit: ms
    private int resultsCount;
    private int page;
}
