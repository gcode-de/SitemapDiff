package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.sitemaps.SitemapService;
import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/crawls")
public class CrawlController {

    @Autowired
    private SiteService siteService;

    @Autowired
    private SitemapService sitemapService;

    @Autowired
    private CrawlRepository crawlRepository;

    @GetMapping("/start/{siteId}")
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
