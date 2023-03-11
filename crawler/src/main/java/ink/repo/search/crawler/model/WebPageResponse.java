package ink.repo.search.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebPageResponse {
    private String id;
    private String url;
    private String title;
    private Integer responseStatusCode;
    private Date createdDate;
    private Date lastFetchedDate;
    private Date lastModifiedDate;
    private String content;
    private List<String> links;
    private Map<String, String> headers;
}
