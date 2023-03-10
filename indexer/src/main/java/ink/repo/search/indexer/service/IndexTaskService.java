package ink.repo.search.indexer.service;

import ink.repo.search.indexer.dto.IndexerTaskRequest;
import ink.repo.search.indexer.exception.AttributeAlreadyDefinedException;
import ink.repo.search.indexer.exception.NotFoundException;
import ink.repo.search.indexer.model.IndexTask;
import ink.repo.search.indexer.repository.IndexTaskRepository;
import ink.repo.search.indexer.thread.BuildIndexThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexTaskService {
    @Autowired
    private final IndexTaskRepository indexTaskRepository;
    @Autowired
    private final ApplicationContext applicationContext;
    @Autowired
    private final TaskExecutor taskExecutor;

    public void createIndex(IndexerTaskRequest indexTaskRequest) throws NotFoundException, AttributeAlreadyDefinedException {
        // Create task
        IndexTask indexTask = new IndexTask();
        indexTask.setTaskStatus(IndexTask.TASK_STATUS_CREATED);
        indexTask.setCrawlerServerProtocol(indexTaskRequest.getCrawlerServerProtocol());
        indexTask.setCrawlerServerAddr(indexTaskRequest.getCrawlerServerAddr());
        indexTask.setCrawlerTaskId(indexTaskRequest.getCrawlerTaskId());
        indexTaskRepository.save(indexTask);

        BuildIndexThread buildIndexThread = applicationContext.getBean(BuildIndexThread.class);
        buildIndexThread.setTaskId(indexTask.getId());
        taskExecutor.execute(buildIndexThread);

        // Update all IDF
        // We have n,
    }
}
