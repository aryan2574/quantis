package com.quantis.cassandra_writer_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Cassandra entity for storing time-series trade events.
 * 
 * This table is optimized for high-throughput writes and time-range queries.
 * The primary key is designed for efficient partitioning and clustering.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("trade_events")
public class TradeEvent {
    
    /**
     * Primary key: (symbol, trade_date, trade_id)
     * - symbol: Partition key for efficient querying by symbol
     * - trade_date: Clustering key for time-based queries (YYYY-MM-DD format)
     * - trade_id: Unique identifier for the trade
     */
    @PrimaryKey
    private TradeEventKey key;
    
    /**
     * User ID who executed the trade
     */
    @Column("user_id")
    private UUID userId;
    
    /**
     * Order ID that generated this trade
     */
    @Column("order_id")
    private String orderId;
    
    /**
     * Trade side (BUY/SELL)
     */
    @Column("side")
    private String side;
    
    /**
     * Quantity traded
     */
    @Column("quantity")
    private BigDecimal quantity;
    
    /**
     * Price per unit
     */
    @Column("price")
    private BigDecimal price;
    
    /**
     * Total trade value (quantity * price)
     */
    @Column("total_value")
    private BigDecimal totalValue;
    
    /**
     * Trade execution timestamp (nanoseconds precision)
     */
    @Column("executed_at")
    private Instant executedAt;
    
    /**
     * Trade status
     */
    @Column("status")
    private String status;
    
    /**
     * Counterparty order ID (for matching)
     */
    @Column("counterparty_order_id")
    private String counterpartyOrderId;
    
    /**
     * Market session identifier
     */
    @Column("market_session")
    private String marketSession;
    
    /**
     * Additional metadata (JSON string)
     */
    @Column("metadata")
    private String metadata;
    
    /**
     * Raw trade data as received from Kafka (for audit/debugging)
     */
    @Column("raw_data")
    private String rawData;
    
    /**
     * Processing timestamp when this record was written
     */
    @Column("processed_at")
    private Instant processedAt;
    
    /**
     * Sequence number for ordering within the same trade_date
     */
    @Column("sequence_number")
    private Long sequenceNumber;
    
    /**
     * Trade event type (EXECUTION, CANCELLATION, etc.)
     */
    @Column("event_type")
    private String eventType;
    
    /**
     * Market data snapshot at time of trade
     */
    @Column("market_data_snapshot")
    private String marketDataSnapshot;
    
    /**
     * Composite primary key class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TradeEventKey {
        
        /**
         * Trading symbol (partition key)
         */
        @Column("symbol")
        private String symbol;
        
        /**
         * Trade date in YYYY-MM-DD format (clustering key)
         */
        @Column("trade_date")
        private String tradeDate;
        
        /**
         * Unique trade identifier (clustering key)
         */
        @Column("trade_id")
        private String tradeId;
    }
}
