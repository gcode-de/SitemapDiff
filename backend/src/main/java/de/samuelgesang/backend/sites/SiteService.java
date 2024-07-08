package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.crawls.CrawlService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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

    public Site createSite(SiteCreateDTO siteCreateDTO) {
        Site site = new Site();
        site.setName(siteCreateDTO.getName());
        site.setBaseURL(siteCreateDTO.getBaseURL());
        site.setSitemap(siteCreateDTO.getSitemap());
        site.setUserId(siteCreateDTO.getUserId());
        site.setCrawlSchedule(siteCreateDTO.getCrawlSchedule());
        site.setEmail(siteCreateDTO.getEmail());
        site.setCrawlIds(new ArrayList<>());

        String favicon = extractFavicon(site.getBaseURL());
        site.setFavicon(favicon);

        return siteRepository.save(site);
    }

    public Site updateSite(SiteUpdateDTO siteUpdateDTO) {
        Site existingSite = siteRepository.findById(siteUpdateDTO.getId())
                .orElseThrow(() -> new RuntimeException("Site not found"));

        existingSite.setName(siteUpdateDTO.getName());
        existingSite.setBaseURL(siteUpdateDTO.getBaseURL());
        existingSite.setSitemap(siteUpdateDTO.getSitemap());
        existingSite.setCrawlSchedule(siteUpdateDTO.getCrawlSchedule());
        existingSite.setEmail(siteUpdateDTO.getEmail());

        String favicon = extractFavicon(existingSite.getBaseURL());
        existingSite.setFavicon(favicon);

        return siteRepository.save(existingSite);
    }

    public Site updateSite(String id, Site site) {
        Site existingSite = siteRepository.findById(id).orElseThrow(() -> new RuntimeException("Site not found"));
        existingSite.setName(site.getName());
        existingSite.setBaseURL(site.getBaseURL());
        existingSite.setSitemap(site.getSitemap());
        existingSite.setCrawlSchedule(site.getCrawlSchedule());
        String favicon = extractFavicon(site.getBaseURL());
        existingSite.setFavicon(favicon);
        existingSite.setCrawlIds(site.getCrawlIds());
        existingSite.setEmail(site.getEmail());
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
        dto.setCrawlSchedule(site.getCrawlSchedule());
        dto.setCrawlIds(site.getCrawlIds());
        dto.setEmail(site.getEmail());
        List<Crawl> crawls = crawlRepository.findByIdIn(site.getCrawlIds());
        dto.setCrawls(crawls);
        return dto;
    }

    public List<Site> getAllSitesWithSchedule(String schedule) {
        return siteRepository.findByCrawlSchedule(schedule);
    }

    private String extractFavicon(String baseURL) {
        String[] prefixes = {"http://", "https://"};
        String[] domains = {"", "www."};

        for (String prefix : prefixes) {
            for (String domain : domains) {
                String testURL = prefix + domain + baseURL;
                String faviconURL = tryExtractFavicon(testURL);
                if (faviconURL != null) {
                    return faviconURL;
                }
            }
        }
        return null;
    }

    private String tryExtractFavicon(String baseURL) {
        try {
            Document doc = Jsoup.connect(baseURL).get();
            Element iconLink = doc.select("link[rel~=(?i)^(shortcut icon|icon|apple-touch-icon|alternate icon|mask-icon|fluid-icon|manifest)]").first();

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
            // Suppress the stack trace to avoid cluttering the logs
            return null;
        }
    }

}
