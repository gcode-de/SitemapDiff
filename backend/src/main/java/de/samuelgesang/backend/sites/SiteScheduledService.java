package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.exceptions.SitemapException;
import de.samuelgesang.backend.sitemaps.SitemapService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SiteScheduledService {

    private final SiteService siteService;
    private final JavaMailSender mailSender;
    private final SitemapService sitemapService;

    public SiteScheduledService(SiteService siteService, JavaMailSender mailSender, SitemapService sitemapService) {
        this.siteService = siteService;
        this.mailSender = mailSender;
        this.sitemapService = sitemapService;
    }

    @Scheduled(cron = "0 0 0 * * *") // Runs every day at midnight
    public void scheduleDailyCrawls() {
        List<Site> sites = siteService.getAllSitesWithSchedule("daily");
        for (Site site : sites) {
            performCrawl(site);
        }
    }

    @Scheduled(cron = "0 0 0 * * MON") // Runs every Monday at midnight
    public void scheduleWeeklyCrawls() {
        List<Site> sites = siteService.getAllSitesWithSchedule("weekly");
        for (Site site : sites) {
            performCrawl(site);
        }
    }

    @Scheduled(cron = "0 0 0 1 * *") // Runs on the 1st day of every month at midnight
    public void scheduleMonthlyCrawls() {
        List<Site> sites = siteService.getAllSitesWithSchedule("monthly");
        for (Site site : sites) {
            performCrawl(site);
        }
    }

    private void performCrawl(Site site) {
        try {
            Crawl crawl = sitemapService.crawlSite(site);
            sendCrawlResultsEmail(site, crawl);
        } catch (SitemapException e) {
            // Handle exception, perhaps log the error or notify an admin
        }
    }

    private void sendCrawlResultsEmail(Site site, Crawl crawl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(site.getEmail());
        message.setSubject("Crawl Results for " + site.getName());
        message.setText("Crawl results: " + crawl.getDiffToPrevCrawl().toString());
        mailSender.send(message);
    }
}
