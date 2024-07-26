package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.exceptions.ResourceNotFoundException;
import de.samuelgesang.backend.exceptions.SitemapException;
import de.samuelgesang.backend.exceptions.UnauthorizedAccessException;
import de.samuelgesang.backend.sitemaps.SitemapService;
import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteRepository;
import de.samuelgesang.backend.url_chunk.UpdateUrlStatusDTO;
import de.samuelgesang.backend.url_chunk.UrlChunk;
import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlService.class);
    private static final int URL_CHUNK_SIZE = 1000;
    private final CrawlRepository crawlRepository;
    private final UrlChunkRepository urlChunkRepository;
    private final SitemapService sitemapService;
    private final SiteRepository siteRepository;

    public Crawl crawlSite(Site site) throws SitemapException {
        try {
            logger.info("Crawling site: {} with ID: {}", site.getName(), site.getId());
            Crawl crawl = new Crawl();
            crawl.setSiteId(site.getId());

            List<String> urls = new ArrayList<>();
            String sitemapUrl = site.getSitemap();
            logger.info("Fetching URLs from sitemap: {}", sitemapUrl);
            fetchUrlsFromSitemap(sitemapUrl, urls);
            logger.info("Total URLs fetched: {}", urls.size());

            List<String> crawlIds = site.getCrawlIds();
            if (crawlIds == null) {
                crawlIds = new ArrayList<>();
            }

            if (crawlIds.isEmpty()) {
                // First crawl, save URLs in chunks
                List<String> urlChunkIds = saveUrlChunks(urls, crawl.getId());
                crawl.setUrlChunkIds(urlChunkIds);
                crawl.setPrevCrawlId(null);
                crawl.setDiffToPrevCrawl(Collections.emptyList());
            } else {
                // Not the first crawl, compare with the previous crawls
                String firstCrawlId = crawlIds.get(0);
                Crawl firstCrawl = crawlRepository.findById(firstCrawlId)
                        .orElseThrow(() -> new ResourceNotFoundException("First crawl not found: " + firstCrawlId));
                List<String> reconstructedPrevUrls = loadUrlsFromChunks(firstCrawl.getUrlChunkIds());

                // Apply all previous diffs to reconstruct the previous state
                for (String id : crawlIds.subList(1, crawlIds.size())) {
                    Crawl previousCrawl = crawlRepository.findById(id).orElse(null);
                    if (previousCrawl != null && previousCrawl.getDiffToPrevCrawl() != null) {
                        applyDiff(reconstructedPrevUrls, previousCrawl.getDiffToPrevCrawl());
                    }
                }

                List<CrawlDiffItem> diffToPrevCrawl = calculateDiff(urls, reconstructedPrevUrls);
                logger.info("Site: {} - diffToPrevCrawl: {}", site.getName(), diffToPrevCrawl);
                crawl.setDiffToPrevCrawl(diffToPrevCrawl);

                // Do not save URLs in chunks for non-first crawls
                crawl.setUrlChunkIds(Collections.emptyList()); // Ensure urlChunkIds is initialized
                crawl.setPrevCrawlId(crawlIds.get(crawlIds.size() - 1));
            }

            // Set finishedAt with the current timestamp in Zulu format
            crawl.setFinishedAt(Instant.now().toString());

            crawlRepository.save(crawl);
            logger.info("Crawl saved with ID: {}", crawl.getId());

            crawlIds.add(crawl.getId());
            site.setCrawlIds(crawlIds);
            siteRepository.save(site);
            logger.info("Site updated with new crawl ID: {}", crawl.getId());

            return crawl;
        } catch (Exception e) {
            String errorMessage = "Error crawling site: " + site.getName();
            logger.error(errorMessage, e);
            throw new SitemapException(errorMessage, e);
        }
    }

    private List<String> saveUrlChunks(List<String> urls, String crawlId) {
        List<String> urlChunkIds = new ArrayList<>();
        for (int i = 0; i < urls.size(); i += URL_CHUNK_SIZE) {
            List<String> chunk = urls.subList(i, Math.min(i + URL_CHUNK_SIZE, urls.size()));
            UrlChunk urlChunk = new UrlChunk();
            urlChunk.setCrawlId(crawlId);
            urlChunk.setUrls(chunk);
            UrlChunk savedChunk = urlChunkRepository.save(urlChunk);
            urlChunkIds.add(savedChunk.getId());
        }
        return urlChunkIds;
    }

    public List<String> loadUrlsFromChunks(List<String> urlChunkIds) {
        if (urlChunkIds == null) {
            return new ArrayList<>();
        }
        List<String> urls = new ArrayList<>();
        for (String chunkId : urlChunkIds) {
            Optional<UrlChunk> chunk = urlChunkRepository.findById(chunkId);
            chunk.ifPresent(urlChunk -> urls.addAll(urlChunk.getUrls()));
        }
        return urls;
    }

    private void applyDiff(List<String> urls, List<CrawlDiffItem> diffToPrevCrawl) {
        for (CrawlDiffItem item : diffToPrevCrawl) {
            if ("add".equals(item.getAction())) {
                urls.add(item.getUrl());
            } else if ("remove".equals(item.getAction())) {
                urls.remove(item.getUrl());
            }
        }
    }

    public List<CrawlDiffItem> calculateDiff(List<String> currentUrls, List<String> previousUrls) {
        Set<String> currentUrlSet = new HashSet<>(currentUrls);
        Set<String> previousUrlSet = new HashSet<>(previousUrls);

        List<CrawlDiffItem> diff = new ArrayList<>();

        // URLs added in current crawl
        for (String url : currentUrlSet) {
            if (!previousUrlSet.contains(url)) {
                CrawlDiffItem item = new CrawlDiffItem();
                item.setAction("add");
                item.setUrl(url);
                item.setChecked(false);
                diff.add(item);
            }
        }

        // URLs removed in current crawl
        for (String url : previousUrlSet) {
            if (!currentUrlSet.contains(url)) {
                CrawlDiffItem item = new CrawlDiffItem();
                item.setAction("remove");
                item.setUrl(url);
                item.setChecked(false);
                diff.add(item);
            }
        }

        return diff;
    }

    private void fetchUrlsFromSitemap(String sitemapUrl, List<String> urls) throws SitemapException {
        try {
            logger.info("Fetching content from sitemap URL: {}", sitemapUrl);
            URL url = new URI(sitemapUrl).toURL();
            String content = sitemapService.fetchContentFromURL(url);

            if (!sitemapService.isXML(content)) {
                throw new SitemapException("Invalid sitemap URL: The URL returns an HTML document instead of an XML.");
            }

            extractUrlsFromSitemap(content, urls);
        } catch (Exception e) {
            String errorMessage = "Error fetching URLs from sitemap: " + sitemapUrl;
            throw new SitemapException(errorMessage, e);
        }
    }

    private void extractUrlsFromSitemap(String content, List<String> urls) throws SitemapException {
        try {
            logger.info("Extracting URLs from sitemap content.");
            // Pattern to match <loc> tags
            Pattern locPattern = Pattern.compile("<loc>(.*?)</loc>");
            Matcher locMatcher = locPattern.matcher(content);
            while (locMatcher.find()) {
                String url = locMatcher.group(1).trim();
                urls.add(url);
            }

            // Pattern to match nested <sitemap> tags and their <loc> tags
            Pattern sitemapPattern = Pattern.compile("<sitemap>.*?<loc>(.*?)</loc>.*?</sitemap>", Pattern.DOTALL);
            Matcher sitemapMatcher = sitemapPattern.matcher(content);
            while (sitemapMatcher.find()) {
                String nestedSitemapUrl = sitemapMatcher.group(1).trim();
                fetchNestedSitemapUrls(nestedSitemapUrl, urls);
            }
        } catch (Exception e) {
            String errorMessage = "Error extracting URLs from sitemap content.";
            throw new SitemapException(errorMessage, e);
        }
    }

    private void fetchNestedSitemapUrls(String nestedSitemapUrl, List<String> urls) throws SitemapException {
        try {
            fetchUrlsFromSitemap(nestedSitemapUrl, urls);
        } catch (Exception e) {
            String errorMessage = "Error fetching nested sitemap: " + nestedSitemapUrl;
            throw new SitemapException(errorMessage, e);
        }
    }

    public void deleteCrawl(String crawlId, String userId) {
        // 1. Find the Crawl to Delete
        Optional<Crawl> optionalCrawl = crawlRepository.findById(crawlId);
        if (optionalCrawl.isEmpty()) {
            throw new ResourceNotFoundException("Crawl not found: " + crawlId);
        }

        Crawl crawl = optionalCrawl.get();
        // 2. Find the Associated Site
        Site site = siteRepository.findById(crawl.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found: " + crawl.getSiteId()));

        // 3. Check Authorization
        if (!site.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized to delete crawl for site: " + crawl.getSiteId());
        }

        // 4. Remove URL Chunks
        urlChunkRepository.deleteAllByCrawlId(crawlId);

        // 5. Update the Crawl List of the Site
        List<String> crawlIds = site.getCrawlIds();
        int index = crawlIds.indexOf(crawlId);
        crawlIds.remove(crawlId);

        if (index == 0 && !crawlIds.isEmpty()) {
            // 6. Handle Deletion of the First Crawl
            String nextCrawlId = crawlIds.get(0);
            Crawl nextCrawl = crawlRepository.findById(nextCrawlId)
                    .orElseThrow(() -> new ResourceNotFoundException("Next crawl not found: " + nextCrawlId));

            nextCrawl.setPrevCrawlId(null);

            List<String> urls = loadUrlsFromChunks(crawl.getUrlChunkIds());
            applyDiff(urls, nextCrawl.getDiffToPrevCrawl());

            List<String> urlChunkIds = saveUrlChunks(urls, nextCrawl.getId());
            nextCrawl.setUrlChunkIds(urlChunkIds);
            nextCrawl.setDiffToPrevCrawl(new ArrayList<>());

            crawlRepository.save(nextCrawl);
        } else if (index > 0 && index < crawlIds.size()) {
            // 7. Handle Deletion of a Middle Crawl
            String prevCrawlId = crawlIds.get(index - 1);
            String nextCrawlId = crawlIds.get(index);

            Crawl nextCrawl = crawlRepository.findById(nextCrawlId)
                    .orElseThrow(() -> new ResourceNotFoundException("Next crawl not found: " + nextCrawlId));
            nextCrawl.setPrevCrawlId(prevCrawlId);

            // Merge diffs while removing contradictory entries
            Map<String, CrawlDiffItem> diffMap = new HashMap<>();
            for (CrawlDiffItem item : crawl.getDiffToPrevCrawl()) {
                diffMap.put(item.getUrl(), item);
            }
            for (CrawlDiffItem item : nextCrawl.getDiffToPrevCrawl()) {
                if (diffMap.containsKey(item.getUrl())) {
                    CrawlDiffItem existingItem = diffMap.get(item.getUrl());
                    if (!existingItem.getAction().equals(item.getAction())) {
                        diffMap.remove(item.getUrl()); // Remove contradictory entries
                    } else {
                        // Combine checked status
                        existingItem.setChecked(existingItem.isChecked() || item.isChecked());
                    }
                } else {
                    diffMap.put(item.getUrl(), item);
                }
            }
            List<CrawlDiffItem> combinedDiff = new ArrayList<>(diffMap.values());
            nextCrawl.setDiffToPrevCrawl(combinedDiff);

            crawlRepository.save(nextCrawl);
        }

        // 8. Update the Site's Crawl IDs
        site.setCrawlIds(crawlIds);
        siteRepository.save(site);

        // 9. Delete the Crawl
        crawlRepository.deleteById(crawlId);
    }


    public Crawl updateUrlCheckedStatus(String crawlId, UpdateUrlStatusDTO updateUrlStatusDTO) {
        log.info("Updating URL checked status for crawlId: {} with DTO: {}", crawlId, updateUrlStatusDTO);

        Crawl crawl = crawlRepository.findById(crawlId)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid crawl ID: " + crawlId));
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

    public void deleteCrawlsBySiteId(String siteId) {
        List<Crawl> crawls = crawlRepository.findBySiteId(siteId);
        for (Crawl crawl : crawls) {
            urlChunkRepository.deleteByCrawlId(crawl.getId());
            crawlRepository.delete(crawl);
        }
    }

}
