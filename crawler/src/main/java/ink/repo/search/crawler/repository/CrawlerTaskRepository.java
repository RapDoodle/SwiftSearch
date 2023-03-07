package ink.repo.search.crawler.repository;

import ink.repo.search.crawler.model.CrawlerTask;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CrawlerTaskRepository extends MongoRepository<CrawlerTask, String> {
}
