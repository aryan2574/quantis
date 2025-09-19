package com.quantis.update_service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

/**
 * Trade model representing an executed trade from the trading engine.
 * This model is used for deserializing trade messages from Kafka.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {
    
    /**
     * Unique trade identifier
     */
    private String tradeId;
    
    /**
     * Original order identifier that generated this trade
     */
    private String orderId;
    
    /**
     * User identifier who placed the order
     */
    private String userId;
    
    /**
     * Trading symbol (e.g., "AAPL", "GOOGL")
     */
    private String symbol;
    
    /**
     * Trade side: "BUY" or "SELL"
     */
    private String side;
    
    /**
     * Number of shares/units traded
     */
    private long quantity;
    
    /**
     * Price per share/unit
     */
    private double price;
    
    /**
     * Total value of the trade (quantity * price)
     */
    private double totalValue;
    
    /**
     * Timestamp when the trade was executed
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant executedAt;
    
    /**
     * Trade status: "EXECUTED", "SETTLED", "FAILED"
     */
    private String status;
    
    /**
     * Counter party order ID (for matched trades)
     */
    private String counterpartyOrderId;
    
    /**
     * Market session when trade occurred
     */
    private String marketSession;
    
    /**
     * Additional trade metadata
     */
    private String metadata;
}
