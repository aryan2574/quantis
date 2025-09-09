package com.quantis.portfolio_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Portfolio Service.
 * 
 * This service provides:
 * - gRPC communication for fast inter-service communication
 * - Portfolio management (positions, cash balance, trades)
 * - Real-time portfolio valuation
 * - Trading history tracking
 * 
 * Key Features:
 * - High-performance gRPC API for Risk Service integration
 * - PostgreSQL for persistent data storage
 * - Redis for caching frequently accessed data
 * - Comprehensive portfolio tracking and analytics
 */
@SpringBootApplication
public class PortfolioServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortfolioServiceApplication.class, args);
    }
}
