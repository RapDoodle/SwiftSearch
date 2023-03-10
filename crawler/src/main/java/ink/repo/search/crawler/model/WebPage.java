package ink.repo.search.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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
}
