package de.samuelgesang.backend.crawls;

import de.samuelgesang.backend.exceptions.SitemapException;
import de.samuelgesang.backend.sites.Site;
import de.samuelgesang.backend.sites.SiteService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CrawlScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlScheduleService.class);

    private final SiteService siteService;
    private final JavaMailSender mailSender;
    private final CrawlRepository crawlRepository;
    private final CrawlService crawlService;

    public CrawlScheduleService(SiteService siteService, JavaMailSender mailSender, CrawlRepository crawlRepository, CrawlService crawlService1) {
        this.siteService = siteService;
        this.mailSender = mailSender;
        this.crawlRepository = crawlRepository;
        this.crawlService = crawlService1;
    }

    //    @Scheduled(cron = "0 */5 * * * *") // Runs every 5 minutes for testing
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
            Crawl crawl = crawlService.crawlSite(site);
            sendCrawlResultsEmail(site, crawl);
        } catch (SitemapException e) {
            logger.error("Error during crawl for site {}: {}", site.getName(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during crawl for site {}: {}", site.getName(), e.getMessage(), e);
        }
    }

    private void sendCrawlResultsEmail(Site site, Crawl crawl) {
        if (site.getEmail() == null || !isValidEmail(site.getEmail())) {
            logger.error("Invalid email address: {}", site.getEmail());
            return;
        }

        List<CrawlDiffItem> diffToPrevCrawl = crawl.getDiffToPrevCrawl();
        StringBuilder emailBody = createEmailBody(site, crawl);

        if (diffToPrevCrawl == null || diffToPrevCrawl.isEmpty()) {
            emailBody.append("No changes");
            sendSimpleEmail(site, emailBody.toString());
        } else {
            sendEmailWithDiff(site, emailBody, diffToPrevCrawl);
        }
    }

    private void sendSimpleEmail(Site site, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(site.getEmail());
        message.setSubject("Crawl Results for " + site.getName());
        message.setText(text);
        try {
            mailSender.send(message);
            logger.info("Crawl results email sent successfully to {}", site.getEmail());
        } catch (MailException e) {
            logger.error("Failed to send crawl results email to {}: {}", site.getEmail(), e.getMessage(), e);
        }
    }

    private void sendEmailWithDiff(Site site, StringBuilder emailBody, List<CrawlDiffItem> diffToPrevCrawl) {
        MimeMessagePreparator preparator = createMimeMessagePreparator(site, emailBody, diffToPrevCrawl);

        try {
            mailSender.send(preparator);
            logger.info("Crawl results email sent successfully to {}", site.getEmail());
        } catch (MailException e) {
            logger.error("Failed to send crawl results email to {}: {}", site.getEmail(), e.getMessage(), e);
        }
    }

    private MimeMessagePreparator createMimeMessagePreparator(Site site, StringBuilder emailBody, List<CrawlDiffItem> diffToPrevCrawl) {
        return mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setTo(site.getEmail());
            messageHelper.setSubject("Crawl Results for " + site.getName());

            if (diffToPrevCrawl.size() <= 5) {
                diffToPrevCrawl.forEach(item -> emailBody.append(item.toString()).append("\n"));
                messageHelper.setText(emailBody.toString());
            } else {
                emailBody.append("See attached CSV file for more details.");
                messageHelper.setText(emailBody.toString());
                attachCsv(diffToPrevCrawl, messageHelper);
            }
        };
    }

    private StringBuilder createEmailBody(Site site, Crawl crawl) {
        StringBuilder sb = new StringBuilder();
        sb.append("Site: ").append(site.getName()).append("\n");
        sb.append("Crawl Schedule: ").append(site.getCrawlSchedule()).append("\n");
        sb.append("Crawl Date: ").append(formatTimestamp(crawl.getFinishedAt())).append("\n");

        if (crawl.getPrevCrawlId() != null) {
            crawlRepository.findById(crawl.getPrevCrawlId()).ifPresent(prevCrawl -> sb.append("Previous Crawl Date: ").append(formatTimestamp(prevCrawl.getFinishedAt())).append("\n"));
        }

        sb.append("Crawl differences: \n");
        return sb;
    }

    private void attachCsv(List<CrawlDiffItem> diffToPrevCrawl, MimeMessageHelper messageHelper) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(osw, CSVFormat.Builder.create().setHeader("Action", "URL", "Checked").build())) {
            for (CrawlDiffItem item : diffToPrevCrawl) {
                printer.printRecord(item.getAction(), item.getUrl(), item.isChecked());
            }
        } catch (IOException e) {
            logger.error("Error while creating CSV", e);
        }

        ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());
        try {
            messageHelper.addAttachment("crawl_diff.csv", resource);
        } catch (MessagingException e) {
            logger.error("Failed to add attachment to email", e);
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
