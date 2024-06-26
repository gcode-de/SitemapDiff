package de.samuelgesang.backend.sitemaps;

import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class SitemapService {

    private static final String[] PROTOCOLS = {"https://", "http://"};
    private static final String[] SUBDOMAINS = {"www.", ""};

    public String findSitemapURL(String baseURL) throws Exception {
        for (String protocol : PROTOCOLS) {
            for (String subdomain : SUBDOMAINS) {
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
}
