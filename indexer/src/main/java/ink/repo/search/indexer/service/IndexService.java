package ink.repo.search.indexer.service;

import ink.repo.search.common.exception.AttributeAlreadyDefinedException;
import ink.repo.search.indexer.dto.IndexerTaskRequest;
import ink.repo.search.indexer.model.IndexerTask;
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
public class IndexService {
    @Autowired
    private final IndexTaskRepository indexTaskRepository;
    @Autowired
    private final ApplicationContext applicationContext;
    @Autowired
    private final TaskExecutor taskExecutor;

    public void createIndex(IndexerTaskRequest indexTaskRequest) throws AttributeAlreadyDefinedException {
        // Create task
        IndexerTask indexerTask = new IndexerTask();
        indexerTask.setTaskStatus(IndexerTask.TASK_STATUS_CREATED);
        indexerTask.setCrawlerServerProtocol(indexTaskRequest.getCrawlerServerProtocol());
        indexerTask.setCrawlerServerAddr(indexTaskRequest.getCrawlerServerAddr());
        indexerTask.setCrawlerTaskId(indexTaskRequest.getCrawlerTaskId());
        indexerTask.setForceUpdate(indexTaskRequest.getForceUpdate());
        indexerTask.setUpdateIDF(indexTaskRequest.getUpdateIDF());
        indexerTask.setUpdatePageRank(indexTaskRequest.getUpdatePageRank());
        indexTaskRepository.save(indexerTask);

        BuildIndexThread buildIndexThread = applicationContext.getBean(BuildIndexThread.class);
        buildIndexThread.setTaskId(indexerTask.getId());
        taskExecutor.execute(buildIndexThread);
    }
}
