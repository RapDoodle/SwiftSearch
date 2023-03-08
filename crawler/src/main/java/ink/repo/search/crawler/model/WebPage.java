package ink.repo.search.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
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
    String url;
    String title;
    Date createdDate;
    Date lastFetchedDate;
    Date lastModifiedDate;
    String content;
    List<String> links;
    Map<String, String> headers;
}
