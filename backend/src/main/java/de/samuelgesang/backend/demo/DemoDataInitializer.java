package de.samuelgesang.backend.demo;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlDiffItem;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {

    static final String DEMO_SITE_ID = "demo-sitemapdiff";
    static final String ANONYMOUS_USER_ID = "null";

    private final SiteRepository siteRepository;
    private final CrawlRepository crawlRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (siteRepository.existsById(DEMO_SITE_ID)) {
            return;
        }

        Crawl initialCrawl = crawl(
                "demo-crawl-initial",
                "2026-08-18T08:00:00Z",
                null,
                List.of()
        );
        Crawl changedCrawl = crawl(
                "demo-crawl-changed",
                "2026-08-19T08:00:00Z",
                initialCrawl.getId(),
                List.of(
                        diff("add", "https://example.com/produkte/sitemap-monitoring"),
                        diff("add", "https://example.com/blog/website-aenderungen"),
                        diff("remove", "https://example.com/produkte/legacy-crawler")
                )
        );

        crawlRepository.saveAll(List.of(initialCrawl, changedCrawl));
        siteRepository.save(new Site(
                DEMO_SITE_ID,
                "Example.com",
                "https://example.com",
                "https://example.com/sitemap.xml",
                "https://example.com/favicon.ico",
                ANONYMOUS_USER_ID,
                "never",
                "",
                List.of(initialCrawl.getId(), changedCrawl.getId())
        ));
    }

    private Crawl crawl(String id, String finishedAt, String previousCrawlId, List<CrawlDiffItem> diff) {
        Crawl crawl = new Crawl();
        crawl.setId(id);
        crawl.setSiteId(DEMO_SITE_ID);
        crawl.setFinishedAt(finishedAt);
        crawl.setPrevCrawlId(previousCrawlId);
        crawl.setUrlChunkIds(List.of());
        crawl.setDiffToPrevCrawl(diff);
        return crawl;
    }

    private CrawlDiffItem diff(String action, String url) {
        CrawlDiffItem item = new CrawlDiffItem();
        item.setAction(action);
        item.setUrl(url);
        item.setChecked(false);
        return item;
    }
}
