package de.samuelgesang.backend.sitemaps;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.sites.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SitemapService {

    @Autowired
    private CrawlRepository crawlRepository;

    public String findSitemapURL(String baseURL) throws Exception {
        String[] protocols = {"https://", "http://"};
        String[] subdomains = {"www.", ""};

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

    public Crawl crawlSite(Site site) throws Exception {
        Crawl crawl = new Crawl();
        crawl.setSiteId(site.getId());

        List<String> urls = new ArrayList<>();
        String sitemapUrl = site.getSitemap();
        System.out.println("Starting to fetch URLs from sitemap for site: " + sitemapUrl);
        fetchUrlsFromSitemap(sitemapUrl, urls);
        System.out.println("Finished fetching URLs. Total URLs found: " + urls.size());

        crawl.setUrls(urls);

        // Set prevCrawlId if there was a previous crawl
        List<String> crawlIds = site.getCrawlIds();
        if (!crawlIds.isEmpty()) {
            crawl.setPrevCrawlId(crawlIds.getLast());
        }

        // Set finishedAt with the current timestamp in Zulu format
        crawl.setFinishedAt(Instant.now().toString());

        return crawl;
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
            System.out.println("Found URL: " + url);  // Debug output to check extracted URLs
        }

        // Pattern to match nested <sitemap> tags and their <loc> tags
        Pattern sitemapPattern = Pattern.compile("<sitemap>.*?<loc>(.*?)</loc>.*?</sitemap>", Pattern.DOTALL);
        Matcher sitemapMatcher = sitemapPattern.matcher(content);
        while (sitemapMatcher.find()) {
            try {
                String nestedSitemapUrl = sitemapMatcher.group(1).trim();
                System.out.println("Found nested sitemap URL: " + nestedSitemapUrl);  // Debug output to check nested sitemap URLs
                fetchUrlsFromSitemap(nestedSitemapUrl, urls);
            } catch (Exception e) {
                e.printStackTrace();  // Handle the exception or log it
            }
        }
    }
}
