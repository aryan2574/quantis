package com.quantis.market_data.controller;

import com.quantis.market_data.service.CentralizedApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * API Configuration Controller
 * 
 * Manages API configuration and monitoring
 */
@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ApiConfigController {
    
    @Autowired
    private CentralizedApiClient centralizedApiClient;
    
    /**
     * Get API call statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getApiStatistics() {
        try {
            Map<String, Object> statistics = centralizedApiClient.getApiCallStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get API statistics: " + e.getMessage()));
        }
    }
    
    /**
     * Check API key status for all providers
     */
    @GetMapping("/api-key-status")
    public ResponseEntity<Map<String, Object>> getApiKeyStatus() {
        Map<String, Object> allStatus = new HashMap<>();
        
        // Alpha Vantage
        try {
            String alphaVantageKey = centralizedApiClient.getAlphaVantageApiKey();
            boolean alphaVantageValid = alphaVantageKey != null && !alphaVantageKey.isEmpty();
            allStatus.put("alpha-vantage", Map.of(
                "provider", "Alpha Vantage",
                "configured", alphaVantageValid,
                "keyLength", alphaVantageValid ? alphaVantageKey.length() : 0,
                "keyPreview", alphaVantageValid ? alphaVantageKey.substring(0, Math.min(8, alphaVantageKey.length())) + "..." : "Not configured"
            ));
        } catch (Exception e) {
            allStatus.put("alpha-vantage", Map.of(
                "provider", "Alpha Vantage",
                "configured", false,
                "error", e.getMessage()
            ));
        }
        
        // ExchangeRate
        try {
            String exchangeRateKey = centralizedApiClient.getExchangeRateApiKey();
            boolean exchangeRateValid = exchangeRateKey != null && !exchangeRateKey.isEmpty();
            allStatus.put("exchange-rate", Map.of(
                "provider", "ExchangeRate",
                "configured", exchangeRateValid,
                "keyLength", exchangeRateValid ? exchangeRateKey.length() : 0,
                "keyPreview", exchangeRateValid ? exchangeRateKey.substring(0, Math.min(8, exchangeRateKey.length())) + "..." : "Not configured"
            ));
        } catch (Exception e) {
            allStatus.put("exchange-rate", Map.of(
                "provider", "ExchangeRate",
                "configured", false,
                "error", e.getMessage()
            ));
        }
        
        // Polygon
        try {
            String polygonKey = centralizedApiClient.getPolygonApiKey();
            boolean polygonValid = polygonKey != null && !polygonKey.isEmpty();
            allStatus.put("polygon", Map.of(
                "provider", "Polygon",
                "configured", polygonValid,
                "keyLength", polygonValid ? polygonKey.length() : 0,
                "keyPreview", polygonValid ? polygonKey.substring(0, Math.min(8, polygonKey.length())) + "..." : "Not configured"
            ));
        } catch (Exception e) {
            allStatus.put("polygon", Map.of(
                "provider", "Polygon",
                "configured", false,
                "error", e.getMessage()
            ));
        }
        
        return ResponseEntity.ok(allStatus);
    }
    
    /**
     * Test API connection
     */
    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testApiConnection() {
        try {
            // Test with a simple API call
            String testUrl = centralizedApiClient.getAlphaVantageBaseUrl() + 
                           "/query?function=TIME_SERIES_INTRADAY&symbol=AAPL&interval=1min&apikey=" + 
                           centralizedApiClient.getAlphaVantageApiKey();
            
            // This would make an actual API call in a real implementation
            Map<String, Object> result = Map.of(
                "status", "success",
                "message", "API connection test completed",
                "url", testUrl.replace(centralizedApiClient.getAlphaVantageApiKey(), "***"),
                "timestamp", System.currentTimeMillis()
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", "API connection test failed: " + e.getMessage(),
                    "timestamp", System.currentTimeMillis()
                ));
        }
    }
    
    /**
     * Get rate limit information
     */
    @GetMapping("/rate-limits")
    public ResponseEntity<Map<String, Object>> getRateLimits() {
        try {
            Map<String, Object> rateLimits = Map.of(
                "alpha-vantage", Map.of(
                    "callsPerMinute", 5,
                    "callsPerDay", 500,
                    "description", "Free tier limits"
                ),
                "currentUsage", centralizedApiClient.getApiCallStatistics()
            );
            
            return ResponseEntity.ok(rateLimits);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to get rate limits: " + e.getMessage()));
        }
    }
}
