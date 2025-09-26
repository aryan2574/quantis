package com.quantis.cassandra_writer_service.repository;

import com.quantis.cassandra_writer_service.model.TradeEvent;
import com.quantis.cassandra_writer_service.model.TradeEventKey;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing trade events in Cassandra.
 * 
 * This repository provides optimized queries for time-series trade data.
 */
@Repository
public interface TradeEventRepository extends CassandraRepository<TradeEvent, TradeEventKey> {
    
    /**
     * Find all trade events for a specific symbol and date range
     */
    @Query("SELECT * FROM trade_events WHERE symbol = :symbol AND trade_date >= :startDate AND trade_date <= :endDate")
    List<TradeEvent> findBySymbolAndDateRange(
        @Param("symbol") String symbol,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );
    
    /**
     * Find trade events for a specific user
     */
    @Query("SELECT * FROM trade_events WHERE user_id = :userId")
    List<TradeEvent> findByUserId(@Param("userId") UUID userId);
    
    /**
     * Find trade events by symbol and executed timestamp range
     */
    @Query("SELECT * FROM trade_events WHERE symbol = :symbol AND executed_at >= :startTime AND executed_at <= :endTime")
    List<TradeEvent> findBySymbolAndExecutedAtRange(
        @Param("symbol") String symbol,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );
    
    /**
     * Find latest trade events for a symbol (limited by count)
     */
    @Query("SELECT * FROM trade_events WHERE symbol = :symbol ORDER BY trade_date DESC, sequence_number DESC LIMIT :limit")
    List<TradeEvent> findLatestBySymbol(@Param("symbol") String symbol, @Param("limit") int limit);
    
    /**
     * Find trade events by order ID
     */
    @Query("SELECT * FROM trade_events WHERE order_id = :orderId")
    List<TradeEvent> findByOrderId(@Param("orderId") String orderId);
    
    /**
     * Count trades for a symbol on a specific date
     */
    @Query("SELECT COUNT(*) FROM trade_events WHERE symbol = :symbol AND trade_date = :tradeDate")
    Long countBySymbolAndDate(@Param("symbol") String symbol, @Param("tradeDate") String tradeDate);
    
    /**
     * Find trade events by status
     */
    @Query("SELECT * FROM trade_events WHERE status = :status")
    List<TradeEvent> findByStatus(@Param("status") String status);
    
    /**
     * Find trade events by symbol and side
     */
    @Query("SELECT * FROM trade_events WHERE symbol = :symbol AND side = :side")
    List<TradeEvent> findBySymbolAndSide(@Param("symbol") String symbol, @Param("side") String side);
}
