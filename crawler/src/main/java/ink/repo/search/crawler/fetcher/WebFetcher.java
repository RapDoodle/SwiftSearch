package ink.repo.search.crawler.fetcher;

import ink.repo.search.crawler.model.FetcherResponse;
import ink.repo.search.crawler.util.HTMLParser;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;

public class WebFetcher implements Fetcher {
    HttpClient client;

    public WebFetcher() {
        client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

    }

    @Override
    public FetcherResponse fetch(String url) throws IOException {
        URLConnection connection = new URL(url).openConnection();

        // Get headers
        Map<String, String> headers = new HashMap<>();
        Set<String> headerKeys = connection.getHeaderFields().keySet();
        for (String headerKey : headerKeys) {
            headers.put(headerKey, connection.getHeaderField(headerKey));
        }

        // Read page content from buffer
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder html = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.length() == 0)
                continue;
            html.append(line);
            html.append('\n');
        }
        in.close();

        // Response
        Document parsedHTML = HTMLParser.parseHTML(html.toString(), url);
        FetcherResponse fetcherResponse = new FetcherResponse();
        fetcherResponse.setUrl(url);
        fetcherResponse.setContent(parsedHTML);
        fetcherResponse.setTitle(parsedHTML.title());
        fetcherResponse.setHeaders(headers);
        return fetcherResponse;
    }

    @Override
    public void close() {

    }
}
