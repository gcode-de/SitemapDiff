package de.samuelgesang.backend.crawls;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@Document(collection = "crawls")
public class Crawl {
    @Id
    private String id;
    private String siteId;
    private String finishedAt;
    private String prevCrawlId;
    private List<String> urlChunkIds;
    private List<CrawlDiffItem> diffToPrevCrawl;
}
