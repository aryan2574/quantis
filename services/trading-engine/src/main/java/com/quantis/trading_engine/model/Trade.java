package com.quantis.trading_engine.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * Trade model representing executed trades.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {
    
    private String tradeId;
    private String orderId;
    private String userId;
    private String symbol;
    private String side;   // BUY or SELL
    private long quantity;
    private double price;
    private double totalValue;
    private Instant executedAt;
    private TradeStatus status;
    
    public enum TradeStatus {
        EXECUTED,
        SETTLED,
        FAILED
    }
}
