package de.samuelgesang.backend.urlChunk;

import de.samuelgesang.backend.urlChunk.UrlChunk;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UrlChunkRepository extends MongoRepository<UrlChunk, String> {
}
