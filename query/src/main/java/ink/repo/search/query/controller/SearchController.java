package ink.repo.search.query.controller;

import ink.repo.search.common.dto.SearchResponse;
import ink.repo.search.query.service.SearchService;
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
    public SearchResponse search(String query, @Nullable Integer page) {
        long beginTime = System.currentTimeMillis();
        if (page == null)
            page = 1;
        SearchResponse response = searchService.search(query, page);
        response.setDuration(System.currentTimeMillis() - beginTime);
        return response;
    }
}
