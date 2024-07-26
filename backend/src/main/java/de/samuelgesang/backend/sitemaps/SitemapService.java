package de.samuelgesang.backend.sitemaps;

import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.exceptions.SitemapException;
import de.samuelgesang.backend.url_chunk.UrlChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Service
public class SitemapService {

    private static final Logger logger = LoggerFactory.getLogger(SitemapService.class);

    public SitemapService(CrawlRepository crawlRepository, UrlChunkRepository urlChunkRepository) {
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

    public String fetchContentFromURL(URL url) throws SitemapException {
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

    public boolean isXML(String content) {
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


}
