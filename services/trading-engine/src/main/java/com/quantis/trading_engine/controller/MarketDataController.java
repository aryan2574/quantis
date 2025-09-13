package com.quantis.trading_engine.controller;

import com.quantis.trading_engine.service.LockFreeMarketDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for market data endpoints
 */
@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "cpp.engine.enabled", havingValue = "true", matchIfMissing = false)
public class MarketDataController {
    
    private final LockFreeMarketDataService marketDataService;
    
    /**
     * Get market data for a specific symbol
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<LockFreeMarketDataService.MarketDataSnapshot> getMarketData(@PathVariable String symbol) {
        try {
            LockFreeMarketDataService.MarketDataSnapshot data = marketDataService.getMarketData(symbol);
            
            if (data != null) {
                return ResponseEntity.ok(data);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Error getting market data for symbol: {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all market data
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllMarketData() {
        try {
            // Get symbols from C++ service
            String[] symbols = marketDataService.getSymbols();
            Map<String, Object> data = Map.of(
                "symbols", symbols,
                "count", symbols.length,
                "service", "C++ Market Data Service"
            );
            return ResponseEntity.ok(data);
            
        } catch (Exception e) {
            log.error("Error getting all market data", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Add a new symbol to track
     */
    @PostMapping("/symbols/{symbol}")
    public ResponseEntity<String> addSymbol(@PathVariable String symbol) {
        try {
            boolean success = marketDataService.addSymbol(symbol);
            if (success) {
                return ResponseEntity.ok("Symbol " + symbol + " added to C++ service tracking");
            } else {
                return ResponseEntity.status(500).body("Failed to add symbol to C++ service");
            }
        } catch (Exception e) {
            log.error("Error adding symbol: {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Remove a symbol from tracking
     */
    @DeleteMapping("/symbols/{symbol}")
    public ResponseEntity<String> removeSymbol(@PathVariable String symbol) {
        try {
            boolean success = marketDataService.removeSymbol(symbol);
            if (success) {
                return ResponseEntity.ok("Symbol " + symbol + " removed from C++ service tracking");
            } else {
                return ResponseEntity.status(500).body("Failed to remove symbol from C++ service");
            }
        } catch (Exception e) {
            log.error("Error removing symbol: {}", symbol, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            Map<String, Object> status = Map.of(
                "status", "UP",
                "service", "market-data",
                "trackedSymbols", marketDataService.getSymbols().length
            );
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            log.error("Error in health check", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get performance metrics for the lock-free market data service
     */
    @GetMapping("/performance")
    public ResponseEntity<LockFreeMarketDataService.PerformanceMetrics> getPerformanceMetrics() {
        try {
            LockFreeMarketDataService.PerformanceMetrics metrics = marketDataService.getPerformanceMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            log.error("Error getting performance metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all symbols being tracked by C++ service
     */
    @GetMapping("/symbols")
    public ResponseEntity<String[]> getSymbols() {
        try {
            String[] symbols = marketDataService.getSymbols();
            return ResponseEntity.ok(symbols);
        } catch (Exception e) {
            log.error("Error getting symbols: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Set API key for C++ service
     */
    @PostMapping("/api-key")
    public ResponseEntity<String> setApiKey(@RequestParam String apiKey) {
        try {
            boolean success = marketDataService.setApiKey(apiKey);
            if (success) {
                return ResponseEntity.ok("API key updated successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to update API key");
            }
        } catch (Exception e) {
            log.error("Error setting API key: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Set update interval for C++ service
     */
    @PostMapping("/update-interval")
    public ResponseEntity<String> setUpdateInterval(@RequestParam long intervalMs) {
        try {
            boolean success = marketDataService.setUpdateInterval(intervalMs);
            if (success) {
                return ResponseEntity.ok("Update interval set to " + intervalMs + "ms");
            } else {
                return ResponseEntity.status(500).body("Failed to set update interval");
            }
        } catch (Exception e) {
            log.error("Error setting update interval: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Check C++ service health
     */
    @GetMapping("/cpp-health")
    public ResponseEntity<Map<String, Object>> getCppHealth() {
        try {
            boolean healthy = marketDataService.isHealthy();
            Map<String, Object> health = Map.of(
                "healthy", healthy,
                "service", "C++ Market Data Service",
                "timestamp", System.currentTimeMillis()
            );
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error checking C++ health: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Reset performance metrics
     */
    @PostMapping("/reset-metrics")
    public ResponseEntity<String> resetMetrics() {
        try {
            boolean success = marketDataService.resetMetrics();
            if (success) {
                return ResponseEntity.ok("Performance metrics reset successfully");
            } else {
                return ResponseEntity.status(500).body("Failed to reset metrics");
            }
        } catch (Exception e) {
            log.error("Error resetting metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
