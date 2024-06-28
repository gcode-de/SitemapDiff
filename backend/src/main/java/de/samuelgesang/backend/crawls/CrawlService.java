package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrawlService {

    private final CrawlRepository crawlRepository;
    private final UrlChunkRepository urlChunkRepository;

    @Autowired
    public CrawlService(CrawlRepository crawlRepository, UrlChunkRepository urlChunkRepository) {
        this.crawlRepository = crawlRepository;
        this.urlChunkRepository = urlChunkRepository;
    }

    public void deleteCrawlsBySiteId(String siteId) {
        List<Crawl> crawls = crawlRepository.findBySiteId(siteId);
        for (Crawl crawl : crawls) {
            urlChunkRepository.deleteByCrawlId(crawl.getId());
            crawlRepository.delete(crawl);
        }
    }
}
