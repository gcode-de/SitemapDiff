package de.samuelgesang.backend.sitemaps;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlDiffItem;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.sites.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SitemapService {

    @Autowired
    private CrawlRepository crawlRepository;

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
        throw new Exception("No valid sitemap found for URL: " + baseURL);
    }

    private boolean isValidSitemap(String sitemapUrl) {
        try {
            String content = fetchContentFromURL(new URL(sitemapUrl));
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
            throw new Exception("Failed to fetch content from URL: " + url + ". Response code: " + responseCode);
        }

        Scanner scanner = new Scanner(url.openStream());
        StringBuilder content = new StringBuilder();
        while (scanner.hasNext()) {
            content.append(scanner.nextLine());
        }
        scanner.close();

        return content.toString();
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

        List<String> currentUrls = new ArrayList<>();
        String sitemapUrl = site.getSitemap();
        System.out.println("Starting to fetch URLs from sitemap for site: " + sitemapUrl);
        fetchUrlsFromSitemap(sitemapUrl, currentUrls);
        System.out.println("Finished fetching URLs. Total URLs found: " + currentUrls.size());

        // Set prevCrawlId if there was a previous crawl
        List<String> crawlIds = site.getCrawlIds();
        if (!crawlIds.isEmpty()) {
            String prevCrawlId = crawlIds.get(crawlIds.size() - 1);
            crawl.setPrevCrawlId(prevCrawlId);

            // Fetch previous crawl
            Crawl prevCrawl = crawlRepository.findById(prevCrawlId).orElse(null);
            if (prevCrawl != null) {
                List<CrawlDiffItem> diffToPrevCrawl = calculateDiff(currentUrls, prevCrawl.getDiffToPrevCrawl().stream()
                        .map(CrawlDiffItem::getUrl).collect(Collectors.toList()));
                System.out.println("diffToPrevCrawl: " + diffToPrevCrawl);
                crawl.setDiffToPrevCrawl(diffToPrevCrawl);
            }
        }

        // Set finishedAt with the current timestamp in Zulu format
        crawl.setFinishedAt(Instant.now().toString());

        return crawl;
    }

    private List<CrawlDiffItem> calculateDiff(List<String> currentUrls, List<String> previousUrls) {
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

    private void fetchUrlsFromSitemap(String sitemapUrl, List<String> urls) throws Exception {
        System.out.println("Fetching content from URL: " + sitemapUrl);
        URL url = new URL(sitemapUrl);
        String content = fetchContentFromURL(url);
        System.out.println("Content fetched from URL: " + sitemapUrl);

        if (!isXML(content)) {
            throw new Exception("Invalid sitemap URL: The URL returns an HTML document instead of an XML.");
        }

        // Extract URLs from the XML content
        extractUrlsFromSitemap(content, urls);
    }

    private void extractUrlsFromSitemap(String content, List<String> urls) {
        System.out.println("Extracting URLs from sitemap content.");
        // Pattern to match <loc> tags
        Pattern locPattern = Pattern.compile("<loc>(.*?)</loc>");
        Matcher locMatcher = locPattern.matcher(content);
        while (locMatcher.find()) {
            String url = locMatcher.group(1).trim();
            urls.add(url);  // Add the extracted URL to the list
        }

        // Pattern to match nested <sitemap> tags and their <loc> tags
        Pattern sitemapPattern = Pattern.compile("<sitemap>.*?<loc>(.*?)</loc>.*?</sitemap>", Pattern.DOTALL);
        Matcher sitemapMatcher = sitemapPattern.matcher(content);
        while (sitemapMatcher.find()) {
            try {
                String nestedSitemapUrl = sitemapMatcher.group(1).trim();
                System.out.println("Found nested sitemap URL: " + nestedSitemapUrl);
                fetchUrlsFromSitemap(nestedSitemapUrl, urls);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
