package com.example.service;

import com.example.dto.LiveGoldPriceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GoldPriceSchedulerService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.gold-api.com/price/XAU"; // Replace with the actual API endpoint

    private double livePrice; // Field to store the current price
    private LocalDateTime updatedAt; // Field to store the last update time

    public GoldPriceSchedulerService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Scheduled task to fetch the live gold price every 5 minutes.
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void updateLivePrice() {
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
     * Get the latest live price and updatedAt timestamp.
     *
     * @return a formatted string with the latest price and timestamp
     */
    public LiveGoldPriceDTO getLivePriceDetails() {
        if (updatedAt != null) {
            return new LiveGoldPriceDTO(livePrice, updatedAt);
        } else {
            return null; // Return null if price or updatedAt are unavailable
        }
    }
}
