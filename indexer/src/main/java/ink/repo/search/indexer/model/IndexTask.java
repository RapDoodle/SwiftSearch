package ink.repo.search.indexer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "indexTasks")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class IndexTask {
    public static final int TASK_STATUS_CREATED = 0;
    public static final int TASK_STATUS_RUNNING = 1;
    public static final int TASK_STATUS_FINISHED = 2;
    public static final int TASK_STATUS_STOPPED = 3;
    public static final int TASK_STATUS_ERROR = 4;

    private String id;
    private String crawlerServerAddr;
    private String crawlerServerProtocol;
    private String crawlerTaskId;
    private Integer taskStatus;
    private List<String> urls;
    private int completedUrls;
    private int totalUrls;
    private long duration;
    private Boolean forceUpdate;
}
