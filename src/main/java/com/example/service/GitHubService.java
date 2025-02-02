package com.example.service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitHubService {
    private static final String RAW_CSV_URL = "https://raw.githubusercontent.com/Parthrk75/gold-price-data/main/gold_prices.csv";

    public List<String[]> getGoldPrices() {
        List<String[]> data = new ArrayList<>();
        try {
            URL url = new URL(RAW_CSV_URL);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

            String line;
            while ((line = br.readLine()) != null) {
                data.add(line.split(","));
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    // New method to get column headers
    public String[] getHeaders() {
        try {
            URL url = new URL(RAW_CSV_URL);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));

            String headerLine = br.readLine(); // Read only the first line (header)
            br.close();

            if (headerLine != null) {
                return headerLine.split(",");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[]{"No headers found"};
    }
}
