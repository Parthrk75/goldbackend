package com.example.controller;

import com.example.dto.GoldPriceDTO;
import com.example.service.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.GoldCsvDTO;
import com.example.dto.LiveGoldPriceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174",
        "http://localhost:3000",
        "https://goldfrontend.vercel.app"
})

public class GoldPriceController {

    @Autowired
    private GoldPriceFetchService goldPriceFetchService;

    @Autowired
    private GoldPriceCsvService goldPriceCsvService;

    @Autowired
    private GoldPriceSchedulerService goldPriceSchedulerService;

    @Autowired
    private GoldPriceService goldPriceService;

    @Autowired
    private CsvReaderService csvReaderService;

    /**
     * API to append the latest gold price to a CSV file.
     * Endpoint: /append
     * @return Status message of the operation.
     */
    @GetMapping("/append")
    public ResponseEntity<String> appendGoldPriceToCsv() {
        // Create a logger instance for this method
        Logger logger = LoggerFactory.getLogger(GoldPriceController.class);

        try {
            // Call the service method to append the gold price to the CSV file
            String result = goldPriceCsvService.appendGoldPriceToCsv();

            // Log the success
            logger.info("Successfully appended gold price to CSV.");

            // Return 200 OK with the result
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Log the error with detailed exception message
            logger.error("Error occurred while appending gold price to CSV: {}", e.getMessage(), e);

            // Return 500 Internal Server Error with error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred while appending gold price: " + e.getMessage());
        }
    }

    /**
     * API to fetch the live gold price.
     * Endpoint: /live
     * @return Live gold price or an error message if unavailable.
     */
    @GetMapping("/live")
    public LiveGoldPriceDTO getLiveGoldPrice() {
        return goldPriceSchedulerService.getLivePriceDetails();
    }

    /**
     * API to fetch historical gold price data.
     * Endpoint: /historical
     * @return Historical gold price data or an error message if unavailable.
     */
    @GetMapping("/historical")
    public ResponseEntity<?> getHistoricalData() {
        List<GoldPriceDTO> historicalData = goldPriceFetchService.getHistoricalData();
        if (historicalData != null && !historicalData.isEmpty()) {
            return ResponseEntity.ok(historicalData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Historical data not available.");
        }
    }


    @GetMapping("/last-entries")
    public ResponseEntity<?> getLastEntries(@RequestParam(defaultValue = "7") int numEntries) {
        // Check if the number of entries is positive
        if (numEntries <= 0) {
            // Returning a structured error response
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid number of entries specified. Please provide a positive number."));
        }

        // Fetch the last 'numEntries' from the service
        List<ObjectNode> lastEntries = csvReaderService.getLastNEntries(numEntries);

        // Handle the case where no entries were found
        if (lastEntries.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "No entries found for the requested number."));
        }

        // Return the last entries as a JSON response
        return ResponseEntity.ok(lastEntries); // Returns the JSON directly
    }


    /**
     * API to fetch gold price entries based on the specified number of days.
     * Endpoint: /gold-price-entries
     * @param days Number of days for which to fetch entries (default is 7 days).
     * @return List of gold price entries.
     */
//    @GetMapping("/gold-price-entries")
//    public ResponseEntity<?> getGoldPriceEntries(
//            @RequestParam(value = "days", required = false) Integer days,
//            @RequestParam(value = "startDate", required = false) String startDate,
//            @RequestParam(value = "endDate", required = false) String endDate) {
//
//        try {
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//
//            // If a custom date range is provided
//            if (startDate != null || endDate != null) {
//                if (startDate == null || endDate == null) {
//                    return ResponseEntity.badRequest()
//                            .body("Both 'startDate' and 'endDate' must be provided for a date range.");
//                }
//
//                LocalDateTime start;
//                LocalDateTime end;
//                try {
//                    start = LocalDate.parse(startDate, formatter).atStartOfDay();
//                    end = LocalDate.parse(endDate, formatter).atStartOfDay();
//                } catch (Exception e) {
//                    return ResponseEntity.badRequest()
//                            .body("Invalid date format. Please use 'yyyy-MM-dd'.");
//                }
//
//                if (end.isBefore(start)) {
//                    return ResponseEntity.badRequest().body("End date must be after start date.");
//                }
//
//                List<GoldCsvDTO> goldPrices = goldPriceService.getPricesByDateRange(start, end);
//                return goldPrices.isEmpty()
//                        ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the specified date range.")
//                        : ResponseEntity.ok(goldPrices);
//            }
//
//            // Fallback to 'days' parameter
//            if (days == null || days < 1) {
//                return ResponseEntity.badRequest().body("The 'days' parameter must be greater than 0.");
//            }
//
//            List<GoldCsvDTO> goldPrices = goldPriceService.getEntriesByDays(days);
//            return goldPrices.isEmpty()
//                    ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the last " + days + " days.")
//                    : ResponseEntity.ok(goldPrices);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("An error occurred while fetching the data: " + e.getMessage());
//        }
//    }

}
