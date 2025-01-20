package com.example.dto;

import java.time.LocalDateTime;

public class GoldPriceDTO {
    private double price;
    private String currency;
    private LocalDateTime timestamp;

    // Getters and setters
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "GoldPriceDTO{" +
                "price=" + price +
                ", currency='" + currency + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
