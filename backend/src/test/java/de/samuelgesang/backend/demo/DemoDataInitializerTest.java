package de.samuelgesang.backend.demo;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DemoDataInitializerTest {

    private final SiteRepository siteRepository = mock(SiteRepository.class);
    private final CrawlRepository crawlRepository = mock(CrawlRepository.class);
    private final DemoDataInitializer initializer = new DemoDataInitializer(siteRepository, crawlRepository);

    @Test
    void seedsAnAnonymousSiteWithAVisibleDiff() {
        initializer.run(null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Crawl>> crawlsCaptor = ArgumentCaptor.forClass(List.class);
        verify(crawlRepository).saveAll(crawlsCaptor.capture());

        List<Crawl> crawls = crawlsCaptor.getValue();
        assertThat(crawls).hasSize(2);
        assertThat(crawls.get(1).getDiffToPrevCrawl())
                .extracting("action")
                .containsExactly("add", "add", "remove");

        ArgumentCaptor<Site> siteCaptor = ArgumentCaptor.forClass(Site.class);
        verify(siteRepository).save(siteCaptor.capture());
        assertThat(siteCaptor.getValue().getUserId()).isEqualTo(DemoDataInitializer.ANONYMOUS_USER_ID);
        assertThat(siteCaptor.getValue().getCrawlIds())
                .containsExactly("demo-crawl-initial", "demo-crawl-changed");
    }

    @Test
    void keepsExistingDemoDataUntouched() {
        when(siteRepository.existsById(DemoDataInitializer.DEMO_SITE_ID)).thenReturn(true);

        initializer.run(null);

        verifyNoInteractions(crawlRepository);
        verify(siteRepository, never()).save(any());
    }
}
