package ink.repo.search.crawler.controller;

import ink.repo.search.crawler.dto.CrawlerTaskRequest;
import ink.repo.search.crawler.exception.AttributeAlreadyDefinedException;
import ink.repo.search.crawler.service.CrawlerTaskService;
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
    public CrawlerTaskRequest createCrawlerTask(@RequestBody CrawlerTaskRequest crawlerTaskRequest) throws AttributeAlreadyDefinedException {
        System.out.println(crawlerTaskRequest);
        crawlerTaskService.createCrawlerTask(crawlerTaskRequest);
        return crawlerTaskRequest;
    }
}
