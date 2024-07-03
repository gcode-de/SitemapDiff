package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.sitemaps.SitemapService;
import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteRepository;
import de.samuelgesang.backend.url_chunk.UpdateUrlStatusDTO;
import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {

    private final CrawlRepository crawlRepository;
    private final UrlChunkRepository urlChunkRepository;
    private final SitemapService sitemapService;
    private final SiteRepository siteRepository;

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

    public void deleteCrawl(String crawlId, String userId) {
        Optional<Crawl> optionalCrawl = crawlRepository.findById(crawlId);
        if (optionalCrawl.isEmpty()) {
            throw new RuntimeException("Crawl not found");
        }

        Crawl crawl = optionalCrawl.get();
        Site site = siteRepository.findById(crawl.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site not found"));

        if (!site.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Remove URL chunks
        urlChunkRepository.deleteAllByCrawlId(crawlId);

        // Update the crawl list of the site
        List<String> crawlIds = site.getCrawlIds();
        int index = crawlIds.indexOf(crawlId);
        crawlIds.remove(crawlId);

        if (index > 0 && index < crawlIds.size()) {
            String prevCrawlId = crawlIds.get(index - 1);
            String nextCrawlId = crawlIds.get(index);

            Crawl nextCrawl = crawlRepository.findById(nextCrawlId).orElseThrow(() -> new RuntimeException("Next crawl not found"));
            nextCrawl.setPrevCrawlId(prevCrawlId);

            // Recalculate diffToPrevCrawl
            List<String> prevUrls = sitemapService.loadUrlsFromChunks(crawlRepository.findById(prevCrawlId)
                    .orElseThrow(() -> new RuntimeException("Previous crawl not found"))
                    .getUrlChunkIds());

            List<String> nextUrls = sitemapService.loadUrlsFromChunks(nextCrawl.getUrlChunkIds());
            nextCrawl.setDiffToPrevCrawl(sitemapService.calculateDiff(nextUrls, prevUrls));

            crawlRepository.save(nextCrawl);
        }

        if (index == 0 && !crawlIds.isEmpty()) {
            String nextCrawlId = crawlIds.getFirst();

            Crawl nextCrawl = crawlRepository.findById(nextCrawlId).orElseThrow(() -> new RuntimeException("Next crawl not found"));
            nextCrawl.setPrevCrawlId(null);
            nextCrawl.setDiffToPrevCrawl(new ArrayList<>());

            crawlRepository.save(nextCrawl);
        }

        site.setCrawlIds(crawlIds);
        siteRepository.save(site);

        crawlRepository.deleteById(crawlId);
    }
}
