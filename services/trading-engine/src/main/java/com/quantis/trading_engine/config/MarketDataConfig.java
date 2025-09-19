package com.quantis.trading_engine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration properties for market data services.
 * This class maps the market-data.* properties from application.yml
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "market-data")
public class MarketDataConfig {
    
    private AlphaVantage alphaVantage = new AlphaVantage();
    
    @Data
    public static class AlphaVantage {
        private String apiKey;
        private String baseUrl;
        private List<String> symbols;
        private int updateIntervalMs;
    }
}
