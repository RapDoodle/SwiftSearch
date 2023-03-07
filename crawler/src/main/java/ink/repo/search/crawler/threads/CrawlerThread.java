package ink.repo.search.crawler.threads;

import ink.repo.search.crawler.acl.ACL;
import ink.repo.search.crawler.fetcher.Fetcher;
import ink.repo.search.crawler.fetcher.SeleniumFetcher;
import ink.repo.search.crawler.model.CrawlerTask;
import ink.repo.search.crawler.model.WebPage;
import ink.repo.search.crawler.repository.CrawlerTaskRepository;
import ink.repo.search.crawler.repository.WebPageRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

@Component
@Scope("application")
public class CrawlerThread implements Runnable {
    private static final int CHECK_STOPPED_INTERVAL = 20;
    private static final int CHECKPOINT_INTERVAL = 100;
    @Autowired
    private CrawlerTaskRepository crawlerTaskRepository;
    @Autowired
    private WebPageRepository webPageRepository;
    private LinkedList<String> urls;
    private String taskId;


    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public void run() {
        // Set task to running
        CrawlerTask crawlerTask = getCurrentCrawlerTask();
        crawlerTask.setTaskStatus(CrawlerTask.TASK_RUNNING);
        crawlerTaskRepository.save(crawlerTask);

        // Default values for maxDepth and maxVisits
        int maxDepth = Integer.MAX_VALUE, maxVisits = Integer.MAX_VALUE;
        if (crawlerTask.getMaxDepth() != null)
            maxDepth = crawlerTask.getMaxDepth();
        if (crawlerTask.getMaxVisits() != null)
            maxVisits = crawlerTask.getMaxVisits();
        System.out.println("maxDepth = " + maxDepth);
        System.out.println("maxVisits = " + maxVisits);

        // Set up ACL
        ACL acl = new ACL();
        acl.addAllowRules(crawlerTask.getAcl());

        // Store the list of visited urls for this task
        this.urls = new LinkedList<>();

        // Visit websites with BFS
        HashSet<String> visitedUrls = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        String baseUrl = crawlerTask.getBaseUrl();
        queue.add(baseUrl);
        int level = 0, visitCount = 0;
        Fetcher fetcher = new SeleniumFetcher(5, true);
        while (!queue.isEmpty() && maxDepth-- > 0) {
            int levelSize = queue.size();
            while (levelSize-- > 0 && maxVisits-- > 0) {
                // Check whether the task is terminated
                crawlerTask = getCurrentCrawlerTask();
                if (visitCount % CHECK_STOPPED_INTERVAL == 5 && checkIsStopped()) {
                    flushUrls();
                    return;
                }

                // Visit the current url
                String currUrl = queue.poll();
                urls.add(currUrl);
                ++visitCount;
                System.out.println("[" + level + "][" + visitCount + "] Visiting " + currUrl);
                try {
                    Document html = fetcher.fetch(currUrl);
                    Elements links = html.getElementsByTag("a");
                    System.out.println("Found " + links.size() + " links.");
                    visitedUrls.add(currUrl);

                    // DB object
                    WebPage webPage = new WebPage();
                    webPage.setUrl(currUrl);
                    webPage.setContent(html.html());

                    // Links
                    LinkedList<String> currUrlLinks = new LinkedList<>();
                    for (Element link : links) {
                        String linkUrl;
                        try {
                            linkUrl = new URL(link.absUrl("href")).toURI().toString();
                        } catch (URISyntaxException e) {
                            continue;
                        }
                        currUrlLinks.add(linkUrl);
                        if (!acl.check(linkUrl) || visitedUrls.contains(linkUrl))
                            continue;
                        queue.add(linkUrl);
                    }

                    webPage.setLinks(currUrlLinks);
                    webPageRepository.save(webPage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Flush urls to database
                if (visitCount % CHECKPOINT_INTERVAL == 0)
                    flushUrls();
            }
            ++level;
        }
        crawlerTask = flushUrls();
        crawlerTask.setTaskStatus(CrawlerTask.TASK_FINISHED);
        crawlerTaskRepository.save(crawlerTask);

        fetcher.close();
    }

    private boolean checkIsStopped() {
        CrawlerTask crawlerTask = getCurrentCrawlerTask();
        return crawlerTask.getTaskStatus() == CrawlerTask.TASK_STOPPED;
    }

    private CrawlerTask flushUrls() {
        CrawlerTask crawlerTask = getCurrentCrawlerTask();
        if (this.urls != null && this.urls.size() > 0) {
            if (crawlerTask.getVisitedUrls() == null)
                crawlerTask.setVisitedUrls(new LinkedList<>());
            crawlerTask.getVisitedUrls().addAll(this.urls);
            crawlerTaskRepository.save(crawlerTask);
            this.urls.clear();
        }
        return crawlerTask;
    }

    private CrawlerTask getCurrentCrawlerTask() {
        Optional<CrawlerTask> optional = crawlerTaskRepository.findById(this.taskId);
        if (!optional.isPresent())
            throw new NotFoundException("Object " + this.taskId + " not found.");
        return optional.get();
    }
}
