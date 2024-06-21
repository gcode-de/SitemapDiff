package de.samuelgesang.backend.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SitemapController {

    @Autowired
    private SitemapService sitemapService;

    @GetMapping("/api/sitemaps/find")
    public String[] findSitemaps(@RequestParam String baseURL) {
        return sitemapService.findSitemaps(baseURL);
    }
}
