package com.quantis.update_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

/**
 * Order book snapshot model for Redis caching.
 * This represents the current state of an order book for a specific symbol.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBookSnapshot {
    
    /**
     * Trading symbol (e.g., "AAPL", "GOOGL")
     */
    private String symbol;
    
    /**
     * Best bid price (highest buy order price)
     */
    private double bestBid;
    
    /**
     * Best ask price (lowest sell order price)
     */
    private double bestAsk;
    
    /**
     * Last traded price
     */
    private double lastPrice;
    
    /**
     * Bid-ask spread
     */
    private double spread;
    
    /**
     * Total volume traded today
     */
    private long totalVolume;
    
    /**
     * Number of active orders
     */
    private long orderCount;
    
    /**
     * Top 5 bid orders (price, quantity)
     */
    private List<PriceLevel> topBids;
    
    /**
     * Top 5 ask orders (price, quantity)
     */
    private List<PriceLevel> topAsks;
    
    /**
     * Timestamp when snapshot was created
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant timestamp;
    
    /**
     * Sequence number for ordering updates
     */
    private long sequenceNumber;
    
    /**
     * Market status: "OPEN", "CLOSED", "HALTED"
     */
    private String marketStatus;
    
    /**
     * Price level representing a price point with quantity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PriceLevel {
        private double price;
        private long quantity;
        private int orderCount;
    }
}
