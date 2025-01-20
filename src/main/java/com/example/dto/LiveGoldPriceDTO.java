package com.example.dto;

import java.time.LocalDateTime;

public class LiveGoldPriceDTO {

    private double price;
    private LocalDateTime updatedAt;

    public LiveGoldPriceDTO(double price, LocalDateTime updatedAt) {
        this.price = price;
        this.updatedAt = updatedAt;
    }

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
}
