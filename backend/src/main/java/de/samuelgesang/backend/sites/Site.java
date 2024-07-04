package de.samuelgesang.backend.sites;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Document(collection = "sites")
public class Site {

    @Id
    private String id;
    private String name;
    private String baseURL;
    private String sitemap;
    private String favicon;
    private String userId;
    private String crawlSchedule;
    private String email;
    private List<String> crawlIds;

    public Site(String id, String name, String baseURL, String sitemap, String favicon, String userId, List<String> crawlIds, String email, String crawlSchedule) {
        this.id = id;
        this.name = name;
        this.baseURL = baseURL;
        this.sitemap = sitemap;
        this.favicon = favicon;
        this.userId = userId;
        this.crawlSchedule = crawlSchedule != null ? crawlSchedule : "";
        this.email = email != null ? email : "";
        this.crawlIds = crawlIds != null ? crawlIds : new ArrayList<>();
    }
}
