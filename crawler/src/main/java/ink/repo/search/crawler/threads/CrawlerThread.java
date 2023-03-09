package ink.repo.search.crawler.threads;

import ink.repo.search.crawler.acl.ACL;
import ink.repo.search.crawler.exception.AttributeAlreadyDefinedException;
import ink.repo.search.crawler.fetcher.Fetcher;
import ink.repo.search.crawler.fetcher.SeleniumFetcher;
import ink.repo.search.crawler.model.CrawlerTask;
import ink.repo.search.crawler.model.FetcherResponse;
import ink.repo.search.crawler.model.WebPage;
import ink.repo.search.crawler.repository.CrawlerTaskRepository;
import ink.repo.search.crawler.repository.WebPageRepository;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Scope("prototype")
public class CrawlerThread implements Runnable {
    private static final int CHECK_STOPPED_INTERVAL = 20;
    private static final int CHECKPOINT_INTERVAL = 100;
    private String taskId;
    @Autowired
    private CrawlerTaskRepository crawlerTaskRepository;
    @Autowired
    private WebPageRepository webPageRepository;
    private LinkedList<String> urls;
    Map<String, Set<String>> urlToParentUrlsMapping = new HashMap<>();
    Map<String, Set<String>> objectIdToParentUrlsMapping = new HashMap<>();
    Map<String, String> urlToObjectIdMapping = new HashMap<>();

    public void setTaskId(String taskId) throws AttributeAlreadyDefinedException {
        if (this.taskId != null)
            throw new AttributeAlreadyDefinedException();
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

        // Set up the access control list
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
                if (visitCount % CHECK_STOPPED_INTERVAL == 0 && checkIsStopped()) {
                    flushTaskCache();
                    return;
                }

                // Visit the current url
                String currUrl = queue.poll();
                currUrl = currUrl.split("#")[0];
                if (visitedUrls.contains(currUrl))
                    continue;
                urls.add(currUrl);
                ++visitCount;
                System.out.println("[" + level + "][" + visitCount + "] Visiting " + currUrl);
                try {
                    FetcherResponse fetcherResponse = fetcher.fetch(currUrl);
                    Map<String, String> headers = fetcherResponse.getHeaders();
                    Document html = fetcherResponse.getContent();

                    // DB object
                    Optional<WebPage> dbFetchRes = webPageRepository.findByUrl(currUrl);
                    WebPage webPage = null;
                    webPage = dbFetchRes.orElseGet(WebPage::new);

                    // Skip if the web page has not been modified
                    boolean fetch = true;
                    Date lastModifiedDate = null;
                    if (headers.getOrDefault("Last-Modified", null) != null) {
                        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                        lastModifiedDate = formatter.parse(headers.get("Last-Modified"));
                        if (webPage.getLastFetchedDate() != null && lastModifiedDate.compareTo(webPage.getLastFetchedDate()) < 0) {
                            System.out.println("Already fetched " + currUrl);
                            fetch = false;
                        }
                    }

                    Elements links = html.getElementsByTag("a");
                    System.out.println("Found " + links.size() + " links.");
                    visitedUrls.add(currUrl);

                    // The list of links the currUrl references to
                    LinkedList<String> currUrlLinks = new LinkedList<>();
                    for (Element link : links) {
                        String linkUrl;
                        try {
                            linkUrl = new URL(link.absUrl("href")).toURI().toString();
                        } catch (URISyntaxException | MalformedURLException e) {
                            continue;
                        }
                        currUrlLinks.add(linkUrl);

                        // Parent references
                        String linkUrlObjectId = urlToObjectIdMapping.getOrDefault(linkUrl, null);
                        if (linkUrlObjectId == null) {
                            if (this.urlToParentUrlsMapping.getOrDefault(linkUrl, null) == null)
                                this.urlToParentUrlsMapping.put(linkUrl, new HashSet<>());
                            this.urlToParentUrlsMapping.get(linkUrl).add(currUrl);
                        } else {
                            this.objectIdToParentUrlsMapping.get(linkUrlObjectId).add(currUrl);
                        }

                        // Access policy
                        if (acl.check(linkUrl) && !visitedUrls.contains(linkUrl))
                            queue.add(linkUrl);
                    }

                    if (fetch) {
                        // Save contents
                        webPage.setUrl(currUrl);
                        webPage.setTitle(fetcherResponse.getTitle());
                        webPage.setContent(html.html());
                        webPage.setHeaders(headers);
                        if (webPage.getCreatedDate() == null)
                            webPage.setCreatedDate(new Date());
                        webPage.setLastModifiedDate(lastModifiedDate);
                        webPage.setLinks(currUrlLinks);
                    }
                    webPage.setLastFetchedDate(new Date());
                    webPageRepository.save(webPage);

                    if (!urlToObjectIdMapping.containsKey(currUrl)) {
                        // Migrate everything in urlToParentUrlsMapping to objectIdToParentUrlsMapping
                        this.objectIdToParentUrlsMapping.put(webPage.getId(), urlToParentUrlsMapping.getOrDefault(currUrl, new HashSet<>()));
                        this.urlToObjectIdMapping.put(webPage.getId(), currUrl);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Flush urls to database
                if (visitCount % CHECKPOINT_INTERVAL == 0)
                    flushTaskCache();
            }
            ++level;
        }
        crawlerTask = flushTaskCache();
        crawlerTask.setTaskStatus(CrawlerTask.TASK_FINISHED);
        crawlerTaskRepository.save(crawlerTask);

        fetcher.close();
    }

    private boolean checkIsStopped() {
        CrawlerTask crawlerTask = getCurrentCrawlerTask();
        return crawlerTask.getTaskStatus() == CrawlerTask.TASK_STOPPED;
    }

    private CrawlerTask flushTaskCache() {
        CrawlerTask crawlerTask = getCurrentCrawlerTask();

        // Flush crawled urls
        if (this.urls != null && this.urls.size() > 0) {
            if (crawlerTask.getVisitedUrls() == null)
                crawlerTask.setVisitedUrls(new LinkedList<>());
            crawlerTask.getVisitedUrls().addAll(this.urls);
            crawlerTaskRepository.save(crawlerTask);
            this.urls.clear();
        }

        // Flush parent pointers
        crawlerTask.setParentPointers(this.objectIdToParentUrlsMapping);

        // Save
        return crawlerTask;
    }

    private CrawlerTask getCurrentCrawlerTask() {
        Optional<CrawlerTask> optional = crawlerTaskRepository.findById(this.taskId);
        if (!optional.isPresent())
            throw new NotFoundException("Object " + this.taskId + " not found.");
        return optional.get();
    }
}
