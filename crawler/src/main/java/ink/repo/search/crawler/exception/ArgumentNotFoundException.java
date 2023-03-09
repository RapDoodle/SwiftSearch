package ink.repo.search.crawler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Request param not Found")
public class ArgumentNotFoundException extends Exception {
}
