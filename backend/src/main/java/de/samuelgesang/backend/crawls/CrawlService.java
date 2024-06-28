package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.sitemaps.SitemapService;
//import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteService;
import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrawlService {

    //    private final SitemapService sitemapService;
    private final CrawlRepository crawlRepository;
    private final UrlChunkRepository urlChunkRepository;

    @Autowired
    @Lazy
    private SiteService siteService;

    @Autowired
    public CrawlService(SitemapService sitemapService, CrawlRepository crawlRepository, UrlChunkRepository urlChunkRepository) {
//        this.sitemapService = sitemapService;
        this.crawlRepository = crawlRepository;
        this.urlChunkRepository = urlChunkRepository;
    }

//    public void crawlAllSites(String userId) throws Exception {
//        List<Site> sites = siteService.findByUser(userId);
//        for (Site site : sites) {
//            Crawl crawl = sitemapService.crawlSite(site);
//            crawlRepository.save(crawl);
//            site.getCrawlIds().add(crawl.getId());
//            siteService.save(site);
//        }
//    }

//    public void crawlSiteById(String siteId, String userId) throws Exception {
//        Site site = siteService.findByIdAndUser(siteId, userId);
//        if (site == null) {
//            throw new IllegalArgumentException("Site does not belong to the user.");
//        }
//
//        Crawl crawl = sitemapService.crawlSite(site);
//        crawlRepository.save(crawl);
//        site.getCrawlIds().add(crawl.getId());
//        siteService.save(site);
//    }

    public void deleteCrawlsBySiteId(String siteId) {
        List<Crawl> crawls = crawlRepository.findBySiteId(siteId);
        for (Crawl crawl : crawls) {
            // Delete URL chunks associated with the crawl
            urlChunkRepository.deleteByCrawlId(crawl.getId());
            crawlRepository.delete(crawl);
        }
    }
}
