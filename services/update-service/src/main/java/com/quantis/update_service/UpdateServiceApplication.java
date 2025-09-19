package com.quantis.update_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Update Service.
 * 
 * This service is responsible for:
 * 1. Consuming trade executions from Kafka 'trades.out' topic
 * 2. Updating Redis cache with latest order book snapshots
 * 3. Maintaining real-time market data for downstream consumers
 * 4. Providing order book state management for the trading platform
 * 
 * Key Features:
 * - High-performance Redis operations with connection pooling
 * - Lock-free order book updates using atomic operations
 * - Real-time market data distribution
 * - Comprehensive metrics and monitoring
 */
@SpringBootApplication
public class UpdateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpdateServiceApplication.class, args);
    }
}
