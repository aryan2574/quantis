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
 * Entity representing a user's position in a specific trading symbol.
 * A position tracks the quantity and average price of holdings.
 */
@Entity
@Table(name = "positions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "symbol"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Position {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "symbol", nullable = false, length = 10)
    private String symbol;
    
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;
    
    @Column(name = "average_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal averagePrice;
    
    @Column(name = "current_price", precision = 20, scale = 8)
    private BigDecimal currentPrice;
    
    @Column(name = "market_value", precision = 20, scale = 8)
    private BigDecimal marketValue;
    
    @Column(name = "unrealized_pnl", precision = 20, scale = 8)
    private BigDecimal unrealizedPnl;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
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
    
    /**
     * Calculate market value based on current price and quantity
     */
    public void calculateMarketValue() {
        if (currentPrice != null && quantity != null) {
            marketValue = currentPrice.multiply(quantity);
        }
    }
    
    /**
     * Calculate unrealized P&L
     */
    public void calculateUnrealizedPnl() {
        if (marketValue != null && averagePrice != null && quantity != null) {
            BigDecimal costBasis = averagePrice.multiply(quantity);
            unrealizedPnl = marketValue.subtract(costBasis);
        }
    }
}
