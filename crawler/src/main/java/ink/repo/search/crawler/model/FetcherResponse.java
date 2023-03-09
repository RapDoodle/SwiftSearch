package ink.repo.search.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.nodes.Document;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class FetcherResponse {
    private String url;
    private String title;
    private Document content;
    private Map<String, String> headers;
}
