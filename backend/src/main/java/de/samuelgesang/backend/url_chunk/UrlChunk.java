package de.samuelgesang.backend.url_chunk;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "url_chunks")
@ToString
public class UrlChunk {
    @Id
    private String id;
    private String crawlId;
    private List<UrlObject> urls;

    public UrlChunk() {
    }

    public UrlChunk(String crawlId, List<UrlObject> urls) {
        this.crawlId = crawlId;
        this.urls = urls;
    }
}


