package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SiteWithCrawlsDTO {

    private String id;
    private String name;
    private String baseURL;
    private String sitemap;
    private String favicon;
    private String userId;
    private String crawlSchedule;
    private String email;
    private List<String> crawlIds;
    private List<Crawl> crawls;

}
