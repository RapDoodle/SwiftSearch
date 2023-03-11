package ink.repo.search.crawler.fetcher;

import ink.repo.search.common.util.HTMLUtils;
import ink.repo.search.crawler.model.FetcherResponse;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.nodes.Document;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v108.network.Network;
import org.openqa.selenium.devtools.v108.network.model.Headers;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SeleniumFetcher implements Fetcher {
    private WebDriver driver;
    private List<Headers> headerList;
    private Integer currResponseStatusCode;

    public SeleniumFetcher(int timeout, boolean headless) {
        // Automatically setup Chrome driver
        WebDriverManager.chromedriver().setup();

        // Driver options
        ChromeOptions options = new ChromeOptions();
        if (headless)
            options.addArguments("--headless");
        // Disable file download
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download_restrictions", 3);
        options.setExperimentalOption("prefs", prefs);

        // Startup Chrome
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);

        // Listen on headers
        DevTools devTools = ((HasDevTools) driver).getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.addListener(Network.responseReceived(),
                response -> {
                    if (this.headerList == null)
                        return;
                    Headers headers = response.getResponse().getHeaders();
                    if (!headers.isEmpty())
                        this.headerList.add(headers);
                    if (this.currResponseStatusCode == null) {
                        // Only store the first response status code for each fetch
                        if (response.getResponse().getStatus() == null)
                            this.currResponseStatusCode = -1;
                        else
                            this.currResponseStatusCode = response.getResponse().getStatus();
                    }
                }
        );
    }

    public SeleniumFetcher() {
        this(5, false);
    }

    @Override
    public FetcherResponse fetch(String url) throws IOException, InterruptedException {
        // Clear the list of headers
        if (this.headerList == null)
            this.headerList = new ArrayList<>();
        else
            this.headerList.clear();

        // Set the current response code to null to obtain the first response code
        // from the first request received
        this.currResponseStatusCode = null;

        // Fetch from the web
        try {
            driver.get(url);
        } catch (TimeoutException e) {

        }
        String content = driver.getPageSource();

        // Get the request header
        Map<String, String> headers = new HashMap<>();
        if (this.headerList.size() >= 1) {
            this.headerList.get(0).forEach((key,value) -> {
                headers.put(key, (String) value);
            });
        }

        // Response
        Document parsedHTML = HTMLUtils.parseHTML(content, url);
        FetcherResponse fetcherResponse = new FetcherResponse();
        fetcherResponse.setUrl(url);
        fetcherResponse.setContent(parsedHTML);
        fetcherResponse.setTitle(driver.getTitle());
        fetcherResponse.setHeaders(headers);
        fetcherResponse.setResponseStatusCode(this.currResponseStatusCode);
        return fetcherResponse;
    }

    @Override
    public void close() {
        this.driver.close();
    }
}
