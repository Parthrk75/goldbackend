package com.example.service;

import com.example.dto.LiveGoldPriceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GoldPriceSchedulerService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.gold-api.com/price/XAU"; // Replace with the actual API endpoint

    private double livePrice; // Field to store the current price
    private LocalDateTime updatedAt; // Field to store the last update time in UTC

    // Thread pool for asynchronous tasks
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    public GoldPriceSchedulerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Scheduled task to fetch the live gold price every 5 minutes asynchronously.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void updateLivePrice() {
        CompletableFuture.runAsync(() -> {
            try {
                // Fetch the live price from the external API
                ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // Parse the response body
                    String responseBody = response.getBody();

                    // Extract the price and updatedAt from the JSON response
                    livePrice = extractPrice(responseBody);
                    updatedAt = extractUpdatedAt(responseBody);

                    System.out.println("Updated live gold price: " + livePrice + ", updatedAt: " + updatedAt);
                } else {
                    System.err.println("Failed to update live gold price. Status: " + response.getStatusCode());
                }
            } catch (Exception e) {
                System.err.println("Error updating live gold price: " + e.getMessage());
                e.printStackTrace();
            }
        }, executorService);
    }

    /**
     * Extracts the price from the API response.
     */
    private double extractPrice(String responseBody) {
        // Implement JSON parsing to extract "price"
        // Assuming the API returns: {"price": 1925.50, "updatedAt": "2025-01-19T12:30:00Z"}
        return Double.parseDouble(responseBody.split("\"price\":")[1].split(",")[0]);
    }

    /**
     * Extracts the updatedAt timestamp from the API response.
     */
    private LocalDateTime extractUpdatedAt(String responseBody) {
        // Extract the updatedAt string from the response
        String updatedAtString = responseBody.split("\"updatedAt\":")[1].split("\"")[1];

        // Use DateTimeFormatter.ISO_DATE_TIME to handle the 'Z' at the end of the timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        return LocalDateTime.parse(updatedAtString, formatter); // Parse the string with the correct formatter
    }

    /**
     * Converts the UTC timestamp to New York time.
     *
     * @param utcTime The UTC time to convert.
     * @return The New York timestamp as a ZonedDateTime.
     */
    private ZonedDateTime convertToNewYorkTime(LocalDateTime utcTime) {
        return utcTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("America/New_York"));
    }

    /**
     * Get the latest live price, updatedAt timestamp in UTC, and the New York time.
     *
     * @return a DTO containing the latest price and timestamps.
     */
    public LiveGoldPriceDTO getLivePriceDetails() {
        CompletableFuture<LiveGoldPriceDTO> livePriceFuture = CompletableFuture.supplyAsync(() -> {
            if (updatedAt != null) {
                ZonedDateTime newYorkTime = convertToNewYorkTime(updatedAt);
                return new LiveGoldPriceDTO(livePrice, updatedAt, newYorkTime);
            } else {
                return null;
            }
        }, executorService);

        try {
            // Wait for the result and return it
            return livePriceFuture.get();
        } catch (Exception e) {
            System.err.println("Error fetching live gold price details: " + e.getMessage());
            return null;
        }
    }

    /**
     * Shutdown the thread pool when the application stops.
     */
    public void shutdownExecutor() {
        executorService.shutdown();
        System.out.println("Executor service shut down.");
    }
}
