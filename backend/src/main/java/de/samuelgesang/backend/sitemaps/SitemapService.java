package de.samuelgesang.backend.sitemaps;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Service
public class SitemapService {

    public String[] findSitemaps(String baseURL) throws Exception {
        List<String> sitemaps = new ArrayList<>();
        try {
            URL url = createURLWithProtocol(baseURL + "/sitemap.xml");
            String content = fetchContentFromURL(url);

            if (!isXML(content)) {
                throw new Exception("Invalid sitemap URL: The URL returns an HTML document instead of an XML.");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(content)));

            NodeList sitemapNodes = document.getElementsByTagName("sitemap");
            if (sitemapNodes.getLength() == 0) {
                // No additional sitemaps, return the main sitemap URL
                sitemaps.add(url.toString());
            } else {
                // Multiple sitemaps, extract their URLs
                for (int i = 0; i < sitemapNodes.getLength(); i++) {
                    String sitemapUrl = sitemapNodes.item(i).getTextContent().trim();
                    sitemaps.add(sitemapUrl);
                }
            }
        } catch (Exception e) {
            throw new Exception("Failed to find sitemaps. Please enter manually.", e);
        }
        return sitemaps.toArray(new String[0]);
    }

    private URL createURLWithProtocol(String urlString) throws Exception {
        try {
            return new URI("https://" + removeProtocol(urlString)).toURL();
        } catch (Exception e) {
            return new URI("http://" + removeProtocol(urlString)).toURL();
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
