package de.samuelgesang.backend.sites;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SiteService {

    @Autowired
    private SiteRepository siteRepository;

    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    public Optional<Site> getSiteById(String id) {
        return siteRepository.findById(id);
    }

    public List<Site> getSitesByUserId(String userId) {
        return siteRepository.findByUserId(userId);
    }

    public Site createSite(Site site) {
        return siteRepository.save(site);
    }

    public Site updateSite(String id, Site site) {
        return siteRepository.save(new Site(id, site.getUserId(), site.getName(), site.getBaseURL(), site.getSitemaps(), site.getCrawlIds()));
    }

    public void deleteSite(String id) {
        siteRepository.deleteById(id);
    }

}
