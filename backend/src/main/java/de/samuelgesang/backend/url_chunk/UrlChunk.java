package de.samuelgesang.backend.url_chunk;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@Document(collection = "url_chunks")
public class UrlChunk {
    @Id
    private String id;
    private String crawlId;
    private List<String> urls;
}