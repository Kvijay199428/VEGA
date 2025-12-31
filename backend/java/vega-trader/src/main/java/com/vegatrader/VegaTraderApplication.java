package com.vegatrader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * VEGA Trader Backend Application
 * 
 * Complete trading platform with:
 * - User authentication (JWT)
 * - Upstox API integration
 * - Real-time market data via WebSocket
 * - Option chain with valuations
 * - Order management
 * - Portfolio tracking
 * - Trading strategies
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class VegaTraderApplication {

    @org.springframework.context.annotation.Bean
    public okhttp3.OkHttpClient okHttpClient() {
        return new okhttp3.OkHttpClient();
    }

    public static void main(String[] args) {
        SpringApplication.run(VegaTraderApplication.class, args);
    }
}
