package ink.repo.search.crawler.dto;

import lombok.*;

import java.util.LinkedList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrawlerTaskRequest {
    private String taskId;
    private String taskName;
    private String baseUrl;
    private Integer maxDepth;
    private Integer maxVisits;
    private LinkedList<String> acl;
}
