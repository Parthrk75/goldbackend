package com.example.dto;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class LiveGoldPriceDTO {
    private double price;
    private LocalDateTime updatedAt; // UTC time
    private ZonedDateTime newYorkTime; // New York time

    public LiveGoldPriceDTO(double price, LocalDateTime updatedAt, ZonedDateTime newYorkTime) {
        this.price = price;
        this.updatedAt = updatedAt;
        this.newYorkTime = newYorkTime;
    }

    // Getters and setters
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getNewYorkTime() {
        return newYorkTime;
    }

    public void setNewYorkTime(ZonedDateTime newYorkTime) {
        this.newYorkTime = newYorkTime;
    }
}
