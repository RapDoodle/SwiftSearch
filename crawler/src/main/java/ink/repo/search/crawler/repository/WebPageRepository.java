package ink.repo.search.crawler.repository;

import ink.repo.search.crawler.model.WebPage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WebPageRepository extends MongoRepository<WebPage, String> {
    Optional<WebPage> findByUrl(String url);
}
