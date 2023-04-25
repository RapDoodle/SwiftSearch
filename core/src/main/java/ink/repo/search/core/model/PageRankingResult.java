package ink.repo.search.core.model;

import ink.repo.search.core.service.SearchService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRankingResult {
    private List<PageScoreTuple> scores;
    private List<String> queryWords;
}
