package com.example.service;

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

@Service
public class GoldPriceDataService {

    private static final String CSV_FILE_PATH = "src/main/resources/historical_gold_spot_prices.csv";

    // Flexible DateTimeFormatter for multiple date formats
    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.SMART);

    public List<GoldPriceData> getGoldPriceData(int days) {
        List<GoldPriceData> goldPriceDataList = new ArrayList<>();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        try (Reader reader = new FileReader(Paths.get(CSV_FILE_PATH).toFile())) {
            CSVParser csvParser = CSVFormat.DEFAULT
                    .withFirstRecordAsHeader() // Skip the header row
                    .parse(reader);

            for (CSVRecord record : csvParser) {
                try {
                    // Check if the record has a valid date field
                    if (record.get(0) == null || record.get(0).isEmpty()) {
                        System.err.println("Skipping row with empty date: " + record);
                        continue;
                    }

                    String dateStr = record.get(0); // Date is in the first column
                    LocalDateTime date = LocalDateTime.parse(dateStr, FLEXIBLE_FORMATTER);

                    // Skip rows older than the cutoff date
                    if (date.isBefore(cutoffDate)) {
                        continue;
                    }

                    // Extract the price
                    String priceStr = extractPrice(record);
                    if (priceStr != null) {
                        double price = Double.parseDouble(priceStr);
                        goldPriceDataList.add(new GoldPriceData(dateStr, price));
                    }
                } catch (Exception innerException) {
                    // Log and skip malformed rows
                    System.err.println("Skipping invalid row: " + record + " Error: " + innerException.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return goldPriceDataList;
    }

    /**
     * Extracts the price from the CSVRecord. 
     * If the record has multiple values, it takes the last value.
     * If it has a single value, it uses that value directly.
     */
    private String extractPrice(CSVRecord record) {
        int valueCount = record.size();
        // Find the last non-empty value
        for (int i = valueCount - 1; i > 0; i--) {
            if (record.get(i) != null && !record.get(i).isEmpty()) {
                return record.get(i);
            }
        }
        return null; // Return null if no valid value is found
    }

    public static class GoldPriceData {
        private String date;
        private double price;

        public GoldPriceData(String date, double price) {
            this.date = date;
            this.price = price;
        }

        public String getDate() {
            return date;
        }

        public double getPrice() {
            return price;
        }
    }
}
