package ink.repo.search.indexer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
