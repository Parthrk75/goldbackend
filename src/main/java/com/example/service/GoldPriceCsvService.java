package com.example.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GoldPriceCsvService {

    private static final String API_URL = "https://api.gold-api.com/price/XAU"; // Replace with the actual API URL
    private static final String CSV_FILE_PATH = "src/main/resources/historical_gold_spot_prices.csv"; // Direct file path

    private final RestTemplate restTemplate;

    public GoldPriceCsvService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Appends the current gold price to the CSV file.
     */
    public String appendGoldPriceToCsv() {
        try {
            // Fetch the live gold price from the API
            GoldPriceResponse response = restTemplate.getForObject(API_URL, GoldPriceResponse.class);

            if (response == null || response.getPrice() <= 0) {
                return "Error: Could not fetch the gold price.";
            }

            double goldPrice = response.getPrice();

            // Get the current date and time
            ZonedDateTime currentZonedDateTime = ZonedDateTime.now();
            String currentDateTime = currentZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"));

            // Prepare the record to append
            String[] newRecord = {currentDateTime, "0", "0", "0", String.valueOf(goldPrice)};

            // Ensure the directory for the file exists
            Path csvPath = Path.of(CSV_FILE_PATH);
            Files.createDirectories(csvPath.getParent());

            // Append data to the CSV file
            try (BufferedWriter writer = Files.newBufferedWriter(csvPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withRecordSeparator("\n"))) {
                csvPrinter.printRecord((Object[]) newRecord);
            }

            return "Gold price appended successfully!";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error accessing the CSV file: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error fetching or processing data: " + e.getMessage();
        }
    }

    /**
     * Scheduled task to append gold price to CSV at 11:00 PM every day.
     */
    @Scheduled(cron = "0 0 23 * * ?") // Every day at 11:00 PM
    public void appendGoldPriceAt11PM() {
        String result = appendGoldPriceToCsv();
        System.out.println("Scheduled Task - Append Gold Price to CSV at 11 PM: " + result);
    }
}
