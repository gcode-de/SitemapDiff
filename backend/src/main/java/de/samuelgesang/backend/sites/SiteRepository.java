package de.samuelgesang.backend.sites;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SiteRepository extends MongoRepository<Site, String> {
    List<Site> findByUserId(String userId);

    Optional<Site> findByIdAndUserId(String id, String userId);

    List<Site> findByCrawlSchedule(String crawlSchedule);
}
