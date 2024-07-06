package de.samuelgesang.backend.sitemaps;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlDiffItem;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.exceptions.SitemapException;
import de.samuelgesang.backend.url_chunk.UrlChunk;
import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import de.samuelgesang.backend.sites.Site;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Service
public class SitemapService {

    private static final Logger logger = LoggerFactory.getLogger(SitemapService.class);
    private static final int URL_CHUNK_SIZE = 1000;

    private final CrawlRepository crawlRepository;
    private final UrlChunkRepository urlChunkRepository;

    public SitemapService(CrawlRepository crawlRepository, UrlChunkRepository urlChunkRepository) {
        this.crawlRepository = crawlRepository;
        this.urlChunkRepository = urlChunkRepository;
    }

    public String findSitemapURL(String baseURL) throws SitemapException {
        String[] protocols = {"https://", "http://"};
        String[] subdomains = {"www.", ""};

        baseURL = removeTrailingSlash(baseURL);

        for (String protocol : protocols) {
            for (String subdomain : subdomains) {
                String sitemapUrl = protocol + subdomain + removeProtocolAndSubdomain(baseURL) + "/sitemap.xml";
                if (isValidSitemap(sitemapUrl)) {
                    return sitemapUrl;
                }
            }
        }
        throw new SitemapException("No valid sitemap found for URL: " + baseURL);
    }

    private boolean isValidSitemap(String sitemapUrl) {
        try {
            String content = fetchContentFromURL(new URI(sitemapUrl).toURL());
            return isXML(content);
        } catch (Exception e) {
            logger.error("Error validating sitemap URL: {}", sitemapUrl, e);
            return false;
        }
    }

    private String fetchContentFromURL(URL url) throws SitemapException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/xml");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                throw new SitemapException("Failed to fetch content from URL: " + url + ". Response code: " + responseCode);
            }

            if (url.getPath().endsWith(".gz")) {
                try (GZIPInputStream gzipInputStream = new GZIPInputStream(url.openStream());
                     BufferedReader reader = new BufferedReader(new InputStreamReader(gzipInputStream))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    return content.toString();
                }
            } else {
                try (Scanner scanner = new Scanner(url.openStream())) {
                    StringBuilder content = new StringBuilder();
                    while (scanner.hasNext()) {
                        content.append(scanner.nextLine());
                    }
                    return content.toString();
                }
            }
        } catch (Exception e) {
            String errorMessage = "Error fetching content from URL: " + url;
            throw new SitemapException(errorMessage, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isXML(String content) {
        return content.trim().startsWith("<");
    }

    private String removeProtocolAndSubdomain(String urlString) {
        if (urlString.startsWith("http://")) {
            urlString = urlString.substring(7);
        } else if (urlString.startsWith("https://")) {
            urlString = urlString.substring(8);
        }

        if (urlString.startsWith("www.")) {
            urlString = urlString.substring(4);
        }

        return urlString;
    }


    private String removeTrailingSlash(String urlString) {
        if (urlString.endsWith("/")) {
            return urlString.substring(0, urlString.length() - 1);
        } else {
            return urlString;
        }
    }

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
            String content = fetchContentFromURL(url);

            if (!isXML(content)) {
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

}
