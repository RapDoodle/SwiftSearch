package ink.repo.search.common.dto;

import ink.repo.search.common.model.EvaluateResultEntry;
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
public class EvaluateResponse {
    public String query;
    public List<EvaluateResultEntry> results;
    public Long duration;  // Unit: ms
    private int resultsCount;

    public String getFormattedDuration() {
        return String.format("%.4f s", (double) this.duration.intValue() / 1000);
    }
}
