package com.quantis.market_data.graphql;

import com.quantis.market_data.graphql.model.MarketDataModels.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GraphQL resolver for market data queries and subscriptions
 * Provides historical data queries and real-time subscriptions
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MarketDataResolver {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    /**
     * Get current market data for a symbol
     */
    @QueryMapping
    public MarketData getMarketData(@Argument String symbol) {
        log.debug("GraphQL query: getMarketData for symbol: {}", symbol);
        
        try {
            String data = redisTemplate.opsForValue().get("marketdata:" + symbol);
            if (data != null) {
                // Parse Redis data and return MarketData object
                return parseMarketDataFromRedis(data, symbol);
            }
        } catch (Exception e) {
            log.error("Error getting market data for symbol: {}", symbol, e);
        }
        
        return MarketData.builder()
            .symbol(symbol)
            .status("NOT_FOUND")
            .build();
    }
    
    /**
     * Get historical market data for a symbol
     */
    @QueryMapping
    public List<HistoricalData> getHistoricalData(
            @Argument String symbol,
            @Argument String interval,
            @Argument Long startTime,
            @Argument Long endTime,
            @Argument Integer limit) {
        
        log.debug("GraphQL query: getHistoricalData for symbol: {}, interval: {}", symbol, interval);
        
        try {
            // Generate sample historical data
            // In a real implementation, this would query a time-series database
            return generateHistoricalData(symbol, interval, startTime, endTime, limit);
        } catch (Exception e) {
            log.error("Error getting historical data for symbol: {}", symbol, e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get order book for a symbol
     */
    @QueryMapping
    public OrderBook getOrderBook(@Argument String symbol, @Argument Integer depth) {
        log.debug("GraphQL query: getOrderBook for symbol: {}, depth: {}", symbol, depth);
        
        try {
            String data = redisTemplate.opsForValue().get("orderbook:" + symbol);
            if (data != null) {
                return parseOrderBookFromRedis(data, symbol, depth);
            }
        } catch (Exception e) {
            log.error("Error getting order book for symbol: {}", symbol, e);
        }
        
        return OrderBook.builder()
            .symbol(symbol)
            .status("NOT_FOUND")
            .build();
    }
    
    /**
     * Get recent trades for a symbol
     */
    @QueryMapping
    public List<Trade> getRecentTrades(@Argument String symbol, @Argument Integer limit) {
        log.debug("GraphQL query: getRecentTrades for symbol: {}, limit: {}", symbol, limit);
        
        try {
            String data = redisTemplate.opsForValue().get("trades:" + symbol);
            if (data != null) {
                return parseTradesFromRedis(data, limit);
            }
        } catch (Exception e) {
            log.error("Error getting recent trades for symbol: {}", symbol, e);
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Get market summary for multiple symbols
     */
    @QueryMapping
    public List<MarketSummary> getMarketSummary(@Argument List<String> symbols) {
        log.debug("GraphQL query: getMarketSummary for symbols: {}", symbols);
        
        List<MarketSummary> summaries = new ArrayList<>();
        
        for (String symbol : symbols) {
            try {
                String data = redisTemplate.opsForValue().get("marketdata:" + symbol);
                if (data != null) {
                    MarketData marketData = parseMarketDataFromRedis(data, symbol);
                    summaries.add(MarketSummary.builder()
                        .symbol(symbol)
                        .lastPrice(marketData.getLastPrice())
                        .change(marketData.getChange())
                        .changePercent(marketData.getChangePercent())
                        .volume(marketData.getVolume())
                        .build());
                }
            } catch (Exception e) {
                log.error("Error getting market summary for symbol: {}", symbol, e);
            }
        }
        
        return summaries;
    }
    
    /**
     * Subscribe to real-time market data updates
     */
    @SubscriptionMapping
    public Flux<MarketDataUpdate> marketDataUpdates(@Argument List<String> symbols) {
        log.info("GraphQL subscription: marketDataUpdates for symbols: {}", symbols);
        
        return Flux.interval(Duration.ofMillis(1000)) // 1 update per second
            .map(tick -> {
                List<MarketData> marketDataList = new ArrayList<>();
                
                for (String symbol : symbols) {
                    try {
                        String data = redisTemplate.opsForValue().get("marketdata:" + symbol);
                        if (data != null) {
                            marketDataList.add(parseMarketDataFromRedis(data, symbol));
                        }
                    } catch (Exception e) {
                        log.error("Error getting market data for subscription: {}", symbol, e);
                    }
                }
                
                return MarketDataUpdate.builder()
                    .symbols(marketDataList)
                    .timestamp(System.currentTimeMillis())
                    .build();
            })
            .filter(update -> !update.getSymbols().isEmpty());
    }
    
    /**
     * Subscribe to real-time trade updates
     */
    @SubscriptionMapping
    public Flux<TradeUpdate> tradeUpdates(@Argument List<String> symbols) {
        log.info("GraphQL subscription: tradeUpdates for symbols: {}", symbols);
        
        return Flux.interval(Duration.ofMillis(500)) // 2 updates per second
            .map(tick -> {
                List<Trade> trades = new ArrayList<>();
                
                for (String symbol : symbols) {
                    try {
                        String data = redisTemplate.opsForValue().get("trades:" + symbol);
                        if (data != null) {
                            trades.addAll(parseTradesFromRedis(data, 5)); // Last 5 trades
                        }
                    } catch (Exception e) {
                        log.error("Error getting trades for subscription: {}", symbol, e);
                    }
                }
                
                return TradeUpdate.builder()
                    .trades(trades)
                    .timestamp(System.currentTimeMillis())
                    .build();
            })
            .filter(update -> !update.getTrades().isEmpty());
    }
    
    // Helper methods
    private MarketData parseMarketDataFromRedis(String data, String symbol) {
        // Simple parsing - in real implementation, use proper JSON parsing
        return MarketData.builder()
            .symbol(symbol)
            .bestBid(150.0) // Mock data
            .bestAsk(150.1)
            .lastPrice(150.05)
            .spread(0.1)
            .volume(1000000L)
            .change(0.5)
            .changePercent(0.33)
            .timestamp(System.currentTimeMillis())
            .status("ACTIVE")
            .build();
    }
    
    private OrderBook parseOrderBookFromRedis(String data, String symbol, Integer depth) {
        List<PriceLevel> bids = new ArrayList<>();
        List<PriceLevel> asks = new ArrayList<>();
        
        // Generate mock order book data
        for (int i = 0; i < (depth != null ? depth : 10); i++) {
            bids.add(PriceLevel.builder()
                .price(150.0 - (i * 0.1))
                .quantity(1000L + (i * 100))
                .orderCount(5 + i)
                .build());
                
            asks.add(PriceLevel.builder()
                .price(150.1 + (i * 0.1))
                .quantity(1000L + (i * 100))
                .orderCount(5 + i)
                .build());
        }
        
        return OrderBook.builder()
            .symbol(symbol)
            .bids(bids)
            .asks(asks)
            .timestamp(System.currentTimeMillis())
            .status("ACTIVE")
            .build();
    }
    
    private List<Trade> parseTradesFromRedis(String data, Integer limit) {
        List<Trade> trades = new ArrayList<>();
        
        // Generate mock trade data
        int tradeCount = limit != null ? Math.min(limit, 10) : 10;
        for (int i = 0; i < tradeCount; i++) {
            trades.add(Trade.builder()
                .tradeId("trade_" + i)
                .symbol("AAPL")
                .side(i % 2 == 0 ? "BUY" : "SELL")
                .quantity(100L + (i * 10))
                .price(150.0 + (i * 0.1))
                .timestamp(System.currentTimeMillis() - (i * 1000))
                .build());
        }
        
        return trades;
    }
    
    private List<HistoricalData> generateHistoricalData(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        List<HistoricalData> historicalData = new ArrayList<>();
        
        long intervalMs = getIntervalMs(interval);
        long currentTime = startTime != null ? startTime : System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours ago
        long end = endTime != null ? endTime : System.currentTimeMillis();
        int count = limit != null ? limit : 100;
        
        double basePrice = 150.0;
        
        for (int i = 0; i < count && currentTime < end; i++) {
            double open = basePrice + (Math.random() - 0.5) * 2;
            double high = open + Math.random() * 1;
            double low = open - Math.random() * 1;
            double close = low + Math.random() * (high - low);
            long volume = 1000000L + (long)(Math.random() * 500000);
            
            historicalData.add(HistoricalData.builder()
                .timestamp(currentTime)
                .open(open)
                .high(high)
                .low(low)
                .close(close)
                .volume(volume)
                .build());
            
            currentTime += intervalMs;
            basePrice = close; // Next candle starts where previous ended
        }
        
        return historicalData;
    }
    
    private long getIntervalMs(String interval) {
        switch (interval.toLowerCase()) {
            case "1m": return 60 * 1000;
            case "5m": return 5 * 60 * 1000;
            case "15m": return 15 * 60 * 1000;
            case "1h": return 60 * 60 * 1000;
            case "4h": return 4 * 60 * 60 * 1000;
            case "1d": return 24 * 60 * 60 * 1000;
            default: return 60 * 1000; // Default to 1 minute
        }
    }
}
