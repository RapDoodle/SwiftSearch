package ink.repo.search.crawler.fetcher;

import ink.repo.search.crawler.parser.HTMLParser;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
    public ImmutablePair<Map<String, String>, Document> fetch(String url) throws IOException {
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

        // Parse the HTML
        return new ImmutablePair<>(headers, HTMLParser.parseHTML(html.toString(), url));
    }

    @Override
    public void close() {

    }
}
