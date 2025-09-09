package com.quantis.trading_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Trading Engine Service.
 * 
 * This service provides:
 * - Order execution engine
 * - Market simulation and matching
 * - Portfolio updates via gRPC
 * - Trade settlement and clearing
 * 
 * Key Features:
 * - Consumes orders.valid and orders.rejected from Kafka
 * - Executes valid orders against simulated market
 * - Updates portfolio positions via gRPC
 * - Publishes trade execution results
 * - Handles order rejections and notifications
 */
@SpringBootApplication
public class TradingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingEngineApplication.class, args);
    }
}
