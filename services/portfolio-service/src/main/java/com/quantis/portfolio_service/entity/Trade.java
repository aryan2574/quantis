package com.quantis.portfolio_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a completed trade/transaction.
 * This tracks the execution history for audit and analysis purposes.
 */
@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;
    
    @Column(name = "side", nullable = false, length = 4)
    @Enumerated(EnumType.STRING)
    private TradeSide side;
    
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;
    
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;
    
    @Column(name = "total_value", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalValue;
    
    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TradeStatus status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (executedAt == null) {
            executedAt = Instant.now();
        }
    }
    
    public enum TradeSide {
        BUY,
        SELL
    }
    
    public enum TradeStatus {
        EXECUTED,
        CANCELLED,
        FAILED
    }
}
