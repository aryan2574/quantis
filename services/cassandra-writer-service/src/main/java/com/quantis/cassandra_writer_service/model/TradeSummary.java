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

/**
 * Cassandra entity for storing daily trade summaries.
 * 
 * This table provides aggregated statistics for efficient reporting and analytics.
 * Data is aggregated by symbol and date for fast querying.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("trade_summaries")
public class TradeSummary {
    
    /**
     * Primary key: (symbol, summary_date)
     */
    @PrimaryKey
    private TradeSummaryKey key;
    
    /**
     * Total number of trades for this symbol on this date
     */
    @Column("total_trades")
    private Long totalTrades;
    
    /**
     * Total volume traded
     */
    @Column("total_volume")
    private BigDecimal totalVolume;
    
    /**
     * Total value traded
     */
    @Column("total_value")
    private BigDecimal totalValue;
    
    /**
     * Opening price (first trade of the day)
     */
    @Column("open_price")
    private BigDecimal openPrice;
    
    /**
     * Closing price (last trade of the day)
     */
    @Column("close_price")
    private BigDecimal closePrice;
    
    /**
     * Highest price during the day
     */
    @Column("high_price")
    private BigDecimal highPrice;
    
    /**
     * Lowest price during the day
     */
    @Column("low_price")
    private BigDecimal lowPrice;
    
    /**
     * Volume-weighted average price
     */
    @Column("vwap")
    private BigDecimal vwap;
    
    /**
     * Number of unique users who traded
     */
    @Column("unique_users")
    private Long uniqueUsers;
    
    /**
     * Number of buy orders
     */
    @Column("buy_orders")
    private Long buyOrders;
    
    /**
     * Number of sell orders
     */
    @Column("sell_orders")
    private Long sellOrders;
    
    /**
     * Last updated timestamp
     */
    @Column("last_updated")
    private Instant lastUpdated;
    
    /**
     * Composite primary key class
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TradeSummaryKey {
        
        /**
         * Trading symbol (partition key)
         */
        @Column("symbol")
        private String symbol;
        
        /**
         * Summary date in YYYY-MM-DD format (clustering key)
         */
        @Column("summary_date")
        private String summaryDate;
    }
}
