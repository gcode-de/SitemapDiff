package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.sitemaps.SitemapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    @Autowired
    private SitemapService sitemapService;

    @Autowired
    private CrawlRepository crawlRepository;

    @GetMapping
    public List<Site> getAllSites(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return siteService.getAllSites("null");
        }

        Map<String, Object> attributes = user.getAttributes();
        String userId = (String) attributes.get("sub");

        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in OAuth2 user attributes");
        }

        return siteService.getAllSites(userId);
    }

    @GetMapping("/{id}")
    public Optional<Site> getSiteById(@PathVariable String id) {
        return siteService.getSiteById(id);
    }

    @PostMapping
    public Site createSite(@RequestBody Site site, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        Map<String, Object> attributes = user.getAttributes();
        String userId = (String) attributes.get("sub");

        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in OAuth2 user attributes");
        }

        site.setUserId(userId);
        return siteService.createSite(site);
    }

    @PutMapping("/{id}")
    public Site updateSite(@PathVariable String id, @RequestBody Site site, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        Map<String, Object> attributes = user.getAttributes();
        String userId = (String) attributes.get("sub");

        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in OAuth2 user attributes");
        }

        site.setUserId(userId);
        return siteService.updateSite(id, site);
    }

    @DeleteMapping("/{id}")
    public void deleteSite(@PathVariable String id, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        Map<String, Object> attributes = user.getAttributes();
        String userId = (String) attributes.get("sub");

        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in OAuth2 user attributes");
        }

        siteService.deleteSite(id, userId);
    }

    @GetMapping("/crawl/all")
    public ResponseEntity<?> crawlAllSites(@AuthenticationPrincipal OAuth2User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            Map<String, Object> attributes = user.getAttributes();
            String userId = (String) attributes.get("sub");

            List<Site> userSites = siteService.getAllSites(userId);
            for (Site site : userSites) {
                Crawl crawl = sitemapService.crawlSite(site);
                crawlRepository.save(crawl);
                site.getCrawlIds().add(crawl.getId());
                siteService.updateSite(site.getId(), site);
            }
            return ResponseEntity.ok("All sites crawled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/crawl/{siteId}")
    public ResponseEntity<?> crawlSiteById(@PathVariable String siteId, @AuthenticationPrincipal OAuth2User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            Map<String, Object> attributes = user.getAttributes();
            String userId = (String) attributes.get("sub");

            Optional<Site> optionalSite = siteService.getSiteById(siteId);
            if (optionalSite.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Site not found.");
            }

            Site site = optionalSite.get();
            if (!site.getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Site does not belong to the user.");
            }

            if (site.getSitemap() == null || site.getSitemap().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Site does not have a sitemap URL.");
            }

            Crawl crawl = sitemapService.crawlSite(site);
            crawlRepository.save(crawl);
            site.getCrawlIds().add(crawl.getId());
            siteService.updateSite(site.getId(), site);

            return ResponseEntity.ok("Site crawled successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
