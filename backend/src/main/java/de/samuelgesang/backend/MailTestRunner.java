package de.samuelgesang.backend;

import de.samuelgesang.backend.mail.MailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;

//@Component // Enable this to send test mail at app start
public class MailTestRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MailTestRunner.class);

    private final MailService mailService;
    private final String testRecipient;

    public MailTestRunner(MailService mailService, @Value("${spring.mail.test-recipient}") String testRecipient) {
        this.mailService = mailService;
        this.testRecipient = testRecipient;
    }

    @Override
    public void run(String... args) {
        try {
            mailService.sendTestEmail(testRecipient, "Test Email", "This is a test email from Spring Boot application.");
            logger.info("Test email sent successfully");
        } catch (MailException e) {
            logger.error("Failed to send test email due to mail exception: {}", e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Failed to send test email due to an unexpected exception: {}", e.getMessage(), e);
        }
    }
}
