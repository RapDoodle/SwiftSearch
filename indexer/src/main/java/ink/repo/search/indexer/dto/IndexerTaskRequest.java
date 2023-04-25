package ink.repo.search.indexer.dto;

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
    private Boolean forceUpdate;
    private Boolean updateIDF;
    private Boolean updatePageRank;
}
