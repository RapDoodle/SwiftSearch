package ink.repo.search.indexer.controller;

import ink.repo.search.indexer.dto.IndexerTaskRequest;
import ink.repo.search.indexer.service.IndexerTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class IndexerTaskController {
    @Autowired
    private IndexerTaskService indexerTaskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createIndex(@RequestBody IndexerTaskRequest crawlerTaskRequest) {
        indexerTaskService.createIndex(crawlerTaskRequest);
    }
}
