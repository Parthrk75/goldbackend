package com.example.dto;

import java.time.LocalDateTime;

public class GoldCsvDTO {
    private LocalDateTime timestamp; // Renamed to match the field in the service class
    private double price;

    // Constructor
    public GoldCsvDTO(LocalDateTime timestamp, double price) {
        this.timestamp = timestamp;
        this.price = price;
    }

    // Getters and setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "GoldCsvDTO{" +
                "timestamp=" + timestamp +
                ", price=" + price +
                '}';
    }
}
