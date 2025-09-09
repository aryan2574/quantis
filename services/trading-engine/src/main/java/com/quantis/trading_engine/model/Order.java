package com.quantis.trading_engine.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * Order model for the Trading Engine.
 * Represents orders received from the Risk Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    private String orderId;
    private String userId;
    private String symbol;
    private String side;   // BUY or SELL
    private long quantity;
    private double price;
    private Instant receivedAt;
    private OrderStatus status;
    
    public enum OrderStatus {
        PENDING,
        EXECUTED,
        PARTIALLY_EXECUTED,
        REJECTED,
        CANCELLED
    }
}
