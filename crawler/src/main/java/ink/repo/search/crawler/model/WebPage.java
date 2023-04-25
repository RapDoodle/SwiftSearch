package ink.repo.search.crawler.model;

import ink.repo.search.common.dto.CrawledWebPageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Document(value = "webpages")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class WebPage {
    @Id
    private String id;
    @Indexed
    private String url;
    private String title;
    private Date createdDate;
    private Date lastFetchedDate;
    private Date lastModifiedDate;
    private String content;
    private List<String> links;
    private Map<String, String> headers;
    private Integer responseStatusCode;

    public CrawledWebPageResponse toResponse() {
        CrawledWebPageResponse response = new CrawledWebPageResponse();
        response.setId(this.id);
        response.setUrl(this.url);
        response.setTitle(this.title);
        response.setResponseStatusCode(this.responseStatusCode);
        response.setHeaders(this.headers);
        response.setContent(this.content);
        response.setLinks(this.links);
        response.setCreatedDate(this.createdDate);
        response.setLastFetchedDate(this.lastFetchedDate);
        response.setLastModifiedDate(this.lastModifiedDate);
        return response;
    }
}
