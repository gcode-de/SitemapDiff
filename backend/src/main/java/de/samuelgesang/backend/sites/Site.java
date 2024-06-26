package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "sites")
public class Site {
    @Id
    private String id;
    private String name;
    private String baseURL;
    private String sitemap;
    private String userId;
    private String scrapeCron;
    private List<String> crawlIds;
    private List<Crawl> crawls;


    public Site() {
    }

    public Site(String id, String name, String baseURL, String sitemap, String userId, String scrapeCron, List<String> crawlIds) {
        this.id = id;
        this.name = name;
        this.baseURL = baseURL;
        this.sitemap = sitemap;
        this.userId = userId;
        this.scrapeCron = scrapeCron;
        this.crawlIds = crawlIds;
    }

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
