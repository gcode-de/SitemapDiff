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
        return siteRepository.findById(id);
    }

    public Site createSite(Site site) {
        return siteRepository.save(site);
    }

    public Site updateSite(String id, Site site) {
        if (siteRepository.existsById(id)) {
            site.setId(id);
            return siteRepository.save(site);
        }
        throw new IllegalArgumentException("Site not found");
    }

    public void deleteSite(String id, String userId) {
        Optional<Site> site = siteRepository.findById(id);
        if (site.isPresent() && site.get().getUserId().equals(userId)) {
            siteRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Site not found or user unauthorized");
        }
    }
}
