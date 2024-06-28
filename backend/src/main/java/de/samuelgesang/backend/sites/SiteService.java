package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.crawls.CrawlService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteService {

    private final SiteRepository siteRepository;
    private final CrawlRepository crawlRepository;
    private final CrawlService crawlService;

    public SiteService(SiteRepository siteRepository,
                       CrawlRepository crawlRepository,
                       @Lazy CrawlService crawlService) {
        this.siteRepository = siteRepository;
        this.crawlRepository = crawlRepository;
        this.crawlService = crawlService;
    }

    public List<Site> getAllSites(String userId) {
        return siteRepository.findByUserId(userId);
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
            crawlService.deleteCrawlsBySiteId(id);
            siteRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Site not found or user unauthorized");
        }
    }

    public List<SiteWithCrawlsDTO> getAllSitesWithCrawls(String userId) {
        List<Site> sites = siteRepository.findByUserId(userId);
        return sites.stream().map(this::mapToDTO).toList();
    }

    public Optional<SiteWithCrawlsDTO> getSiteWithCrawlsByIdAndUser(String siteId, String userId) {
        Optional<Site> site = siteRepository.findByIdAndUserId(siteId, userId);
        return site.map(this::mapToDTO);
    }

    private SiteWithCrawlsDTO mapToDTO(Site site) {
        SiteWithCrawlsDTO dto = new SiteWithCrawlsDTO();
        dto.setId(site.getId());
        dto.setName(site.getName());
        dto.setBaseURL(site.getBaseURL());
        dto.setSitemap(site.getSitemap());
        dto.setUserId(site.getUserId());
        dto.setScrapeCron(site.getScrapeCron());
        dto.setCrawlIds(site.getCrawlIds());
        List<Crawl> crawls = crawlRepository.findByIdIn(site.getCrawlIds());
        dto.setCrawls(crawls);
        return dto;
    }
}
