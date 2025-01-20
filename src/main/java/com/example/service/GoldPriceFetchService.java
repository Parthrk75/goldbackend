package com.example.service;

import com.example.dto.GoldPriceDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoldPriceFetchService {

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.gold-api.com/price/XAU";
    private final List<GoldPriceDTO> historicalData = new ArrayList<>();
    private GoldPriceDTO livePrice;
    private LocalDate lastResetDate = LocalDate.now(); // Track the last reset date

    public GoldPriceFetchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch gold price every 1 hour.
     */
    @Scheduled(fixedRate = 3600000) // Every 1 hour
    public void fetchGoldPrice() {
        try {
            ResponseEntity<GoldPriceDTO> response = restTemplate.getForEntity(apiUrl, GoldPriceDTO.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GoldPriceDTO goldPrice = response.getBody();
                goldPrice.setTimestamp(LocalDateTime.now()); // Add a timestamp if not provided by the API
                synchronized (historicalData) {
                    historicalData.add(goldPrice);
                }
                livePrice = goldPrice;
                System.out.println("Fetched gold price: " + goldPrice);
            } else {
                System.err.println("Failed to fetch gold price. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.err.println("Error fetching gold price: " + e.getMessage());
        }
    }

    /**
     * Reset the historical data automatically at 00:00 every day.
     */
    @Scheduled(cron = "0 0 0 * * ?") // At 00:00 every day
    public void resetAtMidnight() {
        LocalDate currentDate = LocalDate.now();
        if (!currentDate.equals(lastResetDate)) {
            synchronized (historicalData) {
                historicalData.clear();
            }
            lastResetDate = currentDate;
            System.out.println("Historical data reset at midnight. New date: " + currentDate);
            fetchGoldPrice(); // Fetch fresh data immediately after reset
        }
    }

    /**
     * Manually reset the historical data.
     */
    public void resetHistoricalData() {
        synchronized (historicalData) {
            historicalData.clear();
        }
        System.out.println("Historical data reset manually.");
    }

    public GoldPriceDTO getLivePrice() {
        return livePrice;
    }

    public List<GoldPriceDTO> getHistoricalData() {
        return historicalData;
    }
}
