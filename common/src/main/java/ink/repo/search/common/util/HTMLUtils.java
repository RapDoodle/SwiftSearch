package ink.repo.search.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HTMLUtils {

    public static Document parseHTML(String html) {
        return Jsoup.parse(html.toString());
    }

    public static Document parseHTML(String html, String url) {
        Document document = parseHTML(html);
        document.setBaseUri(url);
        return document;
    }

}
