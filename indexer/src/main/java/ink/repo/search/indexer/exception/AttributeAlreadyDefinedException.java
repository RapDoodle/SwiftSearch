package ink.repo.search.indexer.exception;

public class AttributeAlreadyDefinedException extends Exception {
    public AttributeAlreadyDefinedException() {
        super("The attribute has been defined.");
    }
}
