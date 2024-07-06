package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.url_chunk.UpdateUrlStatusDTO;
import de.samuelgesang.backend.sites.SiteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class CrawlServiceTest {

    @Autowired
    private CrawlService crawlService;

    @MockBean
    private CrawlRepository crawlRepository;

    @MockBean
    private SiteRepository siteRepository;

    private Site site;
    private Crawl crawl;

    @BeforeEach
    void setUp() {
        site = new Site();
        site.setId("siteId");
        site.setName("Test Site");
        site.setBaseURL("https://example.com");
        site.setSitemap("https://example.com/sitemap.xml");
        site.setCrawlIds(new ArrayList<>());

        crawl = new Crawl();
        crawl.setId("crawlId");
        crawl.setSiteId(site.getId());
        crawl.setDiffToPrevCrawl(new ArrayList<>());
    }


    @Test
    void updateUrlCheckedStatusUrlNotFound() {
        when(crawlRepository.findById(any(String.class))).thenReturn(Optional.of(crawl));

        UpdateUrlStatusDTO updateUrlStatusDTO = new UpdateUrlStatusDTO();
        updateUrlStatusDTO.setUrl("https://nonexistent-url.com");
        updateUrlStatusDTO.setChecked(true);

        Crawl result = crawlService.updateUrlCheckedStatus("crawlId", updateUrlStatusDTO);

        assertNull(result);
    }

    @Test
    void deleteCrawlUnauthorized() {
        when(crawlRepository.findById(any(String.class))).thenReturn(Optional.of(crawl));
        when(siteRepository.findById(any(String.class))).thenReturn(Optional.of(site));

        assertThrows(RuntimeException.class, () -> crawlService.deleteCrawl("crawlId", "unauthorizedUserId"));
    }
}
