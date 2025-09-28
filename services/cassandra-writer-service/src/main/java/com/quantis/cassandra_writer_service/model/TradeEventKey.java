package com.quantis.cassandra_writer_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;

/**
 * Composite primary key for TradeEvent table.
 * 
 * This key is optimized for time-series queries:
 * - symbol: Partition key (distributes data across nodes)
 * - trade_date: Clustering key (enables efficient time-range queries)
 * - trade_id: Clustering key (ensures uniqueness and ordering)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PrimaryKeyClass
public class TradeEventKey {
    
    /**
     * Partition key: Symbol (e.g., BTCUSD, AAPL)
     * Distributes data across Cassandra nodes
     */
    @PrimaryKeyColumn(name = "symbol", type = PARTITIONED)
    private String symbol;
    
    /**
     * Clustering key: Trade date (YYYY-MM-DD format)
     * Enables efficient time-range queries
     */
    @PrimaryKeyColumn(name = "trade_date", type = CLUSTERED)
    private LocalDate tradeDate;
    
    /**
     * Clustering key: Unique trade identifier
     * Ensures uniqueness and provides ordering within a date
     */
    @PrimaryKeyColumn(name = "trade_id", type = CLUSTERED)
    private UUID tradeId;
}
