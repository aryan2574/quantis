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
 * JPA entity representing an order in the database.
 * This tracks the complete lifecycle of orders from submission to execution.
 */
@Entity
@Table(name = "orders", 
       indexes = {
           @Index(name = "idx_orders_user_id", columnList = "user_id"),
           @Index(name = "idx_orders_symbol", columnList = "symbol"),
           @Index(name = "idx_orders_created_at", columnList = "created_at"),
           @Index(name = "idx_orders_order_id", columnList = "order_id", unique = true),
           @Index(name = "idx_orders_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    /**
     * Unique order identifier from the order ingress service
     */
    @Column(name = "order_id", nullable = false, unique = true, length = 255)
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
     * Order side: "BUY" or "SELL"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private OrderSide side;
    
    /**
     * Number of shares/units to trade
     */
    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;
    
    /**
     * Price per share/unit (limit price)
     */
    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;
    
    /**
     * Order status: "PENDING", "EXECUTED", "PARTIALLY_EXECUTED", "REJECTED", "CANCELLED"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;
    
    /**
     * Order type: "MARKET", "LIMIT", "STOP", "STOP_LIMIT"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 20)
    private OrderType orderType;
    
    /**
     * Time in force: "DAY", "GTC", "IOC", "FOK"
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "time_in_force", nullable = false, length = 10)
    private TimeInForce timeInForce;
    
    /**
     * Timestamp when order was received
     */
    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;
    
    /**
     * Timestamp when order was executed (if applicable)
     */
    @Column(name = "executed_at")
    private Instant executedAt;
    
    /**
     * Quantity that has been filled
     */
    @Column(name = "filled_quantity", precision = 20, scale = 8)
    private BigDecimal filledQuantity;
    
    /**
     * Average execution price
     */
    @Column(name = "average_price", precision = 20, scale = 8)
    private BigDecimal averagePrice;
    
    /**
     * Commission charged for the order
     */
    @Column(name = "commission", precision = 20, scale = 8)
    private BigDecimal commission;
    
    /**
     * Additional order metadata (JSON)
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
    
    public enum OrderSide {
        BUY, SELL
    }
    
    public enum OrderStatus {
        PENDING, EXECUTED, PARTIALLY_EXECUTED, REJECTED, CANCELLED, FAILED
    }
    
    public enum OrderType {
        MARKET, LIMIT, STOP, STOP_LIMIT
    }
    
    public enum TimeInForce {
        DAY, GTC, IOC, FOK
    }
}
