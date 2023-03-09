package ink.repo.search.crawler.fetcher;

import ink.repo.search.crawler.model.FetcherResponse;

import java.io.IOException;

public interface Fetcher {

    FetcherResponse fetch(String url) throws IOException, InterruptedException;

    void close();
}
