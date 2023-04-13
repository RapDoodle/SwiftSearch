package ink.repo.search.core.controller;

import ink.repo.search.common.dto.QueryServerResponse;
import ink.repo.search.core.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping
    public QueryServerResponse search(String query, @Nullable Integer page) {
        long beginTime = System.currentTimeMillis();
        if (page == null)
            page = 1;
        QueryServerResponse response = searchService.search(query, page);
        response.setDuration(System.currentTimeMillis() - beginTime);
        return response;
    }
}
