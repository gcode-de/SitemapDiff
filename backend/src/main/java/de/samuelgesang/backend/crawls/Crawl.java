package de.samuelgesang.backend.crawls;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "crawls")
public class Crawl {
    @Id
    private String id;
    private String siteId;
    private String finishedAt;
    private String prevCrawlId;
    private List<String> urlChunkIds;
    private List<CrawlDiffItem> diffToPrevCrawl;

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public String getPrevCrawlId() {
        return prevCrawlId;
    }

    public void setPrevCrawlId(String prevCrawlId) {
        this.prevCrawlId = prevCrawlId;
    }

    public List<String> getUrlChunkIds() {
        return urlChunkIds;
    }

    public void setUrlChunkIds(List<String> urlChunkIds) {
        this.urlChunkIds = urlChunkIds;
    }

    public List<CrawlDiffItem> getDiffToPrevCrawl() {
        return diffToPrevCrawl;
    }

    public void setDiffToPrevCrawl(List<CrawlDiffItem> diffToPrevCrawl) {
        this.diffToPrevCrawl = diffToPrevCrawl;
    }
}
