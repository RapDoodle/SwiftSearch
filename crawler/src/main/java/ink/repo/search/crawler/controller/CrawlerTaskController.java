package ink.repo.search.crawler.controller;

import ink.repo.search.crawler.dto.CrawlerTaskRequest;
import ink.repo.search.crawler.services.CrawlerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class CrawlerTaskController {
    @Autowired
    private CrawlerTaskService crawlerTaskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrawlerTaskRequest createCrawlerTask(@RequestBody CrawlerTaskRequest crawlerTaskRequest) {
        System.out.println(crawlerTaskRequest);
        crawlerTaskService.createCrawlerTask(crawlerTaskRequest);
        return crawlerTaskRequest;
    }
}
