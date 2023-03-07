package ink.repo.search.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.LinkedList;
import java.util.List;

@Document(value = "tasks")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CrawlerTask {
    public static final int TASK_CREATED = 0;
    public static final int TASK_RUNNING = 1;
    public static final int TASK_FINISHED = 2;
    public static final int TASK_STOPPED = 3;

    @Id
    private String id;
    private String baseUrl;
    private int taskStatus = TASK_CREATED;
    private List<String> acl;
//    @DBRef(lazy = true)
//    private List<WebPage> webPages;
    @CreatedDate
    private LocalDateTime createdAt;
    private Integer maxDepth;
    private Integer maxVisits;
    private List<String> visitedUrls;
}
