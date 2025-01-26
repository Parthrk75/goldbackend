package com.example.service;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GoldPriceCsvService {

    private static final Logger logger = LoggerFactory.getLogger(GoldPriceCsvService.class);
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Value("${csv.file.path:data/historical_gold_spot_prices.csv}")
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
                Files.createDirectories(path.getParent());
                logger.info("Created directories: {}", path.getParent());
            }

            if (Files.notExists(path)) {
                try (CSVWriter writer = new CSVWriter(new FileWriter(path.toFile(), true))) {
                    String[] headers = {"Date", "Open", "High", "Low", "Close"};
                    writer.writeNext(headers);
                    logger.info("CSV file created with headers at: {}", path.toAbsolutePath());
                }
            }
        } catch (IOException e) {
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

    public List<String> readCsvFile() {
        Path path = Paths.get(csvFilePath);

        if (Files.exists(path)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                List<String> lines = reader.lines().toList();
                if (lines.isEmpty()) {
                    logger.warn("CSV file is empty.");
                    return List.of();
                }
                logger.info("CSV file read successfully.");
                return lines;
            } catch (IOException e) {
                logger.error("Error reading the CSV file: {}", e.getMessage(), e);
                return List.of();
            }
        } else {
            logger.error("CSV file does not exist: {}", path.toAbsolutePath());
            return List.of();
        }
    }
}
