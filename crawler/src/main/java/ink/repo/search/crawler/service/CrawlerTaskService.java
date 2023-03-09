package ink.repo.search.crawler.service;

import ink.repo.search.crawler.dto.CrawlerTaskRequest;
import ink.repo.search.crawler.exception.AttributeAlreadyDefinedException;
import ink.repo.search.crawler.model.CrawlerTask;
import ink.repo.search.crawler.repository.CrawlerTaskRepository;
import ink.repo.search.crawler.threads.CrawlerThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CrawlerTaskService {

    private final CrawlerTaskRepository crawlerTaskRepository;
    @Autowired
    private TaskExecutor taskExecutor;
    @Autowired
    private ApplicationContext applicationContext;

    public void createCrawlerTask(CrawlerTaskRequest crawlerTaskRequest) throws AttributeAlreadyDefinedException {
        CrawlerTask crawlerTask = new CrawlerTask();
        crawlerTask.setBaseUrl(crawlerTaskRequest.getBaseUrl());
        crawlerTask.setAcl(crawlerTaskRequest.getAcl());
        crawlerTask.setMaxDepth(crawlerTaskRequest.getMaxDepth());
        crawlerTask.setMaxVisits(crawlerTaskRequest.getMaxVisits());
        crawlerTaskRepository.save(crawlerTask);

        CrawlerThread crawlerThread = applicationContext.getBean(CrawlerThread.class);
        crawlerThread.setTaskId(crawlerTask.getId());
        taskExecutor.execute(crawlerThread);
    }

}
