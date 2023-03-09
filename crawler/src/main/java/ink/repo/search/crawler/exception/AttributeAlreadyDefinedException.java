package ink.repo.search.crawler.exception;

public class AttributeAlreadyDefinedException extends Exception {
    public AttributeAlreadyDefinedException() {
        super("The attribute has been defined.");
    }
}
