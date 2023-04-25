package ink.repo.search.crawler.service;

import ink.repo.search.common.dto.CrawledWebPageRequest;
import ink.repo.search.common.dto.CrawledWebPageResponse;
import ink.repo.search.common.dto.CrawledWebPagesRequest;
import ink.repo.search.common.dto.CrawledWebPagesResponse;
import ink.repo.search.crawler.exception.ArgumentNotFoundException;
import ink.repo.search.crawler.exception.NotFoundException;
import ink.repo.search.crawler.model.WebPage;
import ink.repo.search.crawler.repository.WebPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPageService {
    @Autowired
    private final WebPageRepository webPageRepository;

    public CrawledWebPageResponse getWebPage(CrawledWebPageRequest crawledWebPageRequest) throws NotFoundException, ArgumentNotFoundException {
        Optional<WebPage> webPageOptional = null;
        if (crawledWebPageRequest.getId() != null)
            // Try to get by id
            webPageOptional = webPageRepository.findById(crawledWebPageRequest.getId());
        else if (crawledWebPageRequest.getUrl() != null)
            // Try to get by url
            webPageOptional = webPageRepository.findByUrl(crawledWebPageRequest.getUrl());
        else
            throw new ArgumentNotFoundException();

        WebPage webPage = null;
        if (webPageOptional.isPresent())
            webPage = webPageOptional.get();
        else
            throw new NotFoundException();

        // Response
        CrawledWebPageResponse response = webPage.toResponse();
        return response;
    }

    public CrawledWebPagesResponse getWebPages(CrawledWebPagesRequest crawledWebPagesRequest) {
        List<String> urls = crawledWebPagesRequest.getUrls();
        List<WebPage> webPages = webPageRepository.findWebPagesByUrlIn(urls);
        List<CrawledWebPageResponse> webPagesResponse = new ArrayList<>();
        for (WebPage webPage : webPages) {
            webPagesResponse.add(webPage.toResponse());
        }
        CrawledWebPagesResponse crawledWebPagesResponse = new CrawledWebPagesResponse();
        crawledWebPagesResponse.setPages(webPagesResponse);
        return crawledWebPagesResponse;
    }
}
