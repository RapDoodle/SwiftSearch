package ink.repo.search.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

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
}
