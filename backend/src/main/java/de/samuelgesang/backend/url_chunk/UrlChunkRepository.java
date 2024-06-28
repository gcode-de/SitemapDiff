package de.samuelgesang.backend.url_chunk;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UrlChunkRepository extends MongoRepository<UrlChunk, String> {
    List<UrlChunk> findByCrawlId(String crawlId);

    void deleteByCrawlId(String crawlId);
}
