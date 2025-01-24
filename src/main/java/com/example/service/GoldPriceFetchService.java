package com.example.service;

import com.example.dto.GoldPriceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GoldPriceFetchService {

    private static final Logger logger = LoggerFactory.getLogger(GoldPriceFetchService.class);

    private final RestTemplate restTemplate;
    private final String apiUrl = "https://api.gold-api.com/price/XAU";

    private GoldPriceDTO livePrice; // For storing the latest gold price
    private final List<GoldPriceDTO> historicalData = Collections.synchronizedList(new ArrayList<>());

    public GoldPriceFetchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch gold price every 1 hour at 2 minutes past the hour.
     */
    @Scheduled(fixedRate = 3600000) // Every hour at HH:02
    public void fetchGoldPrice() {
        try {
            System.out.println("[DEBUG] Starting fetchGoldPrice...");
            logger.info("Fetching gold price from API: {}", apiUrl);
            ResponseEntity<GoldPriceDTO> response = restTemplate.getForEntity(apiUrl, GoldPriceDTO.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("[DEBUG] API response received successfully.");
                GoldPriceDTO goldPrice = response.getBody();

                // Get New York time
                ZonedDateTime newYorkTime = ZonedDateTime.now(java.time.ZoneId.of("America/New_York"));
                String formattedNewYorkTime = newYorkTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

                // Set the New York time in the gold price DTO
                goldPrice.setTimestamp(newYorkTime.toLocalDateTime());

                // Log gold price and New York time
                logger.info("Fetched gold price: {} USD", goldPrice.getPrice());
                logger.info("Live New York time: {}", formattedNewYorkTime);

                System.out.println("[DEBUG] Fetched gold price: " + goldPrice.getPrice() + " USD");
                System.out.println("[DEBUG] Live New York time: " + formattedNewYorkTime);

                // Update live price and add to historical data
                livePrice = goldPrice;
                historicalData.add(goldPrice);

                System.out.println("[DEBUG] Live price updated and added to historical data.");
            } else {
                logger.error("Failed to fetch gold price. Status: {}", response.getStatusCode());
                System.out.println("[ERROR] Failed to fetch gold price. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error fetching gold price: {}", e.getMessage());
            System.out.println("[ERROR] Exception while fetching gold price: " + e.getMessage());
        }
    }

    /**
     * Clear historical data at 00:00 New York time daily.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "America/New_York") // Every day at 00:00 New York time
    public void clearHistoricalDataAtMidnight() {
        System.out.println("[DEBUG] Clearing historical data at 00:00 New York time...");
        logger.info("Clearing historical data at 00:00 New York time...");
        historicalData.clear();
    }

    /**
     * Get the live gold price.
     *
     * @return the live price as GoldPriceDTO
     */
    public GoldPriceDTO getLivePrice() {
        System.out.println("[DEBUG] getLivePrice called. Returning live price: " + (livePrice != null ? livePrice.getPrice() : "null"));
        return livePrice;
    }

    /**
     * Get historical gold price data.
     *
     * @return a synchronized list of historical gold prices
     */
    public List<GoldPriceDTO> getHistoricalData() {
        System.out.println("[DEBUG] getHistoricalData called. Returning historical data of size: " + historicalData.size());
        return new ArrayList<>(historicalData); // Return a copy of the list to avoid modification
    }
}
