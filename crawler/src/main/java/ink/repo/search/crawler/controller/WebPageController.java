package ink.repo.search.crawler.controller;

import ink.repo.search.common.dto.WebPageRequest;
import ink.repo.search.crawler.exception.ArgumentNotFoundException;
import ink.repo.search.crawler.exception.NotFoundException;
import ink.repo.search.crawler.model.WebPageResponse;
import ink.repo.search.crawler.service.WebPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webpage")
public class WebPageController {
    @Autowired
    private WebPageService webPageService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public WebPageResponse getWebPage(WebPageRequest webPageRequest) throws NotFoundException, ArgumentNotFoundException {
        return webPageService.getWebPage(webPageRequest);
    }
}
