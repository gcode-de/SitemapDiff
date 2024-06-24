package de.samuelgesang.backend.sites;

import org.springframework.beans.factory.annotation.Autowired;
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
}
