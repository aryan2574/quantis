package com.quantis.postgres_writer_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a trade execution in the database.
 * This is the persistent representation of trade data from the trading engine.
 */
@Entity
@Table(name = "trades", 
       indexes = {
           @Index(name = "idx_trades_user_id", columnList = "user_id"),
           @Index(name = "idx_trades_symbol", columnList = "symbol"),
           @Index(name = "idx_trades_executed_at", columnList = "executed_at"),
           @Index(name = "idx_trades_order_id", columnList = "order_id"),
           @Index(name = "idx_trades_trade_id", columnList = "trade_id", unique = true)
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Unique trade identifier from the trading engine
     */
    @Column(name = "trade_id", nullable = false, unique = true, length = 255)
    private String tradeId;
    
    /**
     * Original order identifier that generated this trade
     */
    @Column(name = "order_id", nullable = false, length = 255)
    private String orderId;
    
    /**
     * User identifier who placed the order
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    /**
     * Trading symbol (e.g., "AAPL", "GOOGL")
     */
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;
    
    /**
     * Trade side: "BUY" or "SELL"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private TradeSide side;
    
    /**
     * Number of shares/units traded
     */
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;
    
    /**
     * Price per share/unit
     */
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;
    
    /**
     * Total value of the trade (quantity * price)
     */
    @Column(name = "total_value", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalValue;
    
    /**
     * Timestamp when the trade was executed
     */
    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;
    
    /**
     * Trade status: "EXECUTED", "SETTLED", "FAILED"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TradeStatus status;
    
    /**
     * Counter party order ID (for matched trades)
     */
    @Column(name = "counterparty_order_id", length = 255)
    private String counterpartyOrderId;
    
    /**
     * Market session when trade occurred
     */
    @Column(name = "market_session", length = 50)
    private String marketSession;
    
    /**
     * Additional trade metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Timestamp when record was created
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    /**
     * Timestamp when record was last updated
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    public enum TradeSide {
        BUY, SELL
    }
    
    public enum TradeStatus {
        EXECUTED, SETTLED, FAILED, CANCELLED
    }
}
