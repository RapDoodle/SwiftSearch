package ink.repo.search.web.service;

import ink.repo.search.common.dto.CrawlerTaskResponse;
import ink.repo.search.common.dto.QueryServerResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SearchService {
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Value("${swift-search.query-server.protocol}")
    private String queryServerProtocol;
    @Value("${swift-search.query-server.addr}")
    private String queryServerAddr;
    @Value("${swift-search.query-server.port}")
    private String queryServerPort;

    public QueryServerResponse search(String query, int page) {
        final String serverUrlBase = queryServerProtocol + "://" + queryServerAddr + ":" + queryServerPort;
        System.out.println(serverUrlBase);
        QueryServerResponse queryServerResponse = webClientBuilder.build().get()
                .uri(serverUrlBase + "/api/v1/search",
                        uriBuilder -> uriBuilder.queryParam("query", query).queryParam("page", page).build())
                .retrieve()
                .bodyToMono(QueryServerResponse.class)
                .block();
        return queryServerResponse;
    }

}
