package com.quantis.market_data.service;

import com.quantis.market_data.config.ApiConfiguration;
import com.quantis.market_data.model.AssetType;
import com.quantis.market_data.model.Symbol;
import com.quantis.market_data.model.MarketData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Centralized API Client
 * 
 * Manages all API calls to external data providers from a single location
 */
@Service
public class CentralizedApiClient {
    
    @Autowired
    private ApiConfiguration apiConfiguration;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final Map<String, Integer> apiCallCounts = new HashMap<>();
    private final Map<String, Long> lastResetTime = new HashMap<>();
    
    /**
     * Get Alpha Vantage API key safely
     */
    public String getAlphaVantageApiKey() {
        String apiKey = apiConfiguration.getKeys().getAlphaVantage().getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Alpha Vantage API key is not configured");
        }
        return apiKey;
    }
    
    /**
     * Get Alpha Vantage base URL
     */
    public String getAlphaVantageBaseUrl() {
        return apiConfiguration.getConfig().getAlphaVantage().getBaseUrl();
    }
    
    /**
     * Get ExchangeRate API key safely
     */
    public String getExchangeRateApiKey() {
        String apiKey = apiConfiguration.getKeys().getExchangeRate().getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("ExchangeRate API key is not configured");
        }
        return apiKey;
    }
    
    /**
     * Get ExchangeRate base URL
     */
    public String getExchangeRateBaseUrl() {
        return apiConfiguration.getKeys().getExchangeRate().getBaseUrl();
    }
    
    /**
     * Get Polygon API key safely
     */
    public String getPolygonApiKey() {
        String apiKey = apiConfiguration.getKeys().getPolygon().getApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Polygon API key is not configured");
        }
        return apiKey;
    }
    
    /**
     * Get Polygon base URL
     */
    public String getPolygonBaseUrl() {
        return apiConfiguration.getKeys().getPolygon().getBaseUrl();
    }
    
    /**
     * Check rate limits before making API call
     */
    public boolean checkRateLimit(String provider) {
        String key = provider + "_minute";
        long currentTime = System.currentTimeMillis();
        
        // Reset counter every minute
        if (!lastResetTime.containsKey(key) || (currentTime - lastResetTime.get(key)) > 60000) {
            apiCallCounts.put(key, 0);
            lastResetTime.put(key, currentTime);
        }
        
        int currentCalls = apiCallCounts.getOrDefault(key, 0);
        int maxCalls = apiConfiguration.getKeys().getAlphaVantage().getRateLimit().getCallsPerMinute();
        
        return currentCalls < maxCalls;
    }
    
    /**
     * Increment API call counter
     */
    public void incrementApiCallCount(String provider) {
        String key = provider + "_minute";
        apiCallCounts.put(key, apiCallCounts.getOrDefault(key, 0) + 1);
    }
    
    /**
     * Get crypto market data from Alpha Vantage
     */
    public CompletableFuture<MarketData> getCryptoMarketData(Symbol symbol) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!checkRateLimit("alpha-vantage")) {
                    throw new RuntimeException("Rate limit exceeded for Alpha Vantage");
                }
                
                String baseCurrency = symbol.getSymbol().replace("USD", "").replace("-FUTURES", "");
                String endpoint = apiConfiguration.getConfig().getAlphaVantage().getEndpoints().getDigitalCurrency();
                String url = getAlphaVantageBaseUrl() + endpoint + 
                           "&symbol=" + baseCurrency + 
                           "&market=USD&interval=1min&apikey=" + getAlphaVantageApiKey();
                
                incrementApiCallCount("alpha-vantage");
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                return parseCryptoResponse(response, symbol);
                
            } catch (RestClientException e) {
                System.err.println("Error fetching crypto market data for " + symbol.getSymbol() + ": " + e.getMessage());
                return createEmptyMarketData(symbol);
            }
        });
    }
    
    /**
     * Get forex market data from Alpha Vantage
     */
    public CompletableFuture<MarketData> getForexMarketData(Symbol symbol) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!checkRateLimit("alpha-vantage")) {
                    throw new RuntimeException("Rate limit exceeded for Alpha Vantage");
                }
                
                String baseCurrency = symbol.getBaseCurrency();
                String quoteCurrency = symbol.getQuoteCurrency();
                String endpoint = apiConfiguration.getConfig().getAlphaVantage().getEndpoints().getForex();
                String url = getAlphaVantageBaseUrl() + endpoint + 
                           "&from_symbol=" + baseCurrency + 
                           "&to_symbol=" + quoteCurrency + 
                           "&interval=1min&apikey=" + getAlphaVantageApiKey();
                
                incrementApiCallCount("alpha-vantage");
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                return parseForexResponse(response, symbol);
                
            } catch (RestClientException e) {
                System.err.println("Error fetching forex market data for " + symbol.getSymbol() + ": " + e.getMessage());
                return createEmptyMarketData(symbol);
            }
        });
    }
    
    /**
     * Get stock market data from Alpha Vantage
     */
    public CompletableFuture<MarketData> getStockMarketData(Symbol symbol) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!checkRateLimit("alpha-vantage")) {
                    throw new RuntimeException("Rate limit exceeded for Alpha Vantage");
                }
                
                String endpoint = apiConfiguration.getConfig().getAlphaVantage().getEndpoints().getTimeSeries();
                String url = getAlphaVantageBaseUrl() + endpoint + 
                           "&symbol=" + symbol.getSymbol() + 
                           "&interval=1min&apikey=" + getAlphaVantageApiKey();
                
                incrementApiCallCount("alpha-vantage");
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                return parseStockResponse(response, symbol);
                
            } catch (RestClientException e) {
                System.err.println("Error fetching stock market data for " + symbol.getSymbol() + ": " + e.getMessage());
                return createEmptyMarketData(symbol);
            }
        });
    }
    
    /**
     * Get ExchangeRate market data
     */
    public CompletableFuture<MarketData> getExchangeRateMarketData(Symbol symbol) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!checkRateLimit("exchange-rate")) {
                    throw new RuntimeException("Rate limit exceeded for ExchangeRate");
                }
                
                String baseCurrency = symbol.getBaseCurrency();
                String url = getExchangeRateBaseUrl() + "/" + getExchangeRateApiKey() + 
                           apiConfiguration.getKeys().getExchangeRate().getEndpoints().getLatest() + "/" + baseCurrency;
                
                incrementApiCallCount("exchange-rate");
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                return parseExchangeRateResponse(response, symbol);
                
            } catch (RestClientException e) {
                System.err.println("Error fetching ExchangeRate market data for " + symbol.getSymbol() + ": " + e.getMessage());
                return createEmptyMarketData(symbol);
            }
        });
    }
    
    /**
     * Get Polygon market data
     */
    public CompletableFuture<MarketData> getPolygonMarketData(Symbol symbol) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!checkRateLimit("polygon")) {
                    throw new RuntimeException("Rate limit exceeded for Polygon");
                }
                
                String endpoint = apiConfiguration.getKeys().getPolygon().getEndpoints().getStocks();
                String url = getPolygonBaseUrl() + endpoint + "/" + symbol.getSymbol() + 
                           "/prev?apikey=" + getPolygonApiKey();
                
                incrementApiCallCount("polygon");
                
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);
                return parsePolygonResponse(response, symbol);
                
            } catch (RestClientException e) {
                System.err.println("Error fetching Polygon market data for " + symbol.getSymbol() + ": " + e.getMessage());
                return createEmptyMarketData(symbol);
            }
        });
    }
    
    /**
     * Get batch market data for multiple symbols
     */
    public CompletableFuture<List<MarketData>> getBatchMarketData(List<Symbol> symbols) {
        List<CompletableFuture<MarketData>> futures = new ArrayList<>();
        
        for (Symbol symbol : symbols) {
            CompletableFuture<MarketData> future;
            
            switch (symbol.getAssetType()) {
                case CRYPTO_SPOT:
                case CRYPTO_FUTURES:
                case CRYPTO_PERPETUAL:
                case CRYPTO_OPTIONS:
                    future = getCryptoMarketData(symbol);
                    break;
                case FOREX:
                    // Use ExchangeRate for forex data
                    future = getExchangeRateMarketData(symbol);
                    break;
                case STOCKS:
                case FUTURES:
                case OPTIONS:
                case ENTERPRISE_TOKENS:
                    // Use Polygon for stocks and other assets
                    future = getPolygonMarketData(symbol);
                    break;
                default:
                    future = CompletableFuture.completedFuture(createEmptyMarketData(symbol));
                    break;
            }
            
            futures.add(future);
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(java.util.stream.Collectors.toList()));
    }
    
    /**
     * Parse crypto response from Alpha Vantage
     */
    private MarketData parseCryptoResponse(Map<String, Object> response, Symbol symbol) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol.getSymbol());
        marketData.setAssetType(symbol.getAssetType());
        marketData.setTimestamp(LocalDateTime.now());
        
        // Check for API limit message
        if (response.containsKey("Note")) {
            System.err.println("Alpha Vantage API limit reached: " + response.get("Note"));
            return createEmptyMarketData(symbol);
        }
        
        // Check for error message
        if (response.containsKey("Error Message")) {
            System.err.println("Alpha Vantage API error: " + response.get("Error Message"));
            return createEmptyMarketData(symbol);
        }
        
        // Parse time series data
        if (response.containsKey("Time Series (Digital Currency Intraday)")) {
            Map<String, Object> timeSeries = (Map<String, Object>) response.get("Time Series (Digital Currency Intraday)");
            
            // Get the most recent data point
            String latestTime = timeSeries.keySet().stream().findFirst().orElse(null);
            if (latestTime != null) {
                Map<String, Object> latestData = (Map<String, Object>) timeSeries.get(latestTime);
                
                if (latestData.containsKey("1a. price (USD)")) {
                    marketData.setPrice(new BigDecimal(latestData.get("1a. price (USD)").toString()));
                }
                if (latestData.containsKey("2. volume")) {
                    marketData.setVolume(new BigDecimal(latestData.get("2. volume").toString()));
                }
                if (latestData.containsKey("3a. high (USD)")) {
                    marketData.setHigh(new BigDecimal(latestData.get("3a. high (USD)").toString()));
                }
                if (latestData.containsKey("3b. low (USD)")) {
                    marketData.setLow(new BigDecimal(latestData.get("3b. low (USD)").toString()));
                }
            }
        }
        
        return marketData;
    }
    
    /**
     * Parse forex response from Alpha Vantage
     */
    private MarketData parseForexResponse(Map<String, Object> response, Symbol symbol) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol.getSymbol());
        marketData.setAssetType(symbol.getAssetType());
        marketData.setTimestamp(LocalDateTime.now());
        
        // Check for API limit message
        if (response.containsKey("Note")) {
            System.err.println("Alpha Vantage API limit reached: " + response.get("Note"));
            return createEmptyMarketData(symbol);
        }
        
        // Check for error message
        if (response.containsKey("Error Message")) {
            System.err.println("Alpha Vantage API error: " + response.get("Error Message"));
            return createEmptyMarketData(symbol);
        }
        
        // Parse time series data
        if (response.containsKey("Time Series (FX)")) {
            Map<String, Object> timeSeries = (Map<String, Object>) response.get("Time Series (FX)");
            
            // Get the most recent data point
            String latestTime = timeSeries.keySet().stream().findFirst().orElse(null);
            if (latestTime != null) {
                Map<String, Object> latestData = (Map<String, Object>) timeSeries.get(latestTime);
                
                if (latestData.containsKey("1. open")) {
                    marketData.setPrice(new BigDecimal(latestData.get("1. open").toString()));
                }
                if (latestData.containsKey("2. high")) {
                    marketData.setHigh(new BigDecimal(latestData.get("2. high").toString()));
                }
                if (latestData.containsKey("3. low")) {
                    marketData.setLow(new BigDecimal(latestData.get("3. low").toString()));
                }
            }
        }
        
        return marketData;
    }
    
    /**
     * Parse stock response from Alpha Vantage
     */
    private MarketData parseStockResponse(Map<String, Object> response, Symbol symbol) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol.getSymbol());
        marketData.setAssetType(symbol.getAssetType());
        marketData.setTimestamp(LocalDateTime.now());
        
        // Check for API limit message
        if (response.containsKey("Note")) {
            System.err.println("Alpha Vantage API limit reached: " + response.get("Note"));
            return createEmptyMarketData(symbol);
        }
        
        // Check for error message
        if (response.containsKey("Error Message")) {
            System.err.println("Alpha Vantage API error: " + response.get("Error Message"));
            return createEmptyMarketData(symbol);
        }
        
        // Parse time series data
        if (response.containsKey("Time Series (1min)")) {
            Map<String, Object> timeSeries = (Map<String, Object>) response.get("Time Series (1min)");
            
            // Get the most recent data point
            String latestTime = timeSeries.keySet().stream().findFirst().orElse(null);
            if (latestTime != null) {
                Map<String, Object> latestData = (Map<String, Object>) timeSeries.get(latestTime);
                
                if (latestData.containsKey("4. close")) {
                    marketData.setPrice(new BigDecimal(latestData.get("4. close").toString()));
                }
                if (latestData.containsKey("5. volume")) {
                    marketData.setVolume(new BigDecimal(latestData.get("5. volume").toString()));
                }
                if (latestData.containsKey("2. high")) {
                    marketData.setHigh(new BigDecimal(latestData.get("2. high").toString()));
                }
                if (latestData.containsKey("3. low")) {
                    marketData.setLow(new BigDecimal(latestData.get("3. low").toString()));
                }
                if (latestData.containsKey("1. open")) {
                    BigDecimal open = new BigDecimal(latestData.get("1. open").toString());
                    BigDecimal close = marketData.getPrice();
                    marketData.setChange(close.subtract(open));
                    marketData.setChangePercent(open.compareTo(BigDecimal.ZERO) > 0 ? 
                        marketData.getChange().divide(open, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                        BigDecimal.ZERO);
                }
            }
        }
        
        return marketData;
    }
    
    /**
     * Parse ExchangeRate response
     */
    private MarketData parseExchangeRateResponse(Map<String, Object> response, Symbol symbol) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol.getSymbol());
        marketData.setAssetType(symbol.getAssetType());
        marketData.setTimestamp(LocalDateTime.now());
        
        // Check for error
        if (response.containsKey("error")) {
            System.err.println("ExchangeRate API error: " + response.get("error"));
            return createEmptyMarketData(symbol);
        }
        
        // Parse rates
        if (response.containsKey("rates")) {
            Map<String, Object> rates = (Map<String, Object>) response.get("rates");
            String quoteCurrency = symbol.getQuoteCurrency();
            
            if (rates.containsKey(quoteCurrency)) {
                BigDecimal rate = new BigDecimal(rates.get(quoteCurrency).toString());
                marketData.setPrice(rate);
            } else {
                marketData.setPrice(BigDecimal.ZERO);
            }
        }
        
        return marketData;
    }
    
    /**
     * Parse Polygon response
     */
    private MarketData parsePolygonResponse(Map<String, Object> response, Symbol symbol) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol.getSymbol());
        marketData.setAssetType(symbol.getAssetType());
        marketData.setTimestamp(LocalDateTime.now());
        
        // Check for error
        if (response.containsKey("error")) {
            System.err.println("Polygon API error: " + response.get("error"));
            return createEmptyMarketData(symbol);
        }
        
        // Parse results
        if (response.containsKey("results")) {
            Map<String, Object> results = (Map<String, Object>) response.get("results");
            
            if (results.containsKey("c")) {
                marketData.setPrice(new BigDecimal(results.get("c").toString()));
            }
            if (results.containsKey("v")) {
                marketData.setVolume(new BigDecimal(results.get("v").toString()));
            }
            if (results.containsKey("h")) {
                marketData.setHigh(new BigDecimal(results.get("h").toString()));
            }
            if (results.containsKey("l")) {
                marketData.setLow(new BigDecimal(results.get("l").toString()));
            }
            if (results.containsKey("o")) {
                BigDecimal open = new BigDecimal(results.get("o").toString());
                BigDecimal close = marketData.getPrice();
                marketData.setChange(close.subtract(open));
                marketData.setChangePercent(open.compareTo(BigDecimal.ZERO) > 0 ? 
                    marketData.getChange().divide(open, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)) : 
                    BigDecimal.ZERO);
            }
        }
        
        return marketData;
    }
    
    /**
     * Create empty market data for error cases
     */
    private MarketData createEmptyMarketData(Symbol symbol) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol.getSymbol());
        marketData.setAssetType(symbol.getAssetType());
        marketData.setPrice(BigDecimal.ZERO);
        marketData.setVolume(BigDecimal.ZERO);
        marketData.setChange(BigDecimal.ZERO);
        marketData.setChangePercent(BigDecimal.ZERO);
        marketData.setTimestamp(LocalDateTime.now());
        return marketData;
    }
    
    /**
     * Get API call statistics
     */
    public Map<String, Object> getApiCallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("callsPerMinute", apiCallCounts);
        stats.put("rateLimit", apiConfiguration.getKeys().getAlphaVantage().getRateLimit());
        stats.put("lastResetTime", lastResetTime);
        return stats;
    }
}
