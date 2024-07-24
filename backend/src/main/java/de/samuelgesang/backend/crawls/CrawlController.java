package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.exceptions.BadRequestException;
import de.samuelgesang.backend.exceptions.ResourceNotFoundException;
import de.samuelgesang.backend.exceptions.UnauthorizedAccessException;
import de.samuelgesang.backend.sitemaps.SitemapService;
import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteService;
import de.samuelgesang.backend.url_chunk.UpdateUrlStatusDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/crawls")
@RequiredArgsConstructor
public class CrawlController {

    private final SiteService siteService;
    private final SitemapService sitemapService;
    private final CrawlRepository crawlRepository;
    private final CrawlService crawlService;

    @GetMapping("/start/{siteId}")
    public ResponseEntity<String> crawlSiteById(@PathVariable String siteId, @AuthenticationPrincipal OAuth2User user) {
        try {
            Map<String, Object> attributes = user.getAttributes();
            String userId = (String) attributes.get("sub");

            Optional<Site> optionalSite = siteService.getSiteById(siteId);
            if (optionalSite.isEmpty()) {
                throw new ResourceNotFoundException("Site not found.");
            }

            Site site = optionalSite.get();
            if (!site.getUserId().equals(userId)) {
                throw new UnauthorizedAccessException("Site does not belong to the user.");
            }

            if (site.getSitemap() == null || site.getSitemap().isEmpty()) {
                throw new BadRequestException("Site does not have a sitemap URL.");
            }

            Crawl crawl = crawlService.crawlSite(site);
            crawlRepository.save(crawl);
            site.getCrawlIds().add(crawl.getId());
            siteService.updateSite(site.getId(), site);

            return ResponseEntity.ok("Site crawled successfully.");
        } catch (ResourceNotFoundException | UnauthorizedAccessException | BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/update-url-status/{crawlId}")
    public ResponseEntity<Crawl> updateUrlCheckedStatus(@PathVariable String crawlId,
                                                        @RequestBody UpdateUrlStatusDTO updateUrlStatusDTO,
                                                        @AuthenticationPrincipal OAuth2User user) {
        try {
            if (user == null) {
                throw new UnauthorizedAccessException("User not authorized.");
            }
            log.info("Received request to update URL checked status: crawlId={}, updateUrlStatusDTO={}", crawlId, updateUrlStatusDTO);

            Crawl updatedCrawl = crawlService.updateUrlCheckedStatus(crawlId, updateUrlStatusDTO);

            if (updatedCrawl != null) {
                return ResponseEntity.ok(updatedCrawl);
            } else {
                throw new ResourceNotFoundException("Crawl not found.");
            }
        } catch (ResourceNotFoundException | UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error updating URL checked status: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{crawlId}")
    public ResponseEntity<String> deleteCrawl(@PathVariable String crawlId, @AuthenticationPrincipal OAuth2User user) {
        try {
            Map<String, Object> attributes = user.getAttributes();
            String userId = (String) attributes.get("sub");

            crawlService.deleteCrawl(crawlId, userId);
            return ResponseEntity.ok("Crawl deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
