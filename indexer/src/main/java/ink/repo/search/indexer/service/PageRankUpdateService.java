package ink.repo.search.indexer.service;

import ink.repo.search.indexer.thread.IDFUpdateThread;
import ink.repo.search.indexer.thread.PageRankUpdateThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PageRankUpdateService {
    @Autowired
    private final ApplicationContext applicationContext;
    @Autowired
    private final TaskExecutor taskExecutor;

    public void updatePageRank() {
        PageRankUpdateThread updateThread = applicationContext.getBean(PageRankUpdateThread.class);
        taskExecutor.execute(updateThread);
    }
}
