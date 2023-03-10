package ink.repo.search.indexer.controller;

import ink.repo.search.indexer.service.PageRankUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/task/pageRank")
@RequiredArgsConstructor
public class PageRankUpdateController {
    @Autowired
    private PageRankUpdateService pageRankUpdateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void updateIDF() {
        pageRankUpdateService.updatePageRank();
    }
}
