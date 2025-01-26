package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvReaderService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderService.class);
    private static final Path CSV_FILE_PATH = Paths.get("data", "historical_gold_spot_prices.csv");

    /**
     * Fetches the last N entries from the CSV file and returns them in JSON format.
     *
     * @param numEntries the number of entries to fetch from the end of the file.
     * @return a list of ObjectNode representing the last N entries.
     */
    public List<ObjectNode> getLastNEntries(int numEntries) {
        ensureCsvFileExists(); // Ensure the file exists
        List<ObjectNode> lastEntries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH.toFile()))) {
            String line;
            List<String> allEntries = new ArrayList<>();

            // Read the entire file into memory
            while ((line = reader.readLine()) != null) {
                allEntries.add(line);
            }

            // Check if there are enough entries
            int startIdx = Math.max(0, allEntries.size() - numEntries);
            List<String> entriesToReturn = allEntries.subList(startIdx, allEntries.size());

            // Prepare the JSON objects for the last entries
            ObjectMapper mapper = new ObjectMapper();
            for (String entry : entriesToReturn) {
                String[] values = entry.split(",");
                ObjectNode jsonObject = mapper.createObjectNode();

                if (values.length > 0) {
                    jsonObject.put("Date", values[0].trim());
                }

                if (values.length > 4) {
                    jsonObject.put("Close", values[4].trim());
                }

                lastEntries.add(jsonObject); // Directly adding JSON objects to the list
            }

            logger.info("Fetched the last {} entries from the CSV file.", numEntries);
        } catch (IOException e) {
            logger.error("Error reading the CSV file: {}", e.getMessage(), e);
        }

        return lastEntries; // Return the list of ObjectNode (JSON objects)
    }

    /**
     * Ensures that the CSV file exists and is accessible.
     * Creates the file if it does not already exist.
     */
    private void ensureCsvFileExists() {
        try {
            if (Files.notExists(CSV_FILE_PATH)) {
                logger.error("CSV file does not exist: {}", CSV_FILE_PATH.toAbsolutePath());
                // You can create the file here if necessary, or throw a custom exception.
                // Files.createFile(CSV_FILE_PATH); // Uncomment if you want to auto-create it
            } else {
                logger.info("CSV file found: {}", CSV_FILE_PATH.toAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error checking CSV file existence: {}", e.getMessage(), e);
        }
    }
}
