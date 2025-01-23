package com.example.controller;

import com.example.dto.GoldPriceDTO;
import com.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.GoldCsvDTO;
import com.example.dto.LiveGoldPriceDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
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
    public String appendGoldPriceToCsv() {
        return goldPriceCsvService.appendGoldPriceToCsv();
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


    /**
     * API to fetch gold price entries based on the specified number of days.
     * Endpoint: /gold-price-entries
     * @param days Number of days for which to fetch entries (default is 7 days).
     * @return List of gold price entries.
     */
    @GetMapping("/gold-price-entries")
    public ResponseEntity<?> getGoldPriceEntries(
            @RequestParam(value = "days", required = false) Integer days,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // If a custom date range is provided
            if (startDate != null || endDate != null) {
                if (startDate == null || endDate == null) {
                    return ResponseEntity.badRequest()
                            .body("Both 'startDate' and 'endDate' must be provided for a date range.");
                }

                LocalDateTime start;
                LocalDateTime end;
                try {
                    start = LocalDate.parse(startDate, formatter).atStartOfDay();
                    end = LocalDate.parse(endDate, formatter).atStartOfDay();
                } catch (Exception e) {
                    return ResponseEntity.badRequest()
                            .body("Invalid date format. Please use 'yyyy-MM-dd'.");
                }

                if (end.isBefore(start)) {
                    return ResponseEntity.badRequest().body("End date must be after start date.");
                }

                List<GoldCsvDTO> goldPrices = goldPriceService.getPricesByDateRange(start, end);
                return goldPrices.isEmpty()
                        ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the specified date range.")
                        : ResponseEntity.ok(goldPrices);
            }

            // Fallback to 'days' parameter
            if (days == null || days < 1) {
                return ResponseEntity.badRequest().body("The 'days' parameter must be greater than 0.");
            }

            List<GoldCsvDTO> goldPrices = goldPriceService.getEntriesByDays(days);
            return goldPrices.isEmpty()
                    ? ResponseEntity.status(HttpStatus.NOT_FOUND).body("No data found for the last " + days + " days.")
                    : ResponseEntity.ok(goldPrices);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching the data: " + e.getMessage());
        }
    }

    @GetMapping("/last-entries")
    public void getLastSevenEntries() {
        csvReaderService.logLastSevenEntries();
    }
}
