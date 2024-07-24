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

            // Split URLs into chunks and save them
            List<String> urlChunkIds = saveUrlChunks(urls, crawl.getId());
            crawl.setUrlChunkIds(urlChunkIds);

            // Set prevCrawlId if there was a previous crawl
            List<String> crawlIds = site.getCrawlIds();
            if (crawlIds == null) {
                crawlIds = new ArrayList<>();
            }
            if (!crawlIds.isEmpty()) {
                String prevCrawlId = crawlIds.getLast();
                crawl.setPrevCrawlId(prevCrawlId);

                // Fetch previous crawl
                Crawl prevCrawl = crawlRepository.findById(prevCrawlId).orElse(null);
                if (prevCrawl != null) {
                    List<String> prevUrls = loadUrlsFromChunks(prevCrawl.getUrlChunkIds());
                    List<CrawlDiffItem> diffToPrevCrawl = calculateDiff(urls, prevUrls);
                    logger.info("Site: {} - diffToPrevCrawl: {}", site.getName(), diffToPrevCrawl);
                    crawl.setDiffToPrevCrawl(diffToPrevCrawl);
                }
            }

            // Set finishedAt with the current timestamp in Zulu format
            crawl.setFinishedAt(Instant.now().toString());

            return crawl;
        } catch (Exception e) {
            String errorMessage = "Error crawling site: " + site.getName();
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
        List<String> urls = new ArrayList<>();
        for (String chunkId : urlChunkIds) {
            Optional<UrlChunk> chunk = urlChunkRepository.findById(chunkId);
            chunk.ifPresent(urlChunk -> urls.addAll(urlChunk.getUrls()));
        }
        return urls;
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

    public void deleteCrawlsBySiteId(String siteId) {
        List<Crawl> crawls = crawlRepository.findBySiteId(siteId);
        for (Crawl crawl : crawls) {
            urlChunkRepository.deleteByCrawlId(crawl.getId());
            crawlRepository.delete(crawl);
        }
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

    public void deleteCrawl(String crawlId, String userId) {
        Optional<Crawl> optionalCrawl = crawlRepository.findById(crawlId);
        if (optionalCrawl.isEmpty()) {
            throw new ResourceNotFoundException("Crawl not found: " + crawlId);
        }

        Crawl crawl = optionalCrawl.get();
        Site site = siteRepository.findById(crawl.getSiteId())
                .orElseThrow(() -> new ResourceNotFoundException("Site not found: " + crawl.getSiteId()));

        if (!site.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized to delete crawl for site: " + crawl.getSiteId());
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

            Crawl nextCrawl = crawlRepository.findById(nextCrawlId)
                    .orElseThrow(() -> new ResourceNotFoundException("Next crawl not found: " + nextCrawlId));
            nextCrawl.setPrevCrawlId(prevCrawlId);

            // Recalculate diffToPrevCrawl
            List<String> prevUrls = loadUrlsFromChunks(crawlRepository.findById(prevCrawlId)
                    .orElseThrow(() -> new ResourceNotFoundException("Previous crawl not found: " + prevCrawlId))
                    .getUrlChunkIds());

            List<String> nextUrls = loadUrlsFromChunks(nextCrawl.getUrlChunkIds());
            nextCrawl.setDiffToPrevCrawl(calculateDiff(nextUrls, prevUrls));

            crawlRepository.save(nextCrawl);
        }

        if (index == 0 && !crawlIds.isEmpty()) {
            String nextCrawlId = crawlIds.getFirst();

            Crawl nextCrawl = crawlRepository.findById(nextCrawlId)
                    .orElseThrow(() -> new ResourceNotFoundException("Next crawl not found: " + nextCrawlId));
            nextCrawl.setPrevCrawlId(null);
            nextCrawl.setDiffToPrevCrawl(new ArrayList<>());

            crawlRepository.save(nextCrawl);
        }

        site.setCrawlIds(crawlIds);
        siteRepository.save(site);

        crawlRepository.deleteById(crawlId);
    }
}
