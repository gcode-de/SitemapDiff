package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.crawls.CrawlService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
        String favicon = extractFavicon(site.getBaseURL());
        site.setFavicon(favicon);
        return siteRepository.save(site);
    }

    public Site updateSite(String id, Site site) {
        Site existingSite = siteRepository.findById(id).orElseThrow(() -> new RuntimeException("Site not found"));
        existingSite.setName(site.getName());
        existingSite.setBaseURL(site.getBaseURL());
        existingSite.setSitemap(site.getSitemap());
        existingSite.setScrapeCron(site.getScrapeCron());
        String favicon = extractFavicon(site.getBaseURL());
        existingSite.setFavicon(favicon);
        return siteRepository.save(existingSite);
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
        dto.setFavicon(site.getFavicon());
        dto.setUserId(site.getUserId());
        dto.setScrapeCron(site.getScrapeCron());
        dto.setCrawlIds(site.getCrawlIds());
        List<Crawl> crawls = crawlRepository.findByIdIn(site.getCrawlIds());
        dto.setCrawls(crawls);
        return dto;
    }

    private String extractFavicon(String baseURL) {
        try {
            Document doc = Jsoup.connect(baseURL).get();
            Element iconLink = doc.select("link[rel~=(?i)^(shortcut|icon|apple-touch-icon)]").first();

            if (iconLink != null) {
                String iconUrl = iconLink.attr("href");
                if (!iconUrl.startsWith("http")) {
                    return URI.create(baseURL).resolve(iconUrl).toString();
                } else {
                    return iconUrl;
                }
            } else {
                return URI.create(baseURL).resolve("/favicon.ico").toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
