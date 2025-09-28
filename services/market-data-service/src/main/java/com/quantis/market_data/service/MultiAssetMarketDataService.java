package com.quantis.market_data.service;

import com.quantis.market_data.model.AssetType;
import com.quantis.market_data.model.Symbol;
import com.quantis.market_data.model.MarketData;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.client.RestTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Multi-Asset Market Data Service
 * 
 * Handles market data for all asset classes with optimized data sources
 */
@Service
public class MultiAssetMarketDataService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Data source configurations
    private final Map<AssetType, String> dataSourceUrls = Map.of(
        AssetType.CRYPTO_SPOT, "https://api.binance.com/api/v3/ticker/price",
        AssetType.CRYPTO_FUTURES, "https://fapi.binance.com/fapi/v1/ticker/price",
        AssetType.FOREX, "https://api.exchangerate-api.com/v4/latest/",
        AssetType.STOCKS, "https://api.polygon.io/v2/aggs/ticker/",
        AssetType.FUTURES, "https://api.polygon.io/v2/aggs/ticker/",
        AssetType.OPTIONS, "https://api.polygon.io/v2/aggs/ticker/"
    );
    
    /**
     * Get real-time market data for any asset type
     */
    @Cacheable(value = "marketData", key = "#symbol.symbol")
    public MarketData getMarketData(Symbol symbol) {
        if (symbol == null || !symbol.isActive()) {
            throw new IllegalArgumentException("Invalid or inactive symbol");
        }
        
        // Check cache first
        String cacheKey = "market_data:" + symbol.getSymbol();
        MarketData cachedData = (MarketData) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null && !isStale(cachedData, symbol.getAssetType())) {
            return cachedData;
        }
        
        // Fetch from appropriate data source
        MarketData marketData = fetchFromDataSource(symbol);
        
        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, marketData, 
            getCacheExpiration(symbol.getAssetType()));
        
        // Publish to Kafka for real-time updates
        kafkaTemplate.send("market-data-updates", symbol.getSymbol(), marketData);
        
        return marketData;
    }
    
    /**
     * Get market data for multiple symbols
     */
    @Async
    public CompletableFuture<List<MarketData>> getMarketDataBatch(List<Symbol> symbols) {
        List<CompletableFuture<MarketData>> futures = symbols.stream()
            .map(symbol -> CompletableFuture.supplyAsync(() -> getMarketData(symbol)))
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
    
    /**
     * Get historical data for any asset type
     */
    public List<MarketData> getHistoricalData(Symbol symbol, LocalDateTime startTime, 
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
        
        // Fetch from appropriate data source
        List<MarketData> historicalData = fetchHistoricalFromDataSource(symbol, startTime, endTime, interval);
        
        // Cache the result
        redisTemplate.opsForValue().set(cacheKey, historicalData, 3600); // 1 hour cache
        
        return historicalData;
    }
    
    /**
     * Get market depth for any asset type
     */
    public Map<String, List<Map<String, Object>>> getMarketDepth(Symbol symbol, int limit) {
        if (symbol == null || !symbol.isActive()) {
            throw new IllegalArgumentException("Invalid or inactive symbol");
        }
        
        String cacheKey = "market_depth:" + symbol.getSymbol() + ":" + limit;
        Map<String, List<Map<String, Object>>> cachedData = 
            (Map<String, List<Map<String, Object>>>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }
        
        Map<String, List<Map<String, Object>>> marketDepth = fetchMarketDepthFromDataSource(symbol, limit);
        
        // Cache for shorter time (market depth changes frequently)
        redisTemplate.opsForValue().set(cacheKey, marketDepth, 30); // 30 seconds cache
        
        return marketDepth;
    }
    
    /**
     * Get available symbols for asset type
     */
    public List<Symbol> getAvailableSymbols(AssetType assetType) {
        String cacheKey = "available_symbols:" + assetType.name();
        List<Symbol> cachedSymbols = (List<Symbol>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedSymbols != null) {
            return cachedSymbols;
        }
        
        List<Symbol> symbols = fetchAvailableSymbolsFromDataSource(assetType);
        
        // Cache for longer time (symbols don't change frequently)
        redisTemplate.opsForValue().set(cacheKey, symbols, 3600); // 1 hour cache
        
        return symbols;
    }
    
    /**
     * Get market status for asset type
     */
    public Map<String, Object> getMarketStatus(AssetType assetType) {
        Map<String, Object> status = new HashMap<>();
        
        status.put("assetType", assetType);
        status.put("isOpen", isMarketOpen(assetType));
        status.put("tradingSession", assetType.getTradingSession());
        status.put("nextOpen", getNextMarketOpen(assetType));
        status.put("nextClose", getNextMarketClose(assetType));
        status.put("timezone", assetType.getTimezone());
        
        return status;
    }
    
    /**
     * Fetch market data from appropriate data source
     */
    private MarketData fetchFromDataSource(Symbol symbol) {
        String url = dataSourceUrls.get(symbol.getAssetType());
        if (url == null) {
            throw new UnsupportedOperationException("No data source configured for asset type: " + symbol.getAssetType());
        }
        
        // Make API call to data source
        Map<String, Object> response = restTemplate.getForObject(url + "?symbol=" + symbol.getSymbol(), Map.class);
        
        // Convert response to MarketData
        return convertToMarketData(response, symbol);
    }
    
    /**
     * Fetch historical data from data source
     */
    private List<MarketData> fetchHistoricalFromDataSource(Symbol symbol, LocalDateTime startTime, 
                                                           LocalDateTime endTime, String interval) {
        String url = dataSourceUrls.get(symbol.getAssetType());
        if (url == null) {
            throw new UnsupportedOperationException("No data source configured for asset type: " + symbol.getAssetType());
        }
        
        // Build historical data URL
        String historicalUrl = url + "/historical?symbol=" + symbol.getSymbol() + 
                             "&start=" + startTime.toString() + 
                             "&end=" + endTime.toString() + 
                             "&interval=" + interval;
        
        List<Map<String, Object>> response = restTemplate.getForObject(historicalUrl, List.class);
        
        return response.stream()
            .map(data -> convertToMarketData(data, symbol))
            .collect(Collectors.toList());
    }
    
    /**
     * Fetch market depth from data source
     */
    private Map<String, List<Map<String, Object>>> fetchMarketDepthFromDataSource(Symbol symbol, int limit) {
        String url = dataSourceUrls.get(symbol.getAssetType());
        if (url == null) {
            throw new UnsupportedOperationException("No data source configured for asset type: " + symbol.getAssetType());
        }
        
        String depthUrl = url + "/depth?symbol=" + symbol.getSymbol() + "&limit=" + limit;
        Map<String, Object> response = restTemplate.getForObject(depthUrl, Map.class);
        
        Map<String, List<Map<String, Object>>> marketDepth = new HashMap<>();
        marketDepth.put("bids", (List<Map<String, Object>>) response.get("bids"));
        marketDepth.put("asks", (List<Map<String, Object>>) response.get("asks"));
        
        return marketDepth;
    }
    
    /**
     * Fetch available symbols from data source
     */
    private List<Symbol> fetchAvailableSymbolsFromDataSource(AssetType assetType) {
        String url = dataSourceUrls.get(assetType);
        if (url == null) {
            throw new UnsupportedOperationException("No data source configured for asset type: " + assetType);
        }
        
        String symbolsUrl = url + "/symbols";
        List<Map<String, Object>> response = restTemplate.getForObject(symbolsUrl, List.class);
        
        return response.stream()
            .map(data -> convertToSymbol(data, assetType))
            .collect(Collectors.toList());
    }
    
    /**
     * Convert API response to MarketData
     */
    private MarketData convertToMarketData(Map<String, Object> data, Symbol symbol) {
        MarketData marketData = new MarketData();
        marketData.setSymbol(symbol.getSymbol());
        marketData.setAssetType(symbol.getAssetType());
        marketData.setPrice(new BigDecimal(data.get("price").toString()));
        marketData.setVolume(new BigDecimal(data.get("volume").toString()));
        marketData.setTimestamp(LocalDateTime.now());
        
        if (data.containsKey("bid")) {
            marketData.setBid(new BigDecimal(data.get("bid").toString()));
        }
        if (data.containsKey("ask")) {
            marketData.setAsk(new BigDecimal(data.get("ask").toString()));
        }
        if (data.containsKey("high")) {
            marketData.setHigh(new BigDecimal(data.get("high").toString()));
        }
        if (data.containsKey("low")) {
            marketData.setLow(new BigDecimal(data.get("low").toString()));
        }
        if (data.containsKey("change")) {
            marketData.setChange(new BigDecimal(data.get("change").toString()));
        }
        if (data.containsKey("changePercent")) {
            marketData.setChangePercent(new BigDecimal(data.get("changePercent").toString()));
        }
        
        return marketData;
    }
    
    /**
     * Convert API response to Symbol
     */
    private Symbol convertToSymbol(Map<String, Object> data, AssetType assetType) {
        Symbol symbol = new Symbol();
        symbol.setSymbol(data.get("symbol").toString());
        symbol.setAssetType(assetType);
        symbol.setExchange(data.get("exchange").toString());
        symbol.setCurrency(data.get("currency").toString());
        symbol.setBaseCurrency(data.get("baseCurrency").toString());
        symbol.setQuoteCurrency(data.get("quoteCurrency").toString());
        
        if (data.containsKey("contractSize")) {
            symbol.setContractSize(new BigDecimal(data.get("contractSize").toString()));
        }
        if (data.containsKey("tickSize")) {
            symbol.setTickSize(new BigDecimal(data.get("tickSize").toString()));
        }
        if (data.containsKey("minOrderSize")) {
            symbol.setMinOrderSize(new BigDecimal(data.get("minOrderSize").toString()));
        }
        if (data.containsKey("maxOrderSize")) {
            symbol.setMaxOrderSize(new BigDecimal(data.get("maxOrderSize").toString()));
        }
        if (data.containsKey("leverage")) {
            symbol.setLeverage(new BigDecimal(data.get("leverage").toString()));
        }
        
        return symbol;
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
    
    /**
     * Check if market is open for asset type
     */
    private boolean isMarketOpen(AssetType assetType) {
        LocalDateTime now = LocalDateTime.now();
        AssetType.TradingSession session = assetType.getTradingSession();
        
        if (session == AssetType.TradingSession.TWENTY_FOUR_SEVEN) {
            return true;
        }
        
        // Implement market hours logic based on timezone and session
        // This is a simplified version - real implementation would handle holidays, etc.
        return true; // Placeholder
    }
    
    /**
     * Get next market open time
     */
    private LocalDateTime getNextMarketOpen(AssetType assetType) {
        // Implement logic to calculate next market open
        return LocalDateTime.now().plusHours(1); // Placeholder
    }
    
    /**
     * Get next market close time
     */
    private LocalDateTime getNextMarketClose(AssetType assetType) {
        // Implement logic to calculate next market close
        return LocalDateTime.now().plusHours(8); // Placeholder
    }
}
