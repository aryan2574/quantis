package com.quantis.update_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.update_service.model.OrderBookSnapshot;
import com.quantis.update_service.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Service responsible for updating order book snapshots in Redis cache.
 * 
 * This service:
 * 1. Maintains real-time order book state for each symbol
 * 2. Updates Redis with latest order book snapshots
 * 3. Provides lock-free operations for high-performance updates
 * 4. Manages order book depth and market data distribution
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBookUpdateService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    // Order book state management
    private final AtomicReference<OrderBookSnapshot> orderBookCache = new AtomicReference<>();
    private final AtomicLong sequenceNumber = new AtomicLong(0);
    
    // Redis key patterns
    private static final String ORDER_BOOK_KEY_PREFIX = "orderbook:";
    private static final String MARKET_DATA_KEY_PREFIX = "marketdata:";
    private static final String TRADE_HISTORY_KEY_PREFIX = "trades:";
    private static final String SEQUENCE_KEY_PREFIX = "sequence:";
    
    /**
     * Update order book state from a trade execution.
     * 
     * @param trade The executed trade
     */
    public void updateOrderBookFromTrade(Trade trade) {
        String symbol = trade.getSymbol();
        
        try {
            // Get current order book snapshot
            OrderBookSnapshot currentSnapshot = getOrderBookSnapshot(symbol);
            
            // Update order book based on trade
            OrderBookSnapshot updatedSnapshot = updateSnapshotFromTrade(currentSnapshot, trade);
            
            // Store updated snapshot in Redis
            storeOrderBookSnapshot(symbol, updatedSnapshot);
            
            // Store trade in history
            storeTradeHistory(symbol, trade);
            
            // Update market data
            updateMarketData(symbol, trade);
            
            log.debug("Updated order book for symbol {} with trade {}", symbol, trade.getTradeId());
            
        } catch (Exception e) {
            log.error("Failed to update order book for symbol {} with trade {}", symbol, trade.getTradeId(), e);
        }
    }
    
    /**
     * Get current order book snapshot for a symbol
     */
    private OrderBookSnapshot getOrderBookSnapshot(String symbol) {
        String key = ORDER_BOOK_KEY_PREFIX + symbol;
        String snapshotJson = redisTemplate.opsForValue().get(key);
        
        if (snapshotJson != null) {
            try {
                return objectMapper.readValue(snapshotJson, OrderBookSnapshot.class);
            } catch (Exception e) {
                log.warn("Failed to deserialize order book snapshot for symbol {}", symbol, e);
            }
        }
        
        // Return default snapshot if none exists
        return createDefaultSnapshot(symbol);
    }
    
    /**
     * Update order book snapshot based on trade execution
     */
    private OrderBookSnapshot updateSnapshotFromTrade(OrderBookSnapshot snapshot, Trade trade) {
        // Update last price
        snapshot.setLastPrice(trade.getPrice());
        
        // Update total volume
        snapshot.setTotalVolume(snapshot.getTotalVolume() + trade.getQuantity());
        
        // Update sequence number
        long newSequence = sequenceNumber.incrementAndGet();
        snapshot.setSequenceNumber(newSequence);
        snapshot.setTimestamp(Instant.now());
        
        // Update market status (simplified - in production this would be more sophisticated)
        snapshot.setMarketStatus("OPEN");
        
        // Note: In a real implementation, you would:
        // 1. Update bid/ask levels based on order book changes
        // 2. Recalculate spread
        // 3. Update top 5 bids/asks
        // 4. Handle order matching logic
        
        // For now, we'll maintain the existing bid/ask and update spread
        snapshot.setSpread(snapshot.getBestAsk() - snapshot.getBestBid());
        
        return snapshot;
    }
    
    /**
     * Store order book snapshot in Redis
     */
    private void storeOrderBookSnapshot(String symbol, OrderBookSnapshot snapshot) {
        try {
            String key = ORDER_BOOK_KEY_PREFIX + symbol;
            String snapshotJson = objectMapper.writeValueAsString(snapshot);
            
            // Store with TTL of 1 hour
            redisTemplate.opsForValue().set(key, snapshotJson, java.time.Duration.ofHours(1));
            
        } catch (Exception e) {
            log.error("Failed to store order book snapshot for symbol {}", symbol, e);
        }
    }
    
    /**
     * Store trade in history for the symbol
     */
    private void storeTradeHistory(String symbol, Trade trade) {
        try {
            String key = TRADE_HISTORY_KEY_PREFIX + symbol;
            String tradeJson = objectMapper.writeValueAsString(trade);
            
            // Add to list with TTL of 24 hours
            redisTemplate.opsForList().leftPush(key, tradeJson);
            redisTemplate.expire(key, java.time.Duration.ofHours(24));
            
            // Keep only last 1000 trades
            redisTemplate.opsForList().trim(key, 0, 999);
            
        } catch (Exception e) {
            log.error("Failed to store trade history for symbol {}", symbol, e);
        }
    }
    
    /**
     * Update market data for the symbol
     */
    private void updateMarketData(String symbol, Trade trade) {
        try {
            String key = MARKET_DATA_KEY_PREFIX + symbol;
            
            // Update last price
            redisTemplate.opsForHash().put(key, "lastPrice", String.valueOf(trade.getPrice()));
            redisTemplate.opsForHash().put(key, "lastUpdate", String.valueOf(System.currentTimeMillis()));
            redisTemplate.opsForHash().put(key, "lastTradeId", trade.getTradeId());
            
            // Increment volume
            redisTemplate.opsForHash().increment(key, "volume", trade.getQuantity());
            
            // Set TTL
            redisTemplate.expire(key, java.time.Duration.ofHours(1));
            
        } catch (Exception e) {
            log.error("Failed to update market data for symbol {}", symbol, e);
        }
    }
    
    /**
     * Create default order book snapshot for a new symbol
     */
    private OrderBookSnapshot createDefaultSnapshot(String symbol) {
        return OrderBookSnapshot.builder()
            .symbol(symbol)
            .bestBid(0.0)
            .bestAsk(0.0)
            .lastPrice(0.0)
            .spread(0.0)
            .totalVolume(0L)
            .orderCount(0L)
            .topBids(List.of())
            .topAsks(List.of())
            .timestamp(Instant.now())
            .sequenceNumber(sequenceNumber.incrementAndGet())
            .marketStatus("CLOSED")
            .build();
    }
    
    /**
     * Get current order book snapshot for a symbol (public method)
     */
    public OrderBookSnapshot getCurrentOrderBook(String symbol) {
        return getOrderBookSnapshot(symbol);
    }
    
    /**
     * Get recent trades for a symbol
     */
    public List<String> getRecentTrades(String symbol, int limit) {
        String key = TRADE_HISTORY_KEY_PREFIX + symbol;
        return redisTemplate.opsForList().range(key, 0, limit - 1);
    }
    
    /**
     * Get market data for a symbol
     */
    public Object getMarketData(String symbol) {
        String key = MARKET_DATA_KEY_PREFIX + symbol;
        return redisTemplate.opsForHash().entries(key);
    }
    
    /**
     * Get processing statistics
     */
    public ProcessingStats getProcessingStats() {
        return ProcessingStats.builder()
            .currentSequenceNumber(sequenceNumber.get())
            .build();
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessingStats {
        private long currentSequenceNumber;
    }
}
