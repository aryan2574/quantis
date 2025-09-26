package com.quantis.market_data.service;

import com.quantis.market_data.model.AssetType;
import com.quantis.market_data.model.Symbol;
import com.quantis.market_data.model.MarketData;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Real Market Data Service
 * 
 * Connects to actual market data providers and fetches real-time data
 */
@Service
public class RealMarketDataService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private CentralizedApiClient centralizedApiClient;
    
    /**
     * Get real-time market data for symbol
     */
    public MarketData getRealTimeMarketData(Symbol symbol) {
        if (symbol == null || !symbol.isActive()) {
            throw new IllegalArgumentException("Invalid or inactive symbol");
        }
        
        // Check cache first
        String cacheKey = "realtime_market_data:" + symbol.getSymbol();
        MarketData cachedData = (MarketData) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null && !isStale(cachedData, symbol.getAssetType())) {
            return cachedData;
        }
        
        // Fetch from centralized API client
        MarketData marketData = fetchFromCentralizedApiClient(symbol);
        
        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, marketData, 
            getCacheExpiration(symbol.getAssetType()));
        
        return marketData;
    }
    
    /**
     * Get real-time market data for multiple symbols
     */
    @Async("marketDataExecutor")
    public CompletableFuture<List<MarketData>> getRealTimeMarketDataBatch(List<Symbol> symbols) {
        return centralizedApiClient.getBatchMarketData(symbols);
    }
    
    /**
     * Get historical data from real data source
     */
    public List<MarketData> getRealHistoricalData(Symbol symbol, LocalDateTime startTime, 
                                                 LocalDateTime endTime, String interval) {
        if (symbol == null || !symbol.isActive()) {
            throw new IllegalArgumentException("Invalid or inactive symbol");
        }
        
        // Check cache first
        String cacheKey = String.format("historical:%s:%s:%s:%s", 
            symbol.getSymbol(), startTime, endTime, interval);
        List<MarketData> cachedData = (List<MarketData>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }
        
        // Fetch from real data source
        List<MarketData> historicalData = fetchHistoricalFromRealDataSource(symbol, startTime, endTime, interval);
        
        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, historicalData, 3600); // 1 hour cache
        
        return historicalData;
    }
    
    /**
     * Get market depth from real data source
     */
    public Map<String, List<Map<String, Object>>> getRealMarketDepth(Symbol symbol, int limit) {
        if (symbol == null || !symbol.isActive()) {
            throw new IllegalArgumentException("Invalid or inactive symbol");
        }
        
        String cacheKey = "market_depth:" + symbol.getSymbol() + ":" + limit;
        Map<String, List<Map<String, Object>>> cachedData = 
            (Map<String, List<Map<String, Object>>>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }
        
        Map<String, List<Map<String, Object>>> marketDepth = fetchMarketDepthFromRealDataSource(symbol, limit);
        
        // Cache for shorter time (market depth changes frequently)
        redisTemplate.opsForValue().set(cacheKey, marketDepth, 30); // 30 seconds cache
        
        return marketDepth;
    }
    
    /**
     * Scheduled task to refresh market data
     */
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    public void refreshMarketData() {
        // Get all active symbols from cache
        for (AssetType assetType : AssetType.values()) {
            String cacheKey = "symbols:" + assetType.name();
            List<Symbol> symbols = (List<Symbol>) redisTemplate.opsForValue().get(cacheKey);
            
            if (symbols != null && !symbols.isEmpty()) {
                // Refresh market data for top symbols (limit to avoid rate limits)
                List<Symbol> topSymbols = symbols.stream()
                    .limit(50) // Limit to top 50 symbols per asset type
                    .collect(Collectors.toList());
                
                refreshMarketDataForSymbols(topSymbols);
            }
        }
    }
    
    /**
     * Refresh market data for specific symbols
     */
    private void refreshMarketDataForSymbols(List<Symbol> symbols) {
        symbols.parallelStream().forEach(symbol -> {
            try {
                getRealTimeMarketData(symbol);
            } catch (Exception e) {
                System.err.println("Error refreshing market data for " + symbol.getSymbol() + ": " + e.getMessage());
            }
        });
    }
    
    /**
     * Fetch market data from centralized API client
     */
    private MarketData fetchFromCentralizedApiClient(Symbol symbol) {
        try {
            CompletableFuture<MarketData> future;
            
            switch (symbol.getAssetType()) {
                case CRYPTO_SPOT:
                case CRYPTO_FUTURES:
                case CRYPTO_PERPETUAL:
                case CRYPTO_OPTIONS:
                    future = centralizedApiClient.getCryptoMarketData(symbol);
                    break;
                case FOREX:
                    future = centralizedApiClient.getForexMarketData(symbol);
                    break;
                case STOCKS:
                case FUTURES:
                case OPTIONS:
                case ENTERPRISE_TOKENS:
                    future = centralizedApiClient.getStockMarketData(symbol);
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported asset type: " + symbol.getAssetType());
            }
            
            return future.get(10, TimeUnit.SECONDS); // 10 second timeout
        } catch (Exception e) {
            System.err.println("Error fetching market data for " + symbol.getSymbol() + ": " + e.getMessage());
            return createEmptyMarketData(symbol);
        }
    }
    
    
    /**
     * Fetch historical data from real data source
     */
    private List<MarketData> fetchHistoricalFromRealDataSource(Symbol symbol, LocalDateTime startTime, 
                                                               LocalDateTime endTime, String interval) {
        // Implement historical data fetching from real data sources
        // This is a placeholder - implement actual historical data fetching
        return new ArrayList<>();
    }
    
    /**
     * Fetch market depth from real data source
     */
    private Map<String, List<Map<String, Object>>> fetchMarketDepthFromRealDataSource(Symbol symbol, int limit) {
        // Implement market depth fetching from real data sources
        // This is a placeholder - implement actual market depth fetching
        Map<String, List<Map<String, Object>>> marketDepth = new HashMap<>();
        marketDepth.put("bids", new ArrayList<>());
        marketDepth.put("asks", new ArrayList<>());
        return marketDepth;
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
     * Check if market data is stale
     */
    private boolean isStale(MarketData data, AssetType assetType) {
        LocalDateTime now = LocalDateTime.now();
        long ageInSeconds = java.time.temporal.ChronoUnit.SECONDS.between(data.getTimestamp(), now);
        return ageInSeconds > assetType.getDataRefreshInterval();
    }
    
    /**
     * Get cache expiration time for asset type
     */
    private int getCacheExpiration(AssetType assetType) {
        return Math.toIntExact(assetType.getDataRefreshInterval());
    }
}
