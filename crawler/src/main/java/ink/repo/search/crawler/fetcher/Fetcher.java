package ink.repo.search.crawler.fetcher;

import org.jsoup.nodes.Document;

import java.io.IOException;

public interface Fetcher {

    Document fetch(String url) throws IOException, InterruptedException;

    void close();
}
