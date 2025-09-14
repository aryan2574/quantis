package com.quantis.market_data.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Controller for real-time market data streaming
 * Serves retail trader dashboards with live market updates
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Track active subscriptions
    private final Map<String, Set<String>> clientSubscriptions = new ConcurrentHashMap<>();
    
    /**
     * Handle market data subscription requests
     */
    @MessageMapping("/market-data/subscribe")
    @SendTo("/topic/market-data")
    public void subscribeToMarketData(SubscriptionRequest request) {
        log.info("Client {} subscribing to symbols: {}", request.getClientId(), request.getSymbols());
        
        // Add to subscription tracking
        clientSubscriptions.computeIfAbsent(request.getClientId(), k -> ConcurrentHashMap.newKeySet())
            .addAll(request.getSymbols());
        
        // Start streaming data for subscribed symbols
        startMarketDataStream(request.getClientId(), request.getSymbols());
    }
    
    /**
     * Handle order book subscription requests
     */
    @MessageMapping("/order-book/subscribe")
    @SendTo("/topic/order-book")
    public void subscribeToOrderBook(OrderBookSubscriptionRequest request) {
        log.info("Client {} subscribing to order book for symbols: {}", request.getClientId(), request.getSymbols());
        
        // Start streaming order book data
        startOrderBookStream(request.getClientId(), request.getSymbols());
    }
    
    /**
     * Handle trade updates subscription
     */
    @MessageMapping("/trades/subscribe")
    @SendTo("/topic/trades")
    public void subscribeToTrades(TradeSubscriptionRequest request) {
        log.info("Client {} subscribing to trades for symbols: {}", request.getClientId(), request.getSymbols());
        
        // Start streaming trade updates
        startTradeStream(request.getClientId(), request.getSymbols());
    }
    
    /**
     * Start market data streaming for a client
     */
    private void startMarketDataStream(String clientId, Set<String> symbols) {
        Flux.interval(Duration.ofMillis(100)) // 10 updates per second
            .map(tick -> {
                // Get market data from Redis cache
                return symbols.stream()
                    .map(symbol -> getMarketDataFromRedis(symbol))
                    .filter(data -> data != null)
                    .toList();
            })
            .filter(dataList -> !dataList.isEmpty())
            .subscribe(
                dataList -> {
                    MarketDataUpdate update = new MarketDataUpdate();
                    update.setSymbols(dataList);
                    update.setTimestamp(System.currentTimeMillis());
                    
                    // Send to specific client
                    messagingTemplate.convertAndSendToUser(
                        clientId, 
                        "/queue/market-data", 
                        update
                    );
                },
                error -> log.error("Error in market data stream for client: {}", clientId, error)
            );
    }
    
    /**
     * Start order book streaming for a client
     */
    private void startOrderBookStream(String clientId, Set<String> symbols) {
        Flux.interval(Duration.ofMillis(200)) // 5 updates per second
            .map(tick -> {
                return symbols.stream()
                    .map(symbol -> getOrderBookFromRedis(symbol))
                    .filter(data -> data != null)
                    .toList();
            })
            .filter(dataList -> !dataList.isEmpty())
            .subscribe(
                dataList -> {
                    OrderBookUpdate update = new OrderBookUpdate();
                    update.setOrderBooks(dataList);
                    update.setTimestamp(System.currentTimeMillis());
                    
                    messagingTemplate.convertAndSendToUser(
                        clientId, 
                        "/queue/order-book", 
                        update
                    );
                },
                error -> log.error("Error in order book stream for client: {}", clientId, error)
            );
    }
    
    /**
     * Start trade streaming for a client
     */
    private void startTradeStream(String clientId, Set<String> symbols) {
        Flux.interval(Duration.ofMillis(500)) // 2 updates per second
            .map(tick -> {
                return symbols.stream()
                    .map(symbol -> getRecentTradesFromRedis(symbol))
                    .filter(data -> data != null)
                    .toList();
            })
            .filter(dataList -> !dataList.isEmpty())
            .subscribe(
                dataList -> {
                    TradeUpdate update = new TradeUpdate();
                    update.setTrades(dataList);
                    update.setTimestamp(System.currentTimeMillis());
                    
                    messagingTemplate.convertAndSendToUser(
                        clientId, 
                        "/queue/trades", 
                        update
                    );
                },
                error -> log.error("Error in trade stream for client: {}", clientId, error)
            );
    }
    
    /**
     * Get market data from Redis cache
     */
    private MarketDataSnapshot getMarketDataFromRedis(String symbol) {
        try {
            String data = redisTemplate.opsForValue().get("marketdata:" + symbol);
            if (data != null) {
                return objectMapper.readValue(data, MarketDataSnapshot.class);
            }
        } catch (Exception e) {
            log.error("Error getting market data from Redis for symbol: {}", symbol, e);
        }
        return null;
    }
    
    /**
     * Get order book from Redis cache
     */
    private OrderBookSnapshot getOrderBookFromRedis(String symbol) {
        try {
            String data = redisTemplate.opsForValue().get("orderbook:" + symbol);
            if (data != null) {
                return objectMapper.readValue(data, OrderBookSnapshot.class);
            }
        } catch (Exception e) {
            log.error("Error getting order book from Redis for symbol: {}", symbol, e);
        }
        return null;
    }
    
    /**
     * Get recent trades from Redis cache
     */
    private TradeSnapshot getRecentTradesFromRedis(String symbol) {
        try {
            String data = redisTemplate.opsForValue().get("trades:" + symbol);
            if (data != null) {
                return objectMapper.readValue(data, TradeSnapshot.class);
            }
        } catch (Exception e) {
            log.error("Error getting trades from Redis for symbol: {}", symbol, e);
        }
        return null;
    }
    
    // Data classes for WebSocket messages
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SubscriptionRequest {
        private String clientId;
        private Set<String> symbols;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderBookSubscriptionRequest {
        private String clientId;
        private Set<String> symbols;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TradeSubscriptionRequest {
        private String clientId;
        private Set<String> symbols;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketDataUpdate {
        private java.util.List<MarketDataSnapshot> symbols;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderBookUpdate {
        private java.util.List<OrderBookSnapshot> orderBooks;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TradeUpdate {
        private java.util.List<TradeSnapshot> trades;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MarketDataSnapshot {
        private String symbol;
        private double bestBid;
        private double bestAsk;
        private double lastPrice;
        private double spread;
        private long volume;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class OrderBookSnapshot {
        private String symbol;
        private java.util.List<PriceLevel> bids;
        private java.util.List<PriceLevel> asks;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PriceLevel {
        private double price;
        private long quantity;
        private int orderCount;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TradeSnapshot {
        private String symbol;
        private java.util.List<Trade> recentTrades;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class Trade {
        private String tradeId;
        private String side;
        private long quantity;
        private double price;
        private long timestamp;
    }
}
