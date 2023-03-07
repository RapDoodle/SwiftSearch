package ink.repo.search.crawler.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTMLParser {

    public static Document parseHTML(String html) {
        return Jsoup.parse(html.toString());
    }

    public static Document parseHTML(String html, String url) {
        Document document = parseHTML(html);
        document.setBaseUri(url);
        return document;
    }

}
