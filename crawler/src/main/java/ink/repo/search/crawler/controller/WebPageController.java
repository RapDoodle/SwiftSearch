package ink.repo.search.crawler.controller;

import ink.repo.search.common.dto.CrawledWebPageRequest;
import ink.repo.search.common.dto.CrawledWebPageResponse;
import ink.repo.search.common.dto.CrawledWebPagesRequest;
import ink.repo.search.common.dto.CrawledWebPagesResponse;
import ink.repo.search.crawler.exception.ArgumentNotFoundException;
import ink.repo.search.crawler.exception.NotFoundException;
import ink.repo.search.crawler.service.WebPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/webpage")
public class WebPageController {
    @Autowired
    private WebPageService webPageService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CrawledWebPageResponse getWebPage(CrawledWebPageRequest crawledWebPageRequest) throws NotFoundException, ArgumentNotFoundException {
        return webPageService.getWebPage(crawledWebPageRequest);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public CrawledWebPagesResponse getWebPages(@RequestBody CrawledWebPagesRequest crawledWebPagesRequest) {
        return webPageService.getWebPages(crawledWebPagesRequest);
    }
}
