package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteService {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private CrawlRepository crawlRepository;

    public List<Site> getAllSites(String userId) {
        List<Site> sites = siteRepository.findByUserId(userId);
        sites.forEach(site -> {
            List<Crawl> crawls = crawlRepository.findByIdIn(site.getCrawlIds());
            site.setCrawls(crawls);
        });
        return sites;
    }

    public Optional<Site> getSiteById(String id) {
        Optional<Site> site = siteRepository.findById(id);
        site.ifPresent(s -> {
            List<Crawl> crawls = crawlRepository.findByIdIn(s.getCrawlIds());
            s.setCrawls(crawls);
        });
        return site;
    }
}
