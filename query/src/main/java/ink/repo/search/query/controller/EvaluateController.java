package ink.repo.search.query.controller;

import ink.repo.search.common.dto.EvaluateResponse;
import ink.repo.search.query.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/evaluate")
@RequiredArgsConstructor
public class EvaluateController {
    @Autowired
    private SearchService searchService;

    @GetMapping
    public EvaluateResponse evaluate(String query) {
        long beginTime = System.currentTimeMillis();
        EvaluateResponse response = searchService.evaluate(query);
        response.setDuration(System.currentTimeMillis() - beginTime);
        return response;
    }
}
