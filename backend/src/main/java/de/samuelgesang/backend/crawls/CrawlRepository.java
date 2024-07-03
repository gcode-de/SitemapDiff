package de.samuelgesang.backend.crawls;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CrawlRepository extends MongoRepository<Crawl, String> {
    List<Crawl> findBySiteId(String siteId);

    List<Crawl> findByIdIn(List<String> crawlIds);
    
}
