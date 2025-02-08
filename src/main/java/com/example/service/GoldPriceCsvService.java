package com.example.service;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GoldPriceCsvService {

    private static final Logger logger = LoggerFactory.getLogger(GoldPriceCsvService.class);
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Value("${csv.file.path:.data/historical_gold_spot_prices.csv}")
    private String csvFilePath;

    @Value("${gold.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public GoldPriceCsvService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private void ensureCsvFileExists() {
        try {
            Path path = Paths.get(csvFilePath);
            if (Files.notExists(path.getParent())) {
                logger.info("CSV file not found: {}", path.getParent());
            }
        } catch (Exception e) {
            logger.error("Failed to ensure CSV file existence: {}", e.getMessage(), e);
            throw new RuntimeException("Error ensuring CSV file existence", e);
        }
    }

    public String appendGoldPriceToCsv() {
        ensureCsvFileExists();

        try {
            GoldPriceResponse response = restTemplate.getForObject(apiUrl, GoldPriceResponse.class);

            if (response == null || response.getPrice() <= 0) {
                logger.error("Invalid response from the API.");
                return "Error: Could not fetch the gold price.";
            }

            double goldPrice = response.getPrice();
            logger.info("Fetched gold price: {}", goldPrice);

            LocalDateTime now = ZonedDateTime.now(java.time.ZoneId.of("America/New_York")).toLocalDateTime();
            String formattedDate = now.format(OUTPUT_FORMAT);

            synchronized (this) {
                Path path = Paths.get(csvFilePath);
                try (CSVWriter writer = new CSVWriter(new FileWriter(path.toFile(), true))) {
                    String[] record = {formattedDate, "0", "0", "0", String.valueOf(goldPrice)};
                    writer.writeNext(record);
                    logger.info("Gold price appended successfully to the CSV file.");
                }
            }

            return "Gold price appended successfully!";
        } catch (Exception e) {
            logger.error("Error while appending gold price: {}", e.getMessage(), e);
            return "Error while appending gold price: " + e.getMessage();
        }
    }

    // Scheduled job to run at 10 PM New York Time every day
    @Scheduled(cron = "0 0 22 * * ?", zone = "America/New_York")
    public void scheduleGoldPriceCsvAppend() {
        logger.info("Running scheduled task: appendGoldPriceToCsv()");
        appendGoldPriceToCsv();
    }
}
