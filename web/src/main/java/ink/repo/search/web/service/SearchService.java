package ink.repo.search.web.service;

import ink.repo.search.common.dto.EvaluateResponse;
import ink.repo.search.common.dto.SearchResponse;
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

    public SearchResponse search(String query, int page) {
        final String serverUrlBase = queryServerProtocol + "://" + queryServerAddr + ":" + queryServerPort;
        SearchResponse queryServerResponse = webClientBuilder.build().get()
                .uri(serverUrlBase + "/api/v1/search",
                        uriBuilder -> uriBuilder.queryParam("query", query).queryParam("page", page).build())
                .retrieve()
                .bodyToMono(SearchResponse.class)
                .block();
        return queryServerResponse;
    }

    public EvaluateResponse evaluate(String query) {
        final String serverUrlBase = queryServerProtocol + "://" + queryServerAddr + ":" + queryServerPort;
        EvaluateResponse evaluateResponse = webClientBuilder.build().get()
                .uri(serverUrlBase + "/api/v1/evaluate",
                        uriBuilder -> uriBuilder.queryParam("query", query).build())
                .retrieve()
                .bodyToMono(EvaluateResponse.class)
                .block();
        return evaluateResponse;
    }

}
