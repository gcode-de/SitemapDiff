package de.samuelgesang.backend.sites;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sites")
@Validated
public class SiteController {

    private final SiteService siteService;

    public SiteController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public List<SiteWithCrawlsDTO> getAllSites(@AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            return siteService.getAllSitesWithCrawls("null");
        }

        String userId = getUserId(user);
        return siteService.getAllSitesWithCrawls(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SiteWithCrawlsDTO> getSiteById(@PathVariable String id, @AuthenticationPrincipal OAuth2User user) {
        String userId = getUserId(user);
        Optional<SiteWithCrawlsDTO> site = siteService.getSiteWithCrawlsByIdAndUser(id, userId);
        return site.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Site> createSite(@Valid @RequestBody SiteCreateDTO siteCreateDTO, @AuthenticationPrincipal OAuth2User user) {
        String userId = getUserId(user);
        siteCreateDTO.setUserId(userId);
        Site site = siteService.createSite(siteCreateDTO);
        return ResponseEntity.ok(site);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Site> updateSite(@PathVariable String id, @Valid @RequestBody SiteUpdateDTO siteUpdateDTO, @AuthenticationPrincipal OAuth2User user) {
        String userId = getUserId(user);
        siteUpdateDTO.setId(id);
        siteUpdateDTO.setUserId(userId);
        Site site = siteService.updateSite(siteUpdateDTO);
        return ResponseEntity.ok(site);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSite(@PathVariable String id, @AuthenticationPrincipal OAuth2User user) {
        String userId = getUserId(user);
        siteService.deleteSite(id, userId);
        return ResponseEntity.noContent().build();
    }

    private String getUserId(OAuth2User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        Map<String, Object> attributes = user.getAttributes();
        String userId = (String) attributes.get("sub");

        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in OAuth2 user attributes");
        }

        return userId;
    }
}
