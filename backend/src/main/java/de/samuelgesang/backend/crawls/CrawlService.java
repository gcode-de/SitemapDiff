package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.url_chunk.UpdateUrlStatusDTO;
import de.samuelgesang.backend.url_chunk.UrlChunk;
import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import de.samuelgesang.backend.url_chunk.UrlObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {

    private final CrawlRepository crawlRepository;
    private final UrlChunkRepository urlChunkRepository;

    public void deleteCrawlsBySiteId(String siteId) {
        List<Crawl> crawls = crawlRepository.findBySiteId(siteId);
        for (Crawl crawl : crawls) {
            urlChunkRepository.deleteByCrawlId(crawl.getId());
            crawlRepository.delete(crawl);
        }
    }

    public Crawl updateUrlCheckedStatus(String crawlId, UpdateUrlStatusDTO updateUrlStatusDTO) {
        log.info("Updating URL checked status for crawlId: {} with DTO: {}", crawlId, updateUrlStatusDTO);

        Crawl crawl = crawlRepository.findById(crawlId).orElseThrow(() -> new IllegalArgumentException("Invalid crawl ID"));
        log.info("Crawl found: {}", crawl);

        boolean updated = false;
        for (CrawlDiffItem diffItem : crawl.getDiffToPrevCrawl()) {
            if (diffItem.getUrl().equals(updateUrlStatusDTO.getUrl())) {
                diffItem.setChecked(updateUrlStatusDTO.isChecked());
                log.info("Updated URL status in diffToPrevCrawl: {}", diffItem);
                updated = true;
                break;
            }
        }

        if (updated) {
            Crawl savedCrawl = crawlRepository.save(crawl);
            log.info("Crawl updated and saved: {}", savedCrawl);
            return savedCrawl;
        } else {
            log.warn("URL not found in diffToPrevCrawl: {}", updateUrlStatusDTO.getUrl());
            return null;
        }
    }
}
