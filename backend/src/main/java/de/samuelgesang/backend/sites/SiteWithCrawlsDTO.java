package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;

import java.util.List;

public class SiteWithCrawlsDTO {
    private String id;
    private String name;
    private String baseURL;
    private String sitemap;
    private String userId;
    private String scrapeCron;
    private List<String> crawlIds;
    private List<Crawl> crawls;

    // Getter und Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getSitemap() {
        return sitemap;
    }

    public void setSitemap(String sitemap) {
        this.sitemap = sitemap;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getScrapeCron() {
        return scrapeCron;
    }

    public void setScrapeCron(String scrapeCron) {
        this.scrapeCron = scrapeCron;
    }

    public List<String> getCrawlIds() {
        return crawlIds;
    }

    public void setCrawlIds(List<String> crawlIds) {
        this.crawlIds = crawlIds;
    }

    public List<Crawl> getCrawls() {
        return crawls;
    }

    public void setCrawls(List<Crawl> crawls) {
        this.crawls = crawls;
    }
}
