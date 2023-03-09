package ink.repo.search.indexer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerTaskResponse {
    private String taskId;
    private String baseUrl;
    private int taskStatus;
    private LocalDateTime createdAt;
    private Integer visitedCount;
    private List<String> visitedUrls;
    private Map<String, List<String>> parentPointers;
}
