package ink.repo.search.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(value = "webpages")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class WebPage {
    @Id
    String url;
    String title;
    Date lastModifiedDate;
    String content;
    List<String> links;
}
