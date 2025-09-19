package com.quantis.postgres_writer_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main Spring Boot application class for the Postgres Writer Service.
 * 
 * This service is responsible for:
 * 1. Consuming trade executions from Kafka 'trades.out' topic
 * 2. Persisting trades, orders, and portfolio updates to PostgreSQL
 * 3. Maintaining ACID compliance for financial data
 * 4. Providing batch processing for high-volume writes
 * 5. Supporting analytics and reporting queries
 * 
 * Key Features:
 * - High-performance batch processing with JPA
 * - Connection pooling with HikariCP
 * - Transactional consistency for financial data
 * - Comprehensive audit trails
 * - Real-time and batch processing modes
 */
@SpringBootApplication
@EnableAsync
public class PostgresWriterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PostgresWriterServiceApplication.class, args);
    }
}
