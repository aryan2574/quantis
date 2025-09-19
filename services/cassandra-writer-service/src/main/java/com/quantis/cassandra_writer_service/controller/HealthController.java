package com.quantis.cassandra_writer_service.controller;

import com.quantis.cassandra_writer_service.service.TradeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health and monitoring controller for the Cassandra Writer Service.
 * 
 * Provides endpoints for health checks, metrics, and service status.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {
    
    private final CassandraTemplate cassandraTemplate;
    private final TradeConsumer tradeConsumer;
    
    /**
     * Basic health check
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean cassandraHealthy = checkCassandraConnection();
            
            health.put("status", cassandraHealthy ? "UP" : "DOWN");
            health.put("service", "cassandra-writer-service");
            health.put("cassandra", cassandraHealthy ? "UP" : "DOWN");
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }
    
    /**
     * Detailed health information
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Service status
            health.put("status", "UP");
            health.put("service", "cassandra-writer-service");
            health.put("timestamp", System.currentTimeMillis());
            
            // Cassandra connection
            boolean cassandraHealthy = checkCassandraConnection();
            health.put("cassandra", Map.of(
                "status", cassandraHealthy ? "UP" : "DOWN",
                "keyspace", "quantis_trading",
                "tables", cassandraHealthy ? getTableInfo() : "N/A"
            ));
            
            // Processing metrics
            TradeConsumer.ProcessingMetrics metrics = tradeConsumer.getProcessingMetrics();
            health.put("processing", Map.of(
                "totalTradesProcessed", metrics.getTotalTradesProcessed(),
                "failedTradesProcessed", metrics.getFailedTradesProcessed(),
                "successRate", metrics.getSuccessRate(),
                "lastProcessedTimestamp", metrics.getLastProcessedTimestamp()
            ));
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Detailed health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(500).body(health);
        }
    }
    
    /**
     * Processing metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            TradeConsumer.ProcessingMetrics processingMetrics = tradeConsumer.getProcessingMetrics();
            metrics.putAll(Map.of(
                "totalTradesProcessed", processingMetrics.getTotalTradesProcessed(),
                "failedTradesProcessed", processingMetrics.getFailedTradesProcessed(),
                "successRate", processingMetrics.getSuccessRate(),
                "lastProcessedTimestamp", processingMetrics.getLastProcessedTimestamp(),
                "timestamp", System.currentTimeMillis()
            ));
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            log.error("Error getting metrics", e);
            metrics.put("error", e.getMessage());
            return ResponseEntity.status(500).body(metrics);
        }
    }
    
    /**
     * Cassandra connection status
     */
    @GetMapping("/cassandra")
    public ResponseEntity<Map<String, Object>> getCassandraStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean healthy = checkCassandraConnection();
            
            status.put("status", healthy ? "UP" : "DOWN");
            status.put("keyspace", "quantis_trading");
            
            if (healthy) {
                status.put("tables", getTableInfo());
                status.put("connection", "Active");
            } else {
                status.put("connection", "Failed");
            }
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error checking Cassandra status", e);
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }
    
    /**
     * Check Cassandra connection
     */
    private boolean checkCassandraConnection() {
        try {
            // Simple query to check connection
            cassandraTemplate.getCqlOperations().queryForObject("SELECT now() FROM system.local", String.class);
            return true;
        } catch (Exception e) {
            log.warn("Cassandra connection check failed", e);
            return false;
        }
    }
    
    /**
     * Get table information
     */
    private Map<String, Object> getTableInfo() {
        try {
            // Get table count
            Long tradeEventsCount = cassandraTemplate.getCqlOperations()
                    .queryForObject("SELECT COUNT(*) FROM trade_events", Long.class);
            Long tradeSummariesCount = cassandraTemplate.getCqlOperations()
                    .queryForObject("SELECT COUNT(*) FROM trade_summaries", Long.class);
            
            return Map.of(
                "trade_events", tradeEventsCount != null ? tradeEventsCount : 0,
                "trade_summaries", tradeSummariesCount != null ? tradeSummariesCount : 0
            );
        } catch (Exception e) {
            log.warn("Error getting table info", e);
            return Map.of("error", "Unable to retrieve table information");
        }
    }
}
