package de.samuelgesang.backend.sitemaps;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlDiffItem;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.exceptions.SitemapException;
import de.samuelgesang.backend.url_chunk.UrlChunk;
import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import de.samuelgesang.backend.url_chunk.UrlObject;
import de.samuelgesang.backend.sites.Site;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SitemapService {

    private static final int URL_CHUNK_SIZE = 1000;
    private final CrawlRepository crawlRepository;
    private final UrlChunkRepository urlChunkRepository;

    public SitemapService(CrawlRepository crawlRepository, UrlChunkRepository urlChunkRepository) {
        this.crawlRepository = crawlRepository;
        this.urlChunkRepository = urlChunkRepository;
    }

    public String findSitemapURL(String baseURL) throws Exception {
        String[] protocols = {"https://", "http://"};
        String[] subdomains = {"www.", ""};

        baseURL = removeTrailingSlash(baseURL);

        for (String protocol : protocols) {
            for (String subdomain : subdomains) {
                String sitemapUrl = protocol + subdomain + removeProtocol(baseURL) + "/sitemap.xml";
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
            return false;
        }
    }

    private String fetchContentFromURL(URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/xml");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new SitemapException("Failed to fetch content from URL: " + url + ". Response code: " + responseCode);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }

    private boolean isXML(String content) {
        return content.trim().startsWith("<");
    }

    private String removeProtocol(String urlString) {
        if (urlString.startsWith("http://")) {
            return urlString.substring(7);
        } else if (urlString.startsWith("https://")) {
            return urlString.substring(8);
        } else {
            return urlString;
        }
    }

    private String removeTrailingSlash(String urlString) {
        if (urlString.endsWith("/")) {
            return urlString.substring(0, urlString.length() - 1);
        } else {
            return urlString;
        }
    }

    public Crawl crawlSite(Site site) throws Exception {
        Crawl crawl = new Crawl();
        crawl.setSiteId(site.getId());

        List<UrlObject> urls = new ArrayList<>();
        String sitemapUrl = site.getSitemap();
        fetchUrlsFromSitemap(sitemapUrl, urls);

        // Split URLs into chunks and save them
        List<String> urlChunkIds = saveUrlChunks(urls, crawl.getId());
        crawl.setUrlChunkIds(urlChunkIds);

        // Set prevCrawlId if there was a previous crawl
        List<String> crawlIds = site.getCrawlIds();
        if (!crawlIds.isEmpty()) {
            String prevCrawlId = crawlIds.get(crawlIds.size() - 1);
            crawl.setPrevCrawlId(prevCrawlId);

            // Fetch previous crawl
            Crawl prevCrawl = crawlRepository.findById(prevCrawlId).orElse(null);
            if (prevCrawl != null) {
                List<UrlObject> prevUrls = loadUrlsFromChunks(prevCrawl.getUrlChunkIds());
                List<CrawlDiffItem> diffToPrevCrawl = calculateDiff(urls, prevUrls);
                log.info("Site: " + site.getName() + " - diffToPrevCrawl: " + diffToPrevCrawl);
                crawl.setDiffToPrevCrawl(diffToPrevCrawl);
            }
        }

        // Set finishedAt with the current timestamp in Zulu format
        crawl.setFinishedAt(Instant.now().toString());

        return crawl;
    }

    private List<String> saveUrlChunks(List<UrlObject> urls, String crawlId) {
        List<String> urlChunkIds = new ArrayList<>();
        for (int i = 0; i < urls.size(); i += URL_CHUNK_SIZE) {
            List<UrlObject> chunk = urls.subList(i, Math.min(i + URL_CHUNK_SIZE, urls.size()));
            UrlChunk urlChunk = new UrlChunk();
            urlChunk.setCrawlId(crawlId);
            urlChunk.setUrls(chunk);
            UrlChunk savedChunk = urlChunkRepository.save(urlChunk);
            urlChunkIds.add(savedChunk.getId());
        }
        return urlChunkIds;
    }

    private List<UrlObject> loadUrlsFromChunks(List<String> urlChunkIds) {
        List<UrlObject> urls = new ArrayList<>();
        for (String chunkId : urlChunkIds) {
            Optional<UrlChunk> chunk = urlChunkRepository.findById(chunkId);
            chunk.ifPresent(urlChunk -> urls.addAll(urlChunk.getUrls()));
        }
        return urls;
    }

    private List<CrawlDiffItem> calculateDiff(List<UrlObject> currentUrls, List<UrlObject> previousUrls) {
        Set<String> currentUrlSet = new HashSet<>();
        Set<String> previousUrlSet = new HashSet<>();

        for (UrlObject urlObject : currentUrls) {
            currentUrlSet.add(urlObject.getUrl());
        }
        for (UrlObject urlObject : previousUrls) {
            previousUrlSet.add(urlObject.getUrl());
        }

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

    private void fetchUrlsFromSitemap(String sitemapUrl, List<UrlObject> urls) throws Exception {
        URL url = new URI(sitemapUrl).toURL();
        String content = fetchContentFromURL(url);

        if (!isXML(content)) {
            throw new SitemapException("Invalid sitemap URL: The URL returns an HTML document instead of an XML.");
        }

        extractUrlsFromSitemap(content, urls);
    }

    private void extractUrlsFromSitemap(String content, List<UrlObject> urls) {
        // Pattern to match <loc> tags
        Pattern locPattern = Pattern.compile("<loc>(.*?)</loc>");
        Matcher locMatcher = locPattern.matcher(content);
        while (locMatcher.find()) {
            String url = locMatcher.group(1).trim();
            urls.add(new UrlObject(url, false));
        }

        // Pattern to match nested <sitemap> tags and their <loc> tags
        Pattern sitemapPattern = Pattern.compile("<sitemap>.*?<loc>(.*?)</loc>.*?</sitemap>", Pattern.DOTALL);
        Matcher sitemapMatcher = sitemapPattern.matcher(content);
        while (sitemapMatcher.find()) {
            try {
                String nestedSitemapUrl = sitemapMatcher.group(1).trim();
                log.info("Found nested sitemap URL: " + nestedSitemapUrl);
                fetchUrlsFromSitemap(nestedSitemapUrl, urls);
            } catch (Exception e) {
                log.error("Error fetching nested sitemap: " + sitemapMatcher.group(1).trim(), e);
            }
        }
    }
}
