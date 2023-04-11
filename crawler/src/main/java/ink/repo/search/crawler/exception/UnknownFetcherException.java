package ink.repo.search.crawler.exception;

public class UnknownFetcherException extends Exception {

    public UnknownFetcherException(String fetcher) {
        super("Unknown fetcher: " + fetcher);
    }
}
