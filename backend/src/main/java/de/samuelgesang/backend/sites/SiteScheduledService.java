package de.samuelgesang.backend.sites;

import de.samuelgesang.backend.crawls.Crawl;
import de.samuelgesang.backend.crawls.CrawlDiffItem;
import de.samuelgesang.backend.crawls.CrawlRepository;
import de.samuelgesang.backend.exceptions.SitemapException;
import de.samuelgesang.backend.sitemaps.SitemapService;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class SiteScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(SiteScheduledService.class);

    private final SiteService siteService;
    private final JavaMailSender mailSender;
    private final SitemapService sitemapService;
    private final CrawlRepository crawlRepository;

    public SiteScheduledService(SiteService siteService, JavaMailSender mailSender, SitemapService sitemapService, CrawlRepository crawlRepository) {
        this.siteService = siteService;
        this.mailSender = mailSender;
        this.sitemapService = sitemapService;
        this.crawlRepository = crawlRepository;
    }

    @Scheduled(cron = "0 */5 * * * *") // Runs every 5 minutes for testing
    public void testScheduledCrawls() {
        List<Site> sites = siteService.getAllSitesWithSchedule("daily");
        for (Site site : sites) {
            performCrawl(site);
        }
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
            crawlRepository.save(crawl);
            updateSiteWithNewCrawl(site, crawl);
            sendCrawlResultsEmail(site, crawl);
        } catch (SitemapException e) {
            logger.error("Error during crawl for site {}: {}", site.getName(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during crawl for site {}: {}", site.getName(), e.getMessage(), e);
        }
    }

    private void updateSiteWithNewCrawl(Site site, Crawl crawl) {
        List<String> crawlIds = site.getCrawlIds();
        if (crawlIds == null) {
            crawlIds = new ArrayList<>();
        }
        crawlIds.add(crawl.getId());
        site.setCrawlIds(crawlIds);
        siteService.updateSite(site.getId(), site);
    }

    private void sendCrawlResultsEmail(Site site, Crawl crawl) {
        if (site.getEmail() == null || !isValidEmail(site.getEmail())) {
            logger.error("Invalid email address: {}", site.getEmail());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(site.getEmail());
        message.setSubject("Crawl Results for " + site.getName());

        List<CrawlDiffItem> diffToPrevCrawl = crawl.getDiffToPrevCrawl();
        if (diffToPrevCrawl == null || diffToPrevCrawl.isEmpty()) {
            message.setText("No changes");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Site: ").append(site.getName()).append("\n");
            sb.append("Crawl Schedule: ").append(site.getCrawlSchedule()).append("\n");
            sb.append("Crawl Date: ").append(formatTimestamp(crawl.getFinishedAt())).append("\n");

            if (crawl.getPrevCrawlId() != null) {
                crawlRepository.findById(crawl.getPrevCrawlId()).ifPresent(prevCrawl -> sb.append("Previous Crawl Date: ").append(formatTimestamp(prevCrawl.getFinishedAt())).append("\n"));
            }

            sb.append("Crawl differences: \n");
            diffToPrevCrawl.stream().limit(300).forEach(item -> sb.append(item.toString()).append("\n"));

            if (diffToPrevCrawl.size() > 300) {
                sb.append("\n...and more");
            }

            message.setText(sb.toString());
        }


        try {
            mailSender.send(message);
            logger.info("Crawl results email sent successfully to {}", site.getEmail());
        } catch (MailException e) {
            logger.error("Failed to send crawl results email to {}: {}", site.getEmail(), e.getMessage(), e);
        }
    }

    private boolean isValidEmail(String email) {
        boolean isValid = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            isValid = false;
        }
        return isValid;
    }

    private String formatTimestamp(String timestamp) {
        Instant instant = Instant.parse(timestamp);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());
        return formatter.format(instant);
    }
}
