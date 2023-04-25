package ink.repo.search.common.dto;

import ink.repo.search.common.model.SearchResultEntry;
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

    public String getFormattedDuration() {
        return String.format("%.4f s", (double) this.duration.intValue() / 1000);
    }
}
