package de.samuelgesang.backend.sitemaps;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class SitemapService {

    public String[] findSitemaps(String baseURL) {
        List<String> sitemaps = new ArrayList<>();
        try {
            URL url = new URL(baseURL + "/sitemap.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(url.openStream());

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
            e.printStackTrace();
        }
        return sitemaps.toArray(new String[0]);
    }
}
