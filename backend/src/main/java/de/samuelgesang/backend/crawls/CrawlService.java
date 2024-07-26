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
            Crawl crawl = initializeCrawl(site);

            List<String> urls = fetchUrls(site.getSitemap());
            logger.info("Total URLs fetched: {}", urls.size());

            List<String> crawlIds = site.getCrawlIds();
            if (crawlIds == null) {
                crawlIds = new ArrayList<>();
            }

            if (crawlIds.isEmpty()) {
                handleFirstCrawl(crawl, urls);
            } else {
                handleSubsequentCrawl(site, crawl, urls, crawlIds);
            }

            finalizeCrawl(crawl);
            saveCrawlAndSite(crawl, site, crawlIds);

            return crawl;
        } catch (ResourceNotFoundException e) {
            String errorMessage = "Resource not found error while crawling site: " + site.getName();
            logger.error(errorMessage, e);
            throw new SitemapException(errorMessage, e);
        } catch (SitemapException e) {
            String errorMessage = "Sitemap error while crawling site: " + site.getName();
            logger.error(errorMessage, e);
            throw e;
        } catch (Exception e) {
            String errorMessage = "Unexpected error while crawling site: " + site.getName();
            logger.error(errorMessage, e);
            throw new SitemapException(errorMessage, e);
        }
    }

    private Crawl initializeCrawl(Site site) {
        Crawl crawl = new Crawl();
        crawl.setSiteId(site.getId());
        return crawl;
    }

    private List<String> fetchUrls(String sitemapUrl) throws SitemapException {
        List<String> urls = new ArrayList<>();
        logger.info("Fetching URLs from sitemap: {}", sitemapUrl);
        fetchUrlsFromSitemap(sitemapUrl, urls);
        return urls;
    }

    private void handleFirstCrawl(Crawl crawl, List<String> urls) {
        List<String> urlChunkIds = saveUrlChunks(urls, crawl.getId());
        crawl.setUrlChunkIds(urlChunkIds);
        crawl.setPrevCrawlId(null);
        crawl.setDiffToPrevCrawl(Collections.emptyList());
    }

    private void handleSubsequentCrawl(Site site, Crawl crawl, List<String> urls, List<String> crawlIds) throws ResourceNotFoundException {
        String firstCrawlId = crawlIds.getFirst();
        Crawl firstCrawl = findCrawlById(firstCrawlId);
        List<String> reconstructedPrevUrls = loadUrlsFromChunks(firstCrawl.getUrlChunkIds());

        applyPreviousDiffs(reconstructedPrevUrls, crawlIds);
        List<CrawlDiffItem> diffToPrevCrawl = calculateDiff(urls, reconstructedPrevUrls);
        logger.info("Site: {} - diffToPrevCrawl: {}", site.getName(), diffToPrevCrawl);
        crawl.setDiffToPrevCrawl(diffToPrevCrawl);

        crawl.setUrlChunkIds(Collections.emptyList());
        crawl.setPrevCrawlId(crawlIds.getLast());
    }

    private void applyPreviousDiffs(List<String> reconstructedPrevUrls, List<String> crawlIds) {
        for (String id : crawlIds.subList(1, crawlIds.size())) {
            Crawl previousCrawl = crawlRepository.findById(id).orElse(null);
            if (previousCrawl != null && previousCrawl.getDiffToPrevCrawl() != null) {
                applyDiff(reconstructedPrevUrls, previousCrawl.getDiffToPrevCrawl());
            }
        }
    }

    private void finalizeCrawl(Crawl crawl) {
        crawl.setFinishedAt(Instant.now().toString());
    }

    private void saveCrawlAndSite(Crawl crawl, Site site, List<String> crawlIds) {
        crawlRepository.save(crawl);
        logger.info("Crawl saved with ID: {}", crawl.getId());

        crawlIds.add(crawl.getId());
        site.setCrawlIds(crawlIds);
        siteRepository.save(site);
        logger.info("Site updated with new crawl ID: {}", crawl.getId());
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
        Crawl crawl = findCrawlById(crawlId);
        Site site = findSiteById(crawl.getSiteId());
        authorizeUser(site, userId);
        removeUrlChunks(crawlId);
        updateCrawlList(site, crawl, crawlId);
        crawlRepository.deleteById(crawlId);
    }

    private Crawl findCrawlById(String crawlId) {
        return crawlRepository.findById(crawlId)
                .orElseThrow(() -> new ResourceNotFoundException("Crawl not found: " + crawlId));
    }

    private Site findSiteById(String siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found: " + siteId));
    }

    private void authorizeUser(Site site, String userId) {
        if (!site.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized to delete crawl for site: " + site.getId());
        }
    }

    private void removeUrlChunks(String crawlId) {
        urlChunkRepository.deleteAllByCrawlId(crawlId);
    }

    private void updateCrawlList(Site site, Crawl crawl, String crawlId) {
        List<String> crawlIds = site.getCrawlIds();
        int index = crawlIds.indexOf(crawlId);
        crawlIds.remove(crawlId);

        if (index == 0 && !crawlIds.isEmpty()) {
            handleFirstCrawlDeletion(crawl, crawlIds);
        } else if (index > 0 && index < crawlIds.size()) {
            handleMiddleCrawlDeletion(crawl, crawlIds, index);
        }

        site.setCrawlIds(crawlIds);
        siteRepository.save(site);
    }

    private void handleFirstCrawlDeletion(Crawl crawl, List<String> crawlIds) {
        String nextCrawlId = crawlIds.getFirst();
        Crawl nextCrawl = findCrawlById(nextCrawlId);

        nextCrawl.setPrevCrawlId(null);
        List<String> urls = loadUrlsFromChunks(crawl.getUrlChunkIds());
        applyDiff(urls, nextCrawl.getDiffToPrevCrawl());
        List<String> urlChunkIds = saveUrlChunks(urls, nextCrawl.getId());
        nextCrawl.setUrlChunkIds(urlChunkIds);
        nextCrawl.setDiffToPrevCrawl(new ArrayList<>());
        crawlRepository.save(nextCrawl);
    }

    private void handleMiddleCrawlDeletion(Crawl crawl, List<String> crawlIds, int index) {
        String prevCrawlId = crawlIds.get(index - 1);
        String nextCrawlId = crawlIds.get(index);

        Crawl nextCrawl = findCrawlById(nextCrawlId);
        nextCrawl.setPrevCrawlId(prevCrawlId);

        Map<String, CrawlDiffItem> diffMap = mergeDiffs(crawl.getDiffToPrevCrawl(), nextCrawl.getDiffToPrevCrawl());
        List<CrawlDiffItem> combinedDiff = new ArrayList<>(diffMap.values());
        nextCrawl.setDiffToPrevCrawl(combinedDiff);

        crawlRepository.save(nextCrawl);
    }

    private Map<String, CrawlDiffItem> mergeDiffs(List<CrawlDiffItem> currentDiff, List<CrawlDiffItem> nextDiff) {
        Map<String, CrawlDiffItem> diffMap = new HashMap<>();
        for (CrawlDiffItem item : currentDiff) {
            diffMap.put(item.getUrl(), item);
        }
        for (CrawlDiffItem item : nextDiff) {
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
        return diffMap;
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
