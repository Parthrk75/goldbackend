package com.example.service;

import com.example.dto.GoldCsvDTO;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class GoldPriceService {

    private static final String CSV_FILE_PATH = "src/main/resources/historical_gold_spot_prices.csv";

    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.SMART);

    /**
     * Reads entries from the CSV file for the last 'n' days.
     *
     * @param days the number of days to fetch
     * @return a list of GoldCsvDTO objects containing the relevant entries
     */
    public List<GoldCsvDTO> getEntriesByDays(int days) {
        List<GoldCsvDTO> goldPrices = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startDate = today.minusDays(days); // Calculate the start date for the range

        System.out.println("Filtering entries between " + startDate + " and " + today); // Debug log

        try (Reader reader = new FileReader(Paths.get(CSV_FILE_PATH).toFile())) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                try {
                    String dateStr = record.get(0); // Date is in the first column
                    if (dateStr == null || dateStr.isEmpty()) {
                        System.err.println("Skipping row with empty date: " + record);
                        continue; // Skip empty rows
                    }

                    LocalDateTime date = LocalDateTime.parse(dateStr, FLEXIBLE_FORMATTER);
                    // Include entries that are between the start date and today (inclusive)
                    if (!date.isBefore(startDate) && !date.isAfter(today)) {
                        String priceStr = record.get(record.size() - 1); // Get the last column value (price)
                        if (priceStr != null && !priceStr.isEmpty()) {
                            double price = Double.parseDouble(priceStr);
                            goldPrices.add(new GoldCsvDTO(date, price));
                        } else {
                            System.err.println("Skipping row with empty price: " + record);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Skipping invalid row: " + record + " Error: " + e.getMessage());
                }
            }

            // Sort the data by date (most recent first)
            goldPrices = goldPrices.stream()
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Total entries after filtering: " + goldPrices.size());
        
        // Display the first and last entries in the filtered list
        if (!goldPrices.isEmpty()) {
            System.out.println("First entry: " + goldPrices.get(0));
            System.out.println("Last entry: " + goldPrices.get(goldPrices.size() - 1));
        }

        return goldPrices;
    }

    /**
     * Get the most recent price data.
     *
     * @return a GoldCsvDTO object containing the latest date and price
     */
    public GoldCsvDTO getMostRecentPrice() {
        List<GoldCsvDTO> allPrices = getEntriesByDays(Integer.MAX_VALUE); // Fetch all data
        return allPrices.isEmpty() ? null : allPrices.get(0); // Return the most recent entry
    }

    /**
     * Get prices for a custom date range.
     *
     * @param startDate the start date of the range
     * @param endDate   the end date of the range
     * @return a list of GoldCsvDTO objects within the specified range
     */
    public List<GoldCsvDTO> getPricesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<GoldCsvDTO> allPrices = getEntriesByDays(Integer.MAX_VALUE); // Fetch all data
        return allPrices.stream()
                .filter(price -> !price.getTimestamp().isBefore(startDate) && !price.getTimestamp().isAfter(endDate))
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .collect(Collectors.toList());
    }
}
