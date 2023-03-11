package ink.repo.search.common.exception;

public class AttributeAlreadyDefinedException extends Exception {
    public AttributeAlreadyDefinedException() {
        super("The attribute has been defined.");
    }
}
