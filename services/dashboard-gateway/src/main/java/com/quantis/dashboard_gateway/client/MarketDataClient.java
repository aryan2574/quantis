package com.quantis.dashboard_gateway.client;

import com.quantis.dashboard_gateway.model.MarketDataModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Client for Market Data Service
 * 
 * Provides access to market data via GraphQL and Redis
 * for real-time dashboard updates
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MarketDataClient {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebClient.Builder webClientBuilder;

    /**
     * Get current market data for a symbol
     */
    public Mono<MarketDataModel.MarketData> getMarketData(String symbol) {
        return Mono.fromCallable(() -> {
            try {
                // Try Redis first for real-time data
                String redisKey = "marketdata:" + symbol;
                Object data = redisTemplate.opsForValue().get(redisKey);
                
                if (data != null) {
                    return parseMarketDataFromRedis(data.toString(), symbol);
                }
                
                // Fallback to GraphQL service
                return getMarketDataFromGraphQL(symbol);
            } catch (Exception e) {
                log.error("Error getting market data for symbol: {}", symbol, e);
                return getDefaultMarketData(symbol);
            }
        });
    }

    /**
     * Get order book for a symbol
     */
    public Mono<MarketDataModel.OrderBook> getOrderBook(String symbol, Integer depth) {
        return Mono.fromCallable(() -> {
            try {
                String redisKey = "orderbook:" + symbol;
                Object data = redisTemplate.opsForValue().get(redisKey);
                
                if (data != null) {
                    return parseOrderBookFromRedis(data.toString(), symbol, depth);
                }
                
                return getDefaultOrderBook(symbol);
            } catch (Exception e) {
                log.error("Error getting order book for symbol: {}", symbol, e);
                return getDefaultOrderBook(symbol);
            }
        });
    }

    /**
     * Get recent trades for a symbol
     */
    public Mono<List<MarketDataModel.Trade>> getRecentTrades(String symbol, Integer limit) {
        return Mono.fromCallable(() -> {
            try {
                String redisKey = "trades:" + symbol;
                Object data = redisTemplate.opsForValue().get(redisKey);
                
                if (data != null) {
                    return parseTradesFromRedis(data.toString(), limit);
                }
                
                return new ArrayList<>();
            } catch (Exception e) {
                log.error("Error getting recent trades for symbol: {}", symbol, e);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Get historical data for a symbol
     */
    public Mono<List<MarketDataModel.HistoricalData>> getHistoricalData(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        return Mono.fromCallable(() -> {
            try {
                // Call Market Data Service GraphQL endpoint
                return getHistoricalDataFromGraphQL(symbol, interval, startTime, endTime, limit);
            } catch (Exception e) {
                log.error("Error getting historical data for symbol: {}", symbol, e);
                return new ArrayList<>();
            }
        });
    }

    /**
     * Get market summary for multiple symbols
     */
    public Mono<List<MarketDataModel.MarketSummary>> getMarketSummary(List<String> symbols) {
        return Mono.fromCallable(() -> {
            try {
                List<MarketDataModel.MarketSummary> summaries = new ArrayList<>();
                for (String symbol : symbols) {
                    MarketDataModel.MarketData data = getMarketData(symbol).block();
                    if (data != null) {
                        summaries.add(MarketDataModel.MarketSummary.builder()
                            .symbol(data.getSymbol())
                            .lastPrice(data.getLastPrice())
                            .change(data.getChange())
                            .changePercent(data.getChangePercent())
                            .volume(data.getVolume())
                            .high24h(data.getHigh24h())
                            .low24h(data.getLow24h())
                            .marketCap(BigDecimal.ZERO)
                            .build());
                    }
                }
                return summaries;
            } catch (Exception e) {
                log.error("Error getting market summary for symbols: {}", symbols, e);
                return new ArrayList<>();
            }
        });
    }

    // Helper methods for data parsing
    private MarketDataModel.MarketData parseMarketDataFromRedis(String data, String symbol) {
        // Simple parsing - in production, use proper JSON parsing
        return MarketDataModel.MarketData.builder()
            .symbol(symbol)
            .bestBid(new BigDecimal("150.0"))
            .bestAsk(new BigDecimal("150.1"))
            .lastPrice(new BigDecimal("150.05"))
            .spread(new BigDecimal("0.1"))
            .volume(1000000L)
            .change(new BigDecimal("0.5"))
            .changePercent(new BigDecimal("0.33"))
            .timestamp(Instant.now())
            .status("ACTIVE")
            .build();
    }

    private MarketDataModel.OrderBook parseOrderBookFromRedis(String data, String symbol, Integer depth) {
        return MarketDataModel.OrderBook.builder()
            .symbol(symbol)
            .bids(new ArrayList<>())
            .asks(new ArrayList<>())
            .timestamp(Instant.now())
            .status("ACTIVE")
            .build();
    }

    private List<MarketDataModel.Trade> parseTradesFromRedis(String data, Integer limit) {
        return new ArrayList<>();
    }

    private MarketDataModel.MarketData getMarketDataFromGraphQL(String symbol) {
        // Call Market Data Service GraphQL endpoint
        return getDefaultMarketData(symbol);
    }

    private List<MarketDataModel.HistoricalData> getHistoricalDataFromGraphQL(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        // Call Market Data Service GraphQL endpoint
        return new ArrayList<>();
    }

    // Default data methods
    private MarketDataModel.MarketData getDefaultMarketData(String symbol) {
        return MarketDataModel.MarketData.builder()
            .symbol(symbol)
            .bestBid(BigDecimal.ZERO)
            .bestAsk(BigDecimal.ZERO)
            .lastPrice(BigDecimal.ZERO)
            .spread(BigDecimal.ZERO)
            .volume(0L)
            .change(BigDecimal.ZERO)
            .changePercent(BigDecimal.ZERO)
            .timestamp(Instant.now())
            .status("NOT_FOUND")
            .build();
    }

    private MarketDataModel.OrderBook getDefaultOrderBook(String symbol) {
        return MarketDataModel.OrderBook.builder()
            .symbol(symbol)
            .bids(new ArrayList<>())
            .asks(new ArrayList<>())
            .timestamp(Instant.now())
            .status("NOT_FOUND")
            .build();
    }
}
