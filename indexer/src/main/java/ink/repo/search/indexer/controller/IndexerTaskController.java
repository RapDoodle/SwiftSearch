package ink.repo.search.indexer.controller;

import ink.repo.search.common.exception.AttributeAlreadyDefinedException;
import ink.repo.search.common.exception.NotFoundException;
import ink.repo.search.indexer.dto.IndexerTaskRequest;
import ink.repo.search.indexer.service.IndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task/index")
@RequiredArgsConstructor
public class IndexerTaskController {
    @Autowired
    private IndexService indexService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createIndex(@RequestBody IndexerTaskRequest crawlerTaskRequest) throws AttributeAlreadyDefinedException, NotFoundException {
        indexService.createIndex(crawlerTaskRequest);
    }
}
