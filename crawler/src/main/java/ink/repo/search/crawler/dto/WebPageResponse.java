package ink.repo.search.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

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
    private Date createdDate;
    private Date lastFetchedDate;
    private Date lastModifiedDate;
    private String content;
    private List<String> links;
    private Map<String, String> headers;
}
