package ink.repo.search.indexer.service;

import ink.repo.search.indexer.thread.IDFUpdateThread;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IDFUpdateService {
    @Autowired
    private final ApplicationContext applicationContext;
    @Autowired
    private final TaskExecutor taskExecutor;

    public void updateIDF() {
        IDFUpdateThread idfUpdateThread = applicationContext.getBean(IDFUpdateThread.class);
        taskExecutor.execute(idfUpdateThread);
    }
}
