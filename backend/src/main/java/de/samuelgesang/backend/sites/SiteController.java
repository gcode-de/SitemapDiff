package de.samuelgesang.backend.sites;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/sites")
public class SiteController {

    @Autowired
    private SiteService siteService;

    @GetMapping
    public List<Site> getAllSites() {
        return siteService.getAllSites();
    }

    @GetMapping("/{id}")
    public Optional<Site> getSiteById(@PathVariable String id) {
        return siteService.getSiteById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Site> getSitesByUserId(@PathVariable String userId) {
        return siteService.getSitesByUserId(userId);
    }

    @PostMapping
    public Site createSite(@RequestBody Site site) {
        return siteService.createSite(site);
    }

    @PutMapping("/{id}")
    public Site updateSite(@PathVariable String id, @RequestBody Site site) {
        return siteService.updateSite(id, site);
    }

    @DeleteMapping("/{id}")
    public void deleteSite(@PathVariable String id) {
        siteService.deleteSite(id);
    }
}
