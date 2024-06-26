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
    private List<String> urls;
    private String prevCrawlId;
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

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getPrevCrawlId() {
        return prevCrawlId;
    }

    public void setPrevCrawlId(String prevCrawlId) {
        this.prevCrawlId = prevCrawlId;
    }

    public List<CrawlDiffItem> getDiffToPrevCrawl() {
        return diffToPrevCrawl;
    }

    public void setDiffToPrevCrawl(List<CrawlDiffItem> diffToPrevCrawl) {
        this.diffToPrevCrawl = diffToPrevCrawl;
    }
}
