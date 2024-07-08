package de.samuelgesang.backend.crawls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ServerTimeLogger implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ServerTimeLogger.class);

    @Override
    public void run(String... args) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedNow = now.format(formatter);
        logger.info("Current server time: " + formattedNow);
    }
}
