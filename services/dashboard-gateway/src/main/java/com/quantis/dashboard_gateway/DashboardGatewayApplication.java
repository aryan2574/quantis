package com.quantis.dashboard_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * GraphQL Gateway Service for Trading Dashboard
 * 
 * This service provides a unified GraphQL API for the trading dashboard,
 * aggregating data from all microservices while maintaining high performance
 * for core trading operations.
 * 
 * Architecture:
 * - GraphQL Gateway (this service) - Dashboard aggregation layer
 * - Portfolio Service (gRPC) - Keep for speed
 * - Market Data Service (GraphQL) - Already exists
 * - Order Ingress Service (gRPC) - Keep for speed
 * - Analytics Services (REST) - Keep for speed
 */
@SpringBootApplication
@EnableCaching
public class DashboardGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DashboardGatewayApplication.class, args);
    }
}
