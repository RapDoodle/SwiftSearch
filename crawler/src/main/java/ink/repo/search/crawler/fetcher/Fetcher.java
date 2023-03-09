package ink.repo.search.crawler.fetcher;

import ink.repo.search.crawler.model.FetcherResponse;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Fetcher {

    FetcherResponse fetch(String url) throws IOException, InterruptedException;

    void close();
}
