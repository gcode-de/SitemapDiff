package de.samuelgesang.backend.sites;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Document(collection = "sites")
public class Site {

    private final String userId;
    private final String name;
    private final String baseURL;
    private final String[] sitemaps;
    private final List<String> crawlIds;
    @Setter
    @Id
    private String id;

    public Site(String id, String userId, String name, String baseURL, String[] sitemaps, List<String> crawlIds) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.baseURL = baseURL;
        this.sitemaps = sitemaps;
        this.crawlIds = crawlIds;
    }

}
