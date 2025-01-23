package com.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;

@Service
public class CsvReaderService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderService.class);
    private static final String CSV_FILE_PATH = "/var/data/historical_gold_spot_prices.csv"; // Adjust path if needed

    /**
     * Logs the last 7 entries from the CSV file.
     */
    public void logLastSevenEntries() {
        LinkedList<String> lastSevenLines = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(Paths.get(CSV_FILE_PATH).toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Maintain only the last 7 lines in the list
                if (lastSevenLines.size() == 7) {
                    lastSevenLines.removeFirst();
                }
                lastSevenLines.add(line);
            }

            logger.info("Last 7 entries from the CSV file:");
            lastSevenLines.forEach(logger::info);
        } catch (IOException e) {
            logger.error("Error reading the CSV file: {}", e.getMessage(), e);
        }
    }
}
