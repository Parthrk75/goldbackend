package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.*;
import java.nio.file.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@EnableScheduling
public class GoldPriceManagementApplication implements CommandLineRunner {

	private static final String FILE_NAME = "historical_gold_spot_prices.csv";  // CSV File Name
	private static final String EXTERNAL_PATH = "./data/" + FILE_NAME;  // External Path

	public static void main(String[] args) {
		SpringApplication.run(GoldPriceManagementApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Ensure external directory exists
		Files.createDirectories(Paths.get("./data"));

		// Copy CSV file from resources to external location if it doesn't exist
		File externalFile = new File(EXTERNAL_PATH);
		if (!externalFile.exists()) {
			try (InputStream is = getClass().getClassLoader().getResourceAsStream(FILE_NAME);
				 OutputStream os = new FileOutputStream(EXTERNAL_PATH)) {
				if (is == null) {
					System.err.println("CSV file not found in resources!");
					return;
				}
				byte[] buffer = new byte[1024];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
				System.out.println("CSV file copied to: " + EXTERNAL_PATH);
			}
		}
	}
}
