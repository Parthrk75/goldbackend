package com.example;

import com.example.dto.GoldCsvDTO;
import com.example.service.GoldPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class GoldPriceManagementApplicationTests {

    @Autowired
    private GoldPriceService goldPriceService;

    @Test
    void contextLoads() {
        // Test context loading
    }

    @Test
    void testGetEntriesByDays() {
        int days = 5; // Example input
        List<GoldCsvDTO> entries = goldPriceService.getEntriesByDays(days);
        assertNotNull(entries, "Entries should not be null");
        assertTrue(entries.size() > 0, "Entries list should not be empty for a valid 'days' input");
    }
}
