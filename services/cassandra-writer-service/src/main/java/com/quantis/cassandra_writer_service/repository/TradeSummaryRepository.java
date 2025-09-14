package com.quantis.cassandra_writer_service.repository;

import com.quantis.cassandra_writer_service.model.TradeSummary;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for accessing trade summaries in Cassandra.
 * 
 * This repository provides queries for aggregated trade statistics.
 */
@Repository
public interface TradeSummaryRepository extends CassandraRepository<TradeSummary, TradeSummary.TradeSummaryKey> {
    
    /**
     * Find trade summary for a specific symbol and date
     */
    @Query("SELECT * FROM trade_summaries WHERE symbol = :symbol AND summary_date = :summaryDate")
    TradeSummary findBySymbolAndDate(@Param("symbol") String symbol, @Param("summaryDate") String summaryDate);
    
    /**
     * Find trade summaries for a symbol within date range
     */
    @Query("SELECT * FROM trade_summaries WHERE symbol = :symbol AND summary_date >= :startDate AND summary_date <= :endDate")
    List<TradeSummary> findBySymbolAndDateRange(
        @Param("symbol") String symbol,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );
    
    /**
     * Find latest trade summaries for all symbols
     */
    @Query("SELECT * FROM trade_summaries WHERE summary_date = :summaryDate")
    List<TradeSummary> findByDate(@Param("summaryDate") String summaryDate);
    
    /**
     * Find top symbols by volume for a specific date
     */
    @Query("SELECT * FROM trade_summaries WHERE summary_date = :summaryDate ORDER BY total_volume DESC LIMIT :limit")
    List<TradeSummary> findTopByVolumeOnDate(@Param("summaryDate") String summaryDate, @Param("limit") int limit);
    
    /**
     * Find top symbols by trade count for a specific date
     */
    @Query("SELECT * FROM trade_summaries WHERE summary_date = :summaryDate ORDER BY total_trades DESC LIMIT :limit")
    List<TradeSummary> findTopByTradeCountOnDate(@Param("summaryDate") String summaryDate, @Param("limit") int limit);
}
