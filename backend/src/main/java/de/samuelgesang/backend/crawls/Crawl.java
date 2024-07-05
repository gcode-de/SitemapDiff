package de.samuelgesang.backend.crawls;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ToString
@Document(collection = "crawls")
public class Crawl {
    @Id
    private String id;
    private String siteId;
    private String finishedAt;
    private String prevCrawlId;
    private List<String> urlChunkIds;
    private List<CrawlDiffItem> diffToPrevCrawl = new ArrayList<>();
}
