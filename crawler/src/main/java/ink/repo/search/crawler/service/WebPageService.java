package ink.repo.search.crawler.service;

import ink.repo.search.crawler.dto.WebPageRequest;
import ink.repo.search.crawler.dto.WebPageResponse;
import ink.repo.search.crawler.exception.ArgumentNotFoundException;
import ink.repo.search.crawler.exception.NotFoundException;
import ink.repo.search.crawler.model.WebPage;
import ink.repo.search.crawler.repository.WebPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPageService {
    @Autowired
    private final WebPageRepository webPageRepository;

    public WebPageResponse getWebPage(WebPageRequest webPageRequest) throws NotFoundException, ArgumentNotFoundException {
        Optional<WebPage> webPageOptional = null;
        if (webPageRequest.getId() != null)
            // Try to get by id
            webPageOptional = webPageRepository.findById(webPageRequest.getId());
        else if (webPageRequest.getUrl() != null)
            // Try to get by url
            webPageOptional = webPageRepository.findByUrl(webPageRequest.getUrl());
        else
            throw new ArgumentNotFoundException();

        WebPage webPage = null;
        if (webPageOptional.isPresent())
            webPage = webPageOptional.get();
        else
            throw new NotFoundException();

        // Response
        WebPageResponse response = new WebPageResponse();
        response.setId(webPage.getId());
        response.setUrl(webPage.getUrl());
        response.setTitle(webPage.getTitle());
        response.setResponseStatusCode(webPage.getResponseStatusCode());
        response.setHeaders(webPage.getHeaders());
        response.setContent(webPage.getContent());
        response.setLinks(webPage.getLinks());
        response.setCreatedDate(webPage.getCreatedDate());
        response.setLastFetchedDate(webPage.getLastFetchedDate());
        response.setLastModifiedDate(webPage.getLastModifiedDate());
        return response;
    }
}
