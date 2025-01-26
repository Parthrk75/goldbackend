package com.example.service;

import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final String API_URL = "https://api.gold-api.com/price/XAU"; // Replace with actual API URL
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // Define the CSV file path within the project folder (e.g., "data" folder)
    private static final Path CSV_FILE_PATH = Paths.get("data", "historical_gold_spot_prices.csv");

    private final RestTemplate restTemplate;

    public GoldPriceCsvService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        ensureCsvFileExists();
    }

    /**
     * Ensures that the CSV file exists and is accessible.
     * Creates the file if it does not already exist.
     */
    private void ensureCsvFileExists() {
        try {
            logger.info("CSV file path: {}", CSV_FILE_PATH.toAbsolutePath());

            // Create directories if they don't exist
            if (Files.notExists(CSV_FILE_PATH.getParent())) {
                Files.createDirectories(CSV_FILE_PATH.getParent());
                logger.info("Created directories: {}", CSV_FILE_PATH.getParent());
            }

            // Create the CSV file if it doesn't exist
            if (Files.notExists(CSV_FILE_PATH)) {
                try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH.toFile(), true))) {
                    String[] headers = {"Date", "Open", "High", "Low", "Close"};
                    writer.writeNext(headers);
                    logger.info("CSV file created with headers at: {}", CSV_FILE_PATH.toAbsolutePath());
                }
            } else {
                logger.info("CSV file already exists at: {}", CSV_FILE_PATH.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Failed to ensure CSV file existence: {}", e.getMessage(), e);
            throw new RuntimeException("Error ensuring CSV file existence", e);
        }
    }

    /**
     * Appends the current gold price to the CSV file.
     *
     * @return A message indicating success or failure.
     */
    public String appendGoldPriceToCsv() {
        logger.debug("Starting to append gold price to the CSV file...");

        try {
            logger.debug("Fetching live gold price from API: {}", API_URL);
            GoldPriceResponse response = restTemplate.getForObject(API_URL, GoldPriceResponse.class);

            if (response == null || response.getPrice() <= 0) {
                logger.error("Error: Could not fetch a valid gold price from the API.");
                return "Error: Could not fetch the gold price.";
            }

            double goldPrice = response.getPrice();
            logger.info("Fetched gold price: {}", goldPrice);

            LocalDateTime now = ZonedDateTime.now(java.time.ZoneId.of("America/New_York")).toLocalDateTime();
            String formattedDate = now.format(OUTPUT_FORMAT);

            // Append data to the CSV file
            try (CSVWriter writer = new CSVWriter(new FileWriter(CSV_FILE_PATH.toFile(), true))) {
                String[] record = {formattedDate, "0", "0", "0", String.valueOf(goldPrice)};
                writer.writeNext(record);
                logger.info("Gold price appended successfully to the CSV file.");
            }

            return "Gold price appended successfully!";
        } catch (Exception e) {
            logger.error("Error while appending gold price to the CSV file: {}", e.getMessage(), e);
            return "Error while appending gold price: " + e.getMessage();
        }
    }

    /**
     * Reads the contents of the CSV file using BufferedReader.
     *
     * @return The contents of the CSV file as a List of Strings, or an empty list if the file is not found or empty.
     */
    public List<String> readCsvFile() {
        if (Files.exists(CSV_FILE_PATH)) {
            try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH.toFile()))) {
                List<String> lines = reader.lines().toList();

                if (lines.isEmpty()) {
                    logger.warn("CSV file is empty.");
                    return List.of(); // Return empty list if the file is empty
                }

                logger.info("CSV file read successfully.");
                return lines; // Return lines from the CSV file
            } catch (IOException e) {
                logger.error("Error reading the CSV file: {}", e.getMessage(), e);
                return List.of(); // Return empty list on error
            }
        } else {
            logger.error("CSV file does not exist.");
            return List.of(); // Return empty list if file doesn't exist
        }
    }
}
