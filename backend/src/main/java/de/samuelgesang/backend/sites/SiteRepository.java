package de.samuelgesang.backend.sites;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteRepository extends MongoRepository<Site, String> {
    List<Site> findByUserId(String userId);
}
