package ink.repo.search.crawler.controller;

import ink.repo.search.common.dto.CrawlerTaskRequest;
import ink.repo.search.common.dto.CrawlerTaskResponse;
import ink.repo.search.crawler.exception.AlreadyStoppedException;
import ink.repo.search.crawler.exception.ArgumentNotFoundException;
import ink.repo.search.crawler.exception.AttributeAlreadyDefinedException;
import ink.repo.search.crawler.exception.NotFoundException;
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

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CrawlerTaskResponse getCrawlerTask(CrawlerTaskRequest crawlerTaskRequest) throws NotFoundException, ArgumentNotFoundException {
        return crawlerTaskService.getCrawlerTask(crawlerTaskRequest);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrawlerTaskResponse createCrawlerTask(@RequestBody CrawlerTaskRequest crawlerTaskRequest) throws AttributeAlreadyDefinedException {
        return crawlerTaskService.createCrawlerTask(crawlerTaskRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CrawlerTaskResponse deleteCrawlerTask(@RequestBody CrawlerTaskRequest crawlerTaskRequest) throws NotFoundException, AlreadyStoppedException {
        return crawlerTaskService.deleteCrawlerTask(crawlerTaskRequest);
    }
}
