package ink.repo.search.crawler.fetcher;

import ink.repo.search.crawler.parser.HTMLParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.nodes.Document;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SeleniumFetcher implements Fetcher {

    WebDriver driver;

    public SeleniumFetcher(int timeout, boolean headless) {
        // Automatically setup Chrome driver
        WebDriverManager.chromedriver().setup();

        // Driver options
        ChromeOptions options = new ChromeOptions();
        if (headless)
            options.addArguments("--headless");

        // Startup Chrome
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(timeout, TimeUnit.SECONDS);
    }

    public SeleniumFetcher() {
        this(5, false);
    }

    @Override
    public Document fetch(String url) throws IOException, InterruptedException {
        try {
            driver.get(url);
        } catch (TimeoutException e) {

        }
        String content = driver.getPageSource();

        // Parse the HTML
        return HTMLParser.parseHTML(content, url);
    }

    @Override
    public void close() {
        this.driver.close();
    }
}
