package com.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to read and process data from a CSV file.
 */
@Service
public class CsvReaderService {

    private static final Logger logger = LoggerFactory.getLogger(CsvReaderService.class);

    // Use the path from application.properties or default to a manual folder location
    @Value("${csv.file.path:/opt/data/historical_gold_spot_prices.csv}")
    private String csvFilePath;

    /**
     * Fetches the last N entries from the CSV file, cleans up inconsistent data, and returns them in JSON format.
     *
     * @param numEntries the number of entries to fetch from the end of the file.
     * @return a list of ObjectNode representing the last N entries.
     */
    public List<ObjectNode> getLastNEntries(int numEntries) {
        ensureCsvFileExists(); // Ensure the file exists

        if (numEntries <= 0) {
            logger.warn("Invalid number of entries requested: {}. Returning empty list.", numEntries);
            return new ArrayList<>();
        }

        Path path = Paths.get(csvFilePath);
        if (Files.notExists(path)) {
            logger.error("CSV file does not exist: {}", path.toAbsolutePath());
            return new ArrayList<>();
        }

        if (!Files.isReadable(path)) {
            logger.error("CSV file is not readable: {}", path.toAbsolutePath());
            return new ArrayList<>();
        }

        List<ObjectNode> lastEntries = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
            List<String> allLines = reader.lines().skip(1).toList(); // Skip header
            if (allLines.isEmpty()) {
                logger.warn("CSV file is empty or has only the header.");
                return lastEntries;
            }

            int startIdx = Math.max(0, allLines.size() - numEntries);
            List<String> lastLines = allLines.subList(startIdx, allLines.size());

            ObjectMapper mapper = new ObjectMapper();
            for (String line : lastLines) {
                if (line.trim().isEmpty()) {
                    logger.warn("Skipping empty line.");
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 5) {
                    logger.warn("Skipping invalid CSV line: {}", line);
                    continue;
                }

                String date = values[0].trim().replaceAll("^\"|\"$", "");
                String close = values[4].trim().replaceAll("^\"|\"$", "");

                ObjectNode jsonObject = mapper.createObjectNode();
                jsonObject.put("Date", date);
                jsonObject.put("Close", close);
                lastEntries.add(jsonObject);
            }

            logger.info("Fetched the last {} entries from the CSV file.", numEntries);
        } catch (IOException e) {
            logger.error("Error reading the CSV file: {}", e.getMessage(), e);
        }

        return lastEntries;
    }

    /**
     * Ensures that the CSV file exists and is accessible.
     * Creates the file if it does not already exist.
     */
    private void ensureCsvFileExists() {
        Path path = Paths.get(csvFilePath);
        try {
            if (Files.notExists(path)) {
                // Create parent directories if they don't exist
                Files.createDirectories(path.getParent());
                logger.info("Directory created: {}", path.getParent());

                // Optionally create an empty CSV file
                Files.createFile(path);
                logger.info("CSV file created: {}", path.toAbsolutePath());
            } else {
                logger.info("CSV file found: {}", path.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Error ensuring CSV file existence: {}", e.getMessage(), e);
        }
    }
}
