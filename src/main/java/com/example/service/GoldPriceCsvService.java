package com.example.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GoldPriceCsvService {

    private static final Logger logger = LoggerFactory.getLogger(GoldPriceCsvService.class);
    private static final String API_URL = "https://api.gold-api.com/price/XAU"; // Replace with actual API URL
    private static final String CSV_FILE_NAME = "historical_gold_spot_prices.csv"; // File in classpath
    private static final String WRITABLE_CSV_FILE = "/var/data/historical_gold_spot_prices.csv"; // Writable location

    private final RestTemplate restTemplate;

    public GoldPriceCsvService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Ensures that the CSV file is available at a writable location.
     */
    private void ensureWritableCsvFile() {
        try {
            Path writablePath = Path.of(WRITABLE_CSV_FILE);

            // Check if the writable file already exists
            if (Files.exists(writablePath)) {
                logger.debug("Writable CSV file already exists at: {}", writablePath);
                return; // No need to copy again
            }

            // Load the CSV file from resources
            ClassPathResource resource = new ClassPathResource(CSV_FILE_NAME);

            // Ensure the writable directory exists
            Files.createDirectories(writablePath.getParent());

            // Copy the CSV file from resources to writable location
            try (InputStream inputStream = resource.getInputStream()) {
                Files.copy(inputStream, writablePath);
                logger.info("CSV file successfully copied to writable location: {}", writablePath);
            }
        } catch (Exception e) {
            logger.error("Failed to copy the CSV file to writable location: {}", e.getMessage(), e);
            throw new RuntimeException("Error ensuring writable CSV file", e);
        }
    }

    /**
     * Appends the current gold price to the writable CSV file using the New York time zone.
     *
     * @return a message indicating success or failure.
     */
    public String appendGoldPriceToCsv() {
        logger.debug("Starting to append gold price to the CSV file...");

        try {
            ensureWritableCsvFile(); // Ensure the file is available for appending

            // Fetch the live gold price from the API
            logger.debug("Fetching live gold price from API: {}", API_URL);
            GoldPriceResponse response = restTemplate.getForObject(API_URL, GoldPriceResponse.class);

            if (response == null || response.getPrice() <= 0) {
                logger.error("Error: Could not fetch a valid gold price from the API.");
                return "Error: Could not fetch the gold price.";
            }

            double goldPrice = response.getPrice();
            logger.info("Fetched gold price: {}", goldPrice);

            // Get the current date and time in the New York time zone
            ZonedDateTime newYorkTime = ZonedDateTime.now(java.time.ZoneId.of("America/New_York"));
            String formattedNewYorkTime = newYorkTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));
            logger.debug("New York time for the record: {}", formattedNewYorkTime);

            // Append the new record to the writable CSV file
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Path.of(WRITABLE_CSV_FILE),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withRecordSeparator("\n"))) {

                csvPrinter.printRecord(formattedNewYorkTime, "0", "0", "0", String.valueOf(goldPrice));
                logger.info("Gold price appended successfully to the CSV file.");
            }

            return "Gold price appended successfully!";
        } catch (Exception e) {
            logger.error("Error while appending gold price to the CSV file: {}", e.getMessage(), e);
            return "Error while appending gold price: " + e.getMessage();
        }
    }

    /**
     * Scheduled task to append gold price to CSV at 11:00 PM New York time.
     */
    @Scheduled(cron = "0 0 23 * * ?", zone = "America/New_York") // Every day at 11:00 PM New York time
    public void appendGoldPriceAt11PM() {
        logger.info("Scheduled task triggered to append gold price at 11 PM New York time.");
        String result = appendGoldPriceToCsv();
        logger.info("Scheduled Task Result: {}", result);
    }
}
