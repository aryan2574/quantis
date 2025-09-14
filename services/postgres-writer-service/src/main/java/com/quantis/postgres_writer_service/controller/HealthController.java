package com.quantis.postgres_writer_service.controller;

import com.quantis.postgres_writer_service.service.TradeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * Health and monitoring controller for the Postgres Writer Service.
 * 
 * Provides endpoints for:
 * 1. Health checks
 * 2. Performance metrics
 * 3. Database connectivity
 * 4. System status monitoring
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {
    
    private final DataSource dataSource;
    private final TradeConsumer tradeConsumer;
    
    /**
     * Basic health check endpoint
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean databaseConnected = checkDatabaseConnection();
        
        Map<String, Object> health = Map.of(
            "status", databaseConnected ? "UP" : "DOWN",
            "service", "postgres-writer-service",
            "timestamp", System.currentTimeMillis(),
            "database", databaseConnected ? "UP" : "DOWN"
        );
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Detailed health check with metrics
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        boolean databaseConnected = checkDatabaseConnection();
        
        TradeConsumer.ProcessingMetrics processingMetrics = tradeConsumer.getProcessingMetrics();
        
        Map<String, Object> health = Map.of(
            "status", databaseConnected ? "UP" : "DOWN",
            "service", "postgres-writer-service",
            "timestamp", System.currentTimeMillis(),
            "database", databaseConnected ? "UP" : "DOWN",
            "processing", Map.of(
                "totalTradesProcessed", processingMetrics.getTotalTradesProcessed(),
                "failedTradesProcessed", processingMetrics.getFailedTradesProcessed(),
                "successRate", processingMetrics.getSuccessRate(),
                "lastProcessedTimestamp", processingMetrics.getLastProcessedTimestamp()
            )
        );
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get processing metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        TradeConsumer.ProcessingMetrics processingMetrics = tradeConsumer.getProcessingMetrics();
        
        Map<String, Object> metrics = Map.of(
            "timestamp", System.currentTimeMillis(),
            "processing", processingMetrics
        );
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Database connectivity test
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        boolean connected = checkDatabaseConnection();
        
        Map<String, Object> status = Map.of(
            "connected", connected,
            "timestamp", System.currentTimeMillis(),
            "status", connected ? "UP" : "DOWN"
        );
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Check database connection
     */
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (Exception e) {
            log.warn("Database connection check failed", e);
            return false;
        }
    }
}
