package ink.repo.search.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexerTaskRequest {
    private String crawlerServerAddr;
    private String crawlerServerProtocol;
    private String crawlerTaskId;
}
