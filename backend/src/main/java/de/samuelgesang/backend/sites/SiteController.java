package de.samuelgesang.backend.sites;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class SiteController {

    @Autowired
    private SiteService siteService;

    @GetMapping("/api/sites")
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

    @GetMapping("/api/sites/{id}")
    public Optional<Site> getSiteById(@PathVariable String id, @AuthenticationPrincipal OAuth2User user) {
        if (user == null) {
            throw new IllegalArgumentException("User is not authenticated");
        }

        Map<String, Object> attributes = user.getAttributes();
        String userId = (String) attributes.get("sub");

        if (userId == null) {
            throw new IllegalArgumentException("User ID not found in OAuth2 user attributes");
        }

        Optional<Site> site = siteService.getSiteById(id);
        if (site.isPresent() && !userId.equals(site.get().getUserId())) {
            throw new IllegalArgumentException("User is not authorized to access this site");
        }

        return site;
    }
}
