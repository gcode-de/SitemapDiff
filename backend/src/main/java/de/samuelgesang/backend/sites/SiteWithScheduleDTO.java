package de.samuelgesang.backend.sites;

import lombok.Data;

@Data
public class SiteWithScheduleDTO {
    private String id;
    private String name;
    private String baseURL;
    private String sitemap;
    private String favicon;
    private String userId;
    private String crawlSchedule;
}
