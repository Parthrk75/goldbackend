package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

@Service
public class CsvReaderService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderService.class);
    private static final String CSV_FILE_PATH = "/var/data/historical_gold_spot_prices.csv"; // Adjust path if needed

    /**
     * Logs the last 'n' entries from the CSV file.
     */
    public void logLastNEntries(int numEntries) {
        if (numEntries <= 0) {
            logger.error("Invalid number of entries specified: {}. Must be greater than zero.", numEntries);
            return;
        }

        LinkedList<String> allLines = new LinkedList<>();
        Path csvPath = Paths.get(CSV_FILE_PATH);

        // Log the absolute path being used
        logger.info("Attempting to read CSV file from path: {}", csvPath.toAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Collect all lines from the CSV file
                allLines.add(line);
            }

            // Get the last 'numEntries' lines
            int startIndex = Math.max(allLines.size() - numEntries, 0);
            LinkedList<String> lastNLines = new LinkedList<>(allLines.subList(startIndex, allLines.size()));

            logger.info("Last {} entries from the CSV file:", numEntries);
            lastNLines.forEach(logger::info);
        } catch (IOException e) {
            logger.error("Error reading the CSV file: {}", e.getMessage(), e);
        }
    }
}
