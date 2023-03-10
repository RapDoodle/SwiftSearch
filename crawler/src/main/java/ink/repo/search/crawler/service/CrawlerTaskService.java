package ink.repo.search.crawler.service;

import ink.repo.search.crawler.dto.CrawlerTaskRequest;
import ink.repo.search.crawler.dto.CrawlerTaskResponse;
import ink.repo.search.crawler.exception.AlreadyStoppedException;
import ink.repo.search.crawler.exception.ArgumentNotFoundException;
import ink.repo.search.crawler.exception.AttributeAlreadyDefinedException;
import ink.repo.search.crawler.exception.NotFoundException;
import ink.repo.search.crawler.model.CrawlerTask;
import ink.repo.search.crawler.repository.CrawlerTaskRepository;
import ink.repo.search.crawler.thread.CrawlerThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerTaskService {
    @Autowired
    private final CrawlerTaskRepository crawlerTaskRepository;
    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private ApplicationContext applicationContext;

    public CrawlerTaskResponse getCrawlerTask(CrawlerTaskRequest crawlerTaskRequest) throws NotFoundException, ArgumentNotFoundException {
        if (crawlerTaskRequest.getTaskId() == null)
            throw new ArgumentNotFoundException();
        Optional<CrawlerTask> crawlerTaskOptional = crawlerTaskRepository.findById(crawlerTaskRequest.getTaskId());
        if (crawlerTaskOptional.isEmpty())
            throw new NotFoundException();
        CrawlerTask crawlerTask = crawlerTaskOptional.get();

        // Response
        CrawlerTaskResponse crawlerTaskResponse = new CrawlerTaskResponse();
        crawlerTaskResponse.setTaskId(crawlerTask.getId());
        crawlerTaskResponse.setBaseUrl(crawlerTask.getBaseUrl());
        crawlerTaskResponse.setTaskStatus(crawlerTask.getTaskStatus());
        crawlerTaskResponse.setVisitedUrls(crawlerTask.getVisitedUrls());
        if (crawlerTask.getVisitedUrls() != null)
            crawlerTaskResponse.setVisitedCount(crawlerTask.getVisitedUrls().size());
        crawlerTaskResponse.setCreatedAt(crawlerTask.getCreatedAt());
        crawlerTaskResponse.setParentPointers(crawlerTask.getParentPointers());

        return crawlerTaskResponse;
    }

    public CrawlerTaskResponse createCrawlerTask(CrawlerTaskRequest crawlerTaskRequest) throws AttributeAlreadyDefinedException {
        CrawlerTask crawlerTask = new CrawlerTask();
        crawlerTask.setBaseUrl(crawlerTaskRequest.getBaseUrl());
        crawlerTask.setAcl(crawlerTaskRequest.getAcl());
        crawlerTask.setMaxDepth(crawlerTaskRequest.getMaxDepth());
        crawlerTask.setMaxVisits(crawlerTaskRequest.getMaxVisits());
        crawlerTaskRepository.save(crawlerTask);

        CrawlerThread crawlerThread = applicationContext.getBean(CrawlerThread.class);
        crawlerThread.setTaskId(crawlerTask.getId());
        taskExecutor.execute(crawlerThread);

        // Response
        CrawlerTaskResponse crawlerTaskResponse = new CrawlerTaskResponse();
        crawlerTaskResponse.setTaskId(crawlerTask.getId());
        crawlerTaskResponse.setBaseUrl(crawlerTask.getBaseUrl());
        crawlerTaskResponse.setTaskStatus(crawlerTask.getTaskStatus());
        crawlerTaskResponse.setCreatedAt(crawlerTask.getCreatedAt());

        return crawlerTaskResponse;
    }

    public CrawlerTaskResponse deleteCrawlerTask(CrawlerTaskRequest crawlerTaskRequest) throws NotFoundException, AlreadyStoppedException {
        // Get the task
        Optional<CrawlerTask> crawlerTaskOptional = crawlerTaskRepository.findById(crawlerTaskRequest.getTaskId());
        if (crawlerTaskOptional.isEmpty())
            throw new NotFoundException();
        CrawlerTask crawlerTask = crawlerTaskOptional.get();

        // Stop the task
        if (crawlerTask.getTaskStatus() == CrawlerTask.TASK_FINISHED ||
                crawlerTask.getTaskStatus() == CrawlerTask.TASK_STOPPED)
            throw new AlreadyStoppedException();
        crawlerTask.setTaskStatus(CrawlerTask.TASK_STOPPED);
        crawlerTaskRepository.save(crawlerTask);

        // Response
        CrawlerTaskResponse crawlerTaskResponse = new CrawlerTaskResponse();
        crawlerTaskResponse.setTaskId(crawlerTask.getId());
        crawlerTaskResponse.setTaskStatus(crawlerTask.getTaskStatus());

        return crawlerTaskResponse;
    }
}
