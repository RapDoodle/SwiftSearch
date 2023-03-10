package ink.repo.search.indexer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "The task is not running")
public class AlreadyStoppedException extends Exception {
    public AlreadyStoppedException() {
        super("The task is not running");
    }
}
