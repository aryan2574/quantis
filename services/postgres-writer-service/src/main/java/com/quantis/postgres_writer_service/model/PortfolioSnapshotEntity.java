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
 * JPA entity representing a portfolio snapshot in the database.
 * This tracks portfolio state changes over time for audit and analytics.
 */
@Entity
@Table(name = "portfolio_snapshots", 
       indexes = {
           @Index(name = "idx_portfolio_snapshots_user_id", columnList = "user_id"),
           @Index(name = "idx_portfolio_snapshots_symbol", columnList = "symbol"),
           @Index(name = "idx_portfolio_snapshots_created_at", columnList = "created_at"),
           @Index(name = "idx_portfolio_snapshots_user_symbol", columnList = "user_id, symbol")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioSnapshotEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    /**
     * User identifier
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    /**
     * Trading symbol (e.g., "AAPL", "GOOGL")
     */
    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;
    
    /**
     * Current position quantity (positive for long, negative for short)
     */
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;
    
    /**
     * Average cost basis
     */
    @Column(name = "average_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal averagePrice;
    
    /**
     * Current market price
     */
    @Column(name = "current_price", precision = 20, scale = 8)
    private BigDecimal currentPrice;
    
    /**
     * Market value of position (quantity * current_price)
     */
    @Column(name = "market_value", precision = 20, scale = 8)
    private BigDecimal marketValue;
    
    /**
     * Unrealized P&L (market_value - cost_basis)
     */
    @Column(name = "unrealized_pnl", precision = 20, scale = 8)
    private BigDecimal unrealizedPnl;
    
    /**
     * Realized P&L from closed positions
     */
    @Column(name = "realized_pnl", precision = 20, scale = 8)
    private BigDecimal realizedPnl;
    
    /**
     * Cash balance
     */
    @Column(name = "cash_balance", precision = 20, scale = 8)
    private BigDecimal cashBalance;
    
    /**
     * Total portfolio value
     */
    @Column(name = "total_portfolio_value", precision = 20, scale = 8)
    private BigDecimal totalPortfolioValue;
    
    /**
     * Currency of the portfolio
     */
    @Column(name = "currency", length = 3)
    private String currency;
    
    /**
     * Timestamp when snapshot was taken
     */
    @Column(name = "snapshot_at", nullable = false)
    private Instant snapshotAt;
    
    /**
     * Trade ID that triggered this snapshot (if applicable)
     */
    @Column(name = "trade_id", length = 255)
    private String tradeId;
    
    /**
     * Snapshot type: "TRADE", "DAILY", "MANUAL"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "snapshot_type", nullable = false, length = 20)
    private SnapshotType snapshotType;
    
    /**
     * Additional snapshot metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    /**
     * Timestamp when record was created
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
    
    public enum SnapshotType {
        TRADE, DAILY, MANUAL, SYSTEM
    }
}
