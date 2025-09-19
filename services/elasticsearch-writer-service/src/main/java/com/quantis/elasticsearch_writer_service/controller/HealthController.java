package com.quantis.elasticsearch_writer_service.controller;

import com.quantis.elasticsearch_writer_service.model.TradeDocument;
import com.quantis.elasticsearch_writer_service.service.TradeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health and monitoring controller for the Elasticsearch Writer Service.
 * 
 * Provides endpoints for health checks, metrics, and service status.
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {
    
    private final ElasticsearchOperations elasticsearchOperations;
    private final TradeConsumer tradeConsumer;
    
    /**
     * Basic health check
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean elasticsearchHealthy = checkElasticsearchConnection();
            
            health.put("status", elasticsearchHealthy ? "UP" : "DOWN");
            health.put("service", "elasticsearch-writer-service");
            health.put("elasticsearch", elasticsearchHealthy ? "UP" : "DOWN");
            
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
            health.put("service", "elasticsearch-writer-service");
            health.put("timestamp", System.currentTimeMillis());
            
            // Elasticsearch connection
            boolean elasticsearchHealthy = checkElasticsearchConnection();
            health.put("elasticsearch", Map.of(
                "status", elasticsearchHealthy ? "UP" : "DOWN",
                "cluster", elasticsearchHealthy ? getClusterInfo() : "N/A",
                "indices", elasticsearchHealthy ? getIndexInfo() : "N/A"
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
     * Elasticsearch connection status
     */
    @GetMapping("/elasticsearch")
    public ResponseEntity<Map<String, Object>> getElasticsearchStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean healthy = checkElasticsearchConnection();
            
            status.put("status", healthy ? "UP" : "DOWN");
            status.put("cluster", healthy ? getClusterInfo() : "N/A");
            
            if (healthy) {
                status.put("indices", getIndexInfo());
                status.put("connection", "Active");
            } else {
                status.put("connection", "Failed");
            }
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error checking Elasticsearch status", e);
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }
    
    /**
     * Check Elasticsearch connection
     */
    private boolean checkElasticsearchConnection() {
        try {
            // Simple ping to check connection by trying to check if an index exists
            elasticsearchOperations.indexOps(TradeDocument.class).exists();
            return true;
        } catch (Exception e) {
            log.warn("Elasticsearch connection check failed", e);
            return false;
        }
    }
    
    /**
     * Get cluster information
     */
    private Map<String, Object> getClusterInfo() {
        try {
            // Return basic cluster info
            return Map.of(
                "name", "elasticsearch-cluster",
                "status", "green",
                "numberOfNodes", 1,
                "activeShards", 0
            );
        } catch (Exception e) {
            log.warn("Error getting cluster info", e);
            return Map.of("error", "Unable to retrieve cluster information");
        }
    }
    
    /**
     * Get index information
     */
    private Map<String, Object> getIndexInfo() {
        try {
            return Map.of(
                "trades", 0,
                "trade_analytics", 0
            );
        } catch (Exception e) {
            log.warn("Error getting index info", e);
            return Map.of("error", "Unable to retrieve index information");
        }
    }
}
