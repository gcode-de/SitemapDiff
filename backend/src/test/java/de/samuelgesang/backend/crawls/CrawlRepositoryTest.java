package de.samuelgesang.backend.crawls;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ExtendWith(SpringExtension.class)
class CrawlRepositoryTest {

    @Autowired
    private CrawlRepository crawlRepository;

    @Test
    void testSaveCrawl() {
        Crawl crawl = new Crawl();
        crawl.setId("testId");
        crawl.setSiteId("testSiteId");
        crawlRepository.save(crawl);

        Crawl foundCrawl = crawlRepository.findById("testId").orElse(null);
        assertThat(foundCrawl).isNotNull();
        assertThat(foundCrawl.getSiteId()).isEqualTo("testSiteId");
    }

    @Test
    void testFindBySiteId() {
        Crawl crawl = new Crawl();
        crawl.setId("testId");
        crawl.setSiteId("testSiteId");
        crawlRepository.save(crawl);

        List<Crawl> foundCrawls = crawlRepository.findBySiteId("testSiteId");
        assertThat(foundCrawls).isNotEmpty();
        assertThat(foundCrawls.getFirst().getId()).isEqualTo("testId");
    }
}
