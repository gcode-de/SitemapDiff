package de.samuelgesang.backend.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sitemaps")
public class SitemapController {

    @Autowired
    private SitemapService sitemapService;

    @GetMapping("/find")
    public ResponseEntity<?> findSitemaps(@RequestParam String baseURL) {
        try {
            String[] sitemaps = sitemapService.findSitemaps(baseURL);
            return ResponseEntity.ok(sitemaps);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
