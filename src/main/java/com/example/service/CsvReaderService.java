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
import java.util.List;

@Service
public class CsvReaderService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderService.class);
    private static final String CSV_FILE_PATH = "/var/data/historical_gold_spot_prices.csv"; // Adjust path if needed

    /**
     * Fetches the last 'n' entries from the CSV file and logs them.
     *
     * @param numEntries Number of entries to fetch.
     * @return List of the last 'n' lines from the CSV file.
     */
    public List<String> getLastNEntries(int numEntries) {
        LinkedList<String> allLines = new LinkedList<>();
        Path csvPath = Paths.get(CSV_FILE_PATH);

        logger.info("Attempting to read CSV file from path: {}", csvPath.toAbsolutePath());

        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath.toFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                allLines.add(line);
            }

            // Calculate the last 'n' entries
            int startIndex = Math.max(allLines.size() - numEntries, 0);
            List<String> lastNLines = allLines.subList(startIndex, allLines.size());

            // Log the entries to the terminal
            logger.info("Last {} entries from the CSV file:", numEntries);
            lastNLines.forEach(logger::info);

            return lastNLines;
        } catch (IOException e) {
            logger.error("Error reading the CSV file: {}", e.getMessage(), e);
        }

        return List.of(); // Return an empty list if an error occurs
    }
}
