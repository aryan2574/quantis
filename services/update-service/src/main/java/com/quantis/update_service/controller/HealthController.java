package com.quantis.update_service.controller;

import com.quantis.update_service.service.OrderBookUpdateService;
import com.quantis.update_service.service.TradeConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Health and monitoring controller for the Update Service.
 * 
 * Provides endpoints for:
 * 1. Health checks
 * 2. Performance metrics
 * 3. Order book state inspection
 * 4. System status monitoring
 */
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Slf4j
public class HealthController {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final TradeConsumer tradeConsumer;
    private final OrderBookUpdateService orderBookUpdateService;
    
    /**
     * Basic health check endpoint
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        boolean redisConnected = checkRedisConnection();
        
        Map<String, Object> health = Map.of(
            "status", redisConnected ? "UP" : "DOWN",
            "service", "update-service",
            "timestamp", System.currentTimeMillis(),
            "redis", redisConnected ? "UP" : "DOWN"
        );
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Detailed health check with metrics
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealth() {
        boolean redisConnected = checkRedisConnection();
        
        TradeConsumer.ProcessingMetrics processingMetrics = tradeConsumer.getProcessingMetrics();
        OrderBookUpdateService.ProcessingStats processingStats = orderBookUpdateService.getProcessingStats();
        
        Map<String, Object> health = Map.of(
            "status", redisConnected ? "UP" : "DOWN",
            "service", "update-service",
            "timestamp", System.currentTimeMillis(),
            "redis", redisConnected ? "UP" : "DOWN",
            "processing", Map.of(
                "totalTradesProcessed", processingMetrics.getTotalTradesProcessed(),
                "failedTradesProcessed", processingMetrics.getFailedTradesProcessed(),
                "successRate", processingMetrics.getSuccessRate(),
                "lastProcessedTimestamp", processingMetrics.getLastProcessedTimestamp(),
                "currentSequenceNumber", processingStats.getCurrentSequenceNumber()
            )
        );
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get order book snapshot for a symbol
     */
    @GetMapping("/orderbook/{symbol}")
    public ResponseEntity<?> getOrderBook(@PathVariable String symbol) {
        try {
            var orderBook = orderBookUpdateService.getCurrentOrderBook(symbol);
            return ResponseEntity.ok(orderBook);
        } catch (Exception e) {
            log.error("Failed to get order book for symbol {}", symbol, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get order book for symbol " + symbol));
        }
    }
    
    /**
     * Get recent trades for a symbol
     */
    @GetMapping("/trades/{symbol}")
    public ResponseEntity<?> getRecentTrades(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            var trades = orderBookUpdateService.getRecentTrades(symbol, limit);
            return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "trades", trades,
                "count", trades.size()
            ));
        } catch (Exception e) {
            log.error("Failed to get recent trades for symbol {}", symbol, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get recent trades for symbol " + symbol));
        }
    }
    
    /**
     * Get market data for a symbol
     */
    @GetMapping("/marketdata/{symbol}")
    public ResponseEntity<?> getMarketData(@PathVariable String symbol) {
        try {
            var marketData = orderBookUpdateService.getMarketData(symbol);
            return ResponseEntity.ok(Map.of(
                "symbol", symbol,
                "data", marketData
            ));
        } catch (Exception e) {
            log.error("Failed to get market data for symbol {}", symbol, e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get market data for symbol " + symbol));
        }
    }
    
    /**
     * Get processing metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        TradeConsumer.ProcessingMetrics processingMetrics = tradeConsumer.getProcessingMetrics();
        OrderBookUpdateService.ProcessingStats processingStats = orderBookUpdateService.getProcessingStats();
        
        Map<String, Object> metrics = Map.of(
            "timestamp", System.currentTimeMillis(),
            "processing", processingMetrics,
            "orderBook", processingStats
        );
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Check Redis connection
     */
    private boolean checkRedisConnection() {
        try {
            redisTemplate.opsForValue().get("health-check");
            return true;
        } catch (Exception e) {
            log.warn("Redis connection check failed", e);
            return false;
        }
    }
}
