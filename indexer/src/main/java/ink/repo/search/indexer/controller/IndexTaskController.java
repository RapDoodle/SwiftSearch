package ink.repo.search.indexer.controller;

import ink.repo.search.indexer.dto.IndexerTaskRequest;
import ink.repo.search.indexer.exception.AttributeAlreadyDefinedException;
import ink.repo.search.indexer.exception.NotFoundException;
import ink.repo.search.indexer.service.IndexTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task/index")
@RequiredArgsConstructor
public class IndexTaskController {
    @Autowired
    private IndexTaskService indexTaskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createIndex(@RequestBody IndexerTaskRequest crawlerTaskRequest) throws AttributeAlreadyDefinedException, NotFoundException {
        indexTaskService.createIndex(crawlerTaskRequest);
    }
}
