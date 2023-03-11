package ink.repo.search.indexer.controller;

import ink.repo.search.indexer.service.IDFUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/task/idf")
@RequiredArgsConstructor
public class IDFUpdateTaskController {
    @Autowired
    private IDFUpdateService idfUpdateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void updateIDF() {
        idfUpdateService.updateIDF();
    }
}
