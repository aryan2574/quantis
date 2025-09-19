package com.quantis.postgres_writer_service.repository;

import com.quantis.postgres_writer_service.model.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for TradeEntity operations.
 * Provides data access methods for trade persistence and queries.
 */
@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {
    
    /**
     * Find trade by trade ID (unique identifier from trading engine)
     */
    Optional<TradeEntity> findByTradeId(String tradeId);
    
    /**
     * Find trades by user ID with pagination
     */
    List<TradeEntity> findByUserIdOrderByExecutedAtDesc(UUID userId);
    
    /**
     * Find trades by symbol with pagination
     */
    List<TradeEntity> findBySymbolOrderByExecutedAtDesc(String symbol);
    
    /**
     * Find trades by user ID and symbol
     */
    List<TradeEntity> findByUserIdAndSymbolOrderByExecutedAtDesc(UUID userId, String symbol);
    
    /**
     * Find trades within time range
     */
    @Query("SELECT t FROM TradeEntity t WHERE t.executedAt >= :startTime AND t.executedAt <= :endTime ORDER BY t.executedAt DESC")
    List<TradeEntity> findTradesByTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    /**
     * Find trades by user within time range
     */
    @Query("SELECT t FROM TradeEntity t WHERE t.userId = :userId AND t.executedAt >= :startTime AND t.executedAt <= :endTime ORDER BY t.executedAt DESC")
    List<TradeEntity> findTradesByUserAndTimeRange(@Param("userId") UUID userId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    /**
     * Get total trading volume for a user
     */
    @Query("SELECT COALESCE(SUM(t.totalValue), 0) FROM TradeEntity t WHERE t.userId = :userId")
    Double getTotalTradingVolume(@Param("userId") UUID userId);
    
    /**
     * Get total trading volume for a user and symbol
     */
    @Query("SELECT COALESCE(SUM(t.totalValue), 0) FROM TradeEntity t WHERE t.userId = :userId AND t.symbol = :symbol")
    Double getTotalTradingVolumeByUserAndSymbol(@Param("userId") UUID userId, @Param("symbol") String symbol);
    
    /**
     * Get trade count by user
     */
    @Query("SELECT COUNT(t) FROM TradeEntity t WHERE t.userId = :userId")
    Long getTradeCountByUser(@Param("userId") UUID userId);
    
    /**
     * Get trade count by symbol
     */
    @Query("SELECT COUNT(t) FROM TradeEntity t WHERE t.symbol = :symbol")
    Long getTradeCountBySymbol(@Param("symbol") String symbol);
    
    /**
     * Check if trade exists by trade ID
     */
    boolean existsByTradeId(String tradeId);
    
    /**
     * Get latest trades for a symbol (for market data)
     */
    @Query("SELECT t FROM TradeEntity t WHERE t.symbol = :symbol ORDER BY t.executedAt DESC")
    List<TradeEntity> findLatestTradesBySymbol(@Param("symbol") String symbol, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Get trade statistics for a user
     */
    @Query("SELECT " +
           "COUNT(t) as totalTrades, " +
           "SUM(t.totalValue) as totalVolume, " +
           "AVG(t.price) as averagePrice, " +
           "MIN(t.executedAt) as firstTrade, " +
           "MAX(t.executedAt) as lastTrade " +
           "FROM TradeEntity t WHERE t.userId = :userId")
    Object[] getTradeStatisticsByUser(@Param("userId") UUID userId);
}
