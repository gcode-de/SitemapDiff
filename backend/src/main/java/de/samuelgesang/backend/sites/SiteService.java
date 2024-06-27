package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.crawls.CrawlService;
import de.samuelgesang.backend.sitemaps.SitemapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteService {

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private CrawlRepository crawlRepository;

    @Autowired
    private SitemapService sitemapService;

    @Autowired
    @Lazy
    private CrawlService crawlService;

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
            // Delete all crawls associated with the site
            crawlService.deleteCrawlsBySiteId(id);
            siteRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Site not found or user unauthorized");
        }
    }

    public List<Site> findByUser(String userId) {
        return siteRepository.findByUserId(userId);
    }

    public Site findByIdAndUser(String siteId, String userId) {
        Optional<Site> site = siteRepository.findByIdAndUserId(siteId, userId);
        return site.isPresent() ? site.get() : null;
    }

    public Site save(Site site) {
        return siteRepository.save(site);
    }

    public void crawlAllSites(String userId) throws Exception {
        List<Site> sites = findByUser(userId);
        for (Site site : sites) {
            Crawl crawl = sitemapService.crawlSite(site);
            crawlRepository.save(crawl);
            site.getCrawlIds().add(crawl.getId());
            save(site);
        }
    }

    public void crawlSiteById(String siteId, String userId) throws Exception {
        Site site = findByIdAndUser(siteId, userId);
        if (site == null) {
            throw new IllegalArgumentException("Site does not belong to the user.");
        }

        Crawl crawl = sitemapService.crawlSite(site);
        crawlRepository.save(crawl);
        site.getCrawlIds().add(crawl.getId());
        save(site);
    }
}
