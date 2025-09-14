package com.quantis.market_data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Market Data Service Application
 * 
 * Provides real-time market data streaming to clients via:
 * - WebSockets (for retail trader dashboards)
 * - GraphQL (for historical queries & analytics)
 * - gRPC streaming (for algo clients)
 */
@SpringBootApplication
public class MarketDataServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataServiceApplication.class, args);
    }
}
