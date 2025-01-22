package com.example.service;

import com.example.dto.GoldPriceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
     * Fetch gold price every 2 minutes.
     */
    @Scheduled(fixedRate = 120000) // Every 2 minutes
    public void fetchGoldPrice() {
        try {
            logger.info("Fetching gold price from API: {}", apiUrl);
            ResponseEntity<GoldPriceDTO> response = restTemplate.getForEntity(apiUrl, GoldPriceDTO.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                GoldPriceDTO goldPrice = response.getBody();

                // Get New York time
                ZonedDateTime newYorkTime = ZonedDateTime.now(java.time.ZoneId.of("America/New_York"));
                String formattedNewYorkTime = newYorkTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"));

                // Set the New York time in the gold price DTO
                goldPrice.setTimestamp(newYorkTime.toLocalDateTime());

                // Log gold price and New York time
                logger.info("Fetched gold price: {} USD", goldPrice.getPrice());
                logger.info("Live New York time: {}", formattedNewYorkTime);

                // Update live price and add to historical data
                livePrice = goldPrice;
                historicalData.add(goldPrice);

                // Limit historical data to the latest 24 entries
                if (historicalData.size() > 24) {
                    historicalData.remove(0);
                }
            } else {
                logger.error("Failed to fetch gold price. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            logger.error("Error fetching gold price: {}", e.getMessage());
        }
    }

    /**
     * Get the live gold price.
     *
     * @return the live price as GoldPriceDTO
     */
    public GoldPriceDTO getLivePrice() {
        return livePrice;
    }

    /**
     * Get historical gold price data.
     *
     * @return a synchronized list of historical gold prices
     */
    public List<GoldPriceDTO> getHistoricalData() {
        return new ArrayList<>(historicalData); // Return a copy of the list to avoid modification
    }
}
