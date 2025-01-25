package com.example.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
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
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GoldPriceCsvService {

    private static final Logger logger = LoggerFactory.getLogger(GoldPriceCsvService.class);
    private static final String API_URL = "https://api.gold-api.com/price/XAU"; // Replace with actual API URL
    private static final String CSV_FILE_NAME = "historical_gold_spot_prices.csv"; // File in classpath
    private static final String WRITABLE_CSV_FILE = "/var/data/historical_gold_spot_prices.csv"; // Writable location
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private static final DateTimeFormatter OUTPUT_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final RestTemplate restTemplate;

    public GoldPriceCsvService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Ensures that the CSV file is available at a writable location and copies it from the classpath if necessary.
     */
    private void ensureWritableCsvFile() {
        try {
            Path writablePath = Path.of(WRITABLE_CSV_FILE);

            // If file already exists, no need to copy again.
            if (Files.exists(writablePath)) {
                logger.debug("Writable CSV file already exists at: {}", writablePath);
                return;
            }

            ClassPathResource resource = new ClassPathResource(CSV_FILE_NAME);
            Files.createDirectories(writablePath.getParent());

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
     * Reformat the dates in the CSV file to match the desired format: dd-MM-yyyy HH:mm
     */
    public void reformatCsvDates() {
        ensureWritableCsvFile();

        try {
            Path writablePath = Path.of(WRITABLE_CSV_FILE);
            List<CSVRecord> records;

            // Read existing data from the CSV
            try (BufferedReader reader = Files.newBufferedReader(writablePath, StandardCharsets.UTF_8);
                 CSVParser parser = new CSVParser(reader, CSVFormat.DEFAULT)) {
                records = parser.getRecords();
                logger.debug("Read {} records from the CSV file.", records.size());
            }

            // Reformat and write the updated data
            try (BufferedWriter writer = Files.newBufferedWriter(writablePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
                 CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withRecordSeparator("\n"))) {

                for (CSVRecord record : records) {
                    String rawDate = record.get(0);
                    String formattedDate;
                    try {
                        LocalDateTime dateTime = ZonedDateTime.parse(rawDate, INPUT_FORMAT).toLocalDateTime();
                        formattedDate = dateTime.format(OUTPUT_FORMAT);
                    } catch (Exception e) {
                        logger.warn("Skipping invalid date format: {}", rawDate);
                        formattedDate = rawDate; // Retain the original if parsing fails
                    }

                    // Reprint the record with formatted date
                    printer.printRecord(formattedDate, record.get(1), record.get(2), record.get(3), record.get(4));
                }
            }

            logger.info("Successfully reformatted dates in the CSV file.");
        } catch (Exception e) {
            logger.error("Error while reformatting dates in the CSV file: {}", e.getMessage(), e);
        }
    }

    /**
     * Appends the current gold price to the writable CSV file using the formatted date.
     *
     * @return a message indicating success or failure.
     */
    public String appendGoldPriceToCsv() {
        logger.debug("Starting to append gold price to the CSV file...");

        try {
            ensureWritableCsvFile();

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

            try (BufferedWriter writer = Files.newBufferedWriter(
                    Path.of(WRITABLE_CSV_FILE),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withRecordSeparator("\n"))) {

                csvPrinter.printRecord(formattedDate, "0", "0", "0", String.valueOf(goldPrice));
                logger.info("Gold price appended successfully to the CSV file.");
            }

            return "Gold price appended successfully!";
        } catch (Exception e) {
            logger.error("Error while appending gold price to the CSV file: {}", e.getMessage(), e);
            return "Error while appending gold price: " + e.getMessage();
        }
    }

    /**
     * Scheduler to run appendGoldPriceToCsv at 00:20 New York time daily.
     */
    @Scheduled(cron = "0 20 0 * * ?", zone = "America/New_York")
    public void scheduledAppendGoldPrice() {
        logger.info("Running scheduled task to append gold price...");
        String result = appendGoldPriceToCsv();
        logger.info("Scheduled task result: {}", result);
    }

    /**
     * This method ensures the CSV file is always accessible for reading and appending without any overwrite risk.
     * Will trigger once when the server starts.
     */

    @Scheduled(cron = "0 20 0 * * ?", zone = "America/New_York") // Run every day at 00:20 New York time
    public void checkCsvFile() {
        logger.info("Checking CSV file before appending data...");

        // Ensure the CSV file is available and not corrupted or missing.
        ensureWritableCsvFile();

        // Log if file is successfully found or not.
        Path writablePath = Path.of(WRITABLE_CSV_FILE);
        if (Files.exists(writablePath)) {
            logger.info("CSV file found and ready for appending.");
        } else {
            logger.error("CSV file not found or is corrupted!");
        }
    }
}
