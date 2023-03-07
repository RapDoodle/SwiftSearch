package ink.repo.search.crawler.repository;

import ink.repo.search.crawler.model.WebPage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WebPageRepository extends MongoRepository<WebPage, String> {
}
