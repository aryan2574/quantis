package com.quantis.postgres_writer_service.repository;

import com.quantis.postgres_writer_service.model.PortfolioSnapshotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for PortfolioSnapshotEntity operations.
 * Provides data access methods for portfolio snapshot persistence and queries.
 */
@Repository
public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshotEntity, UUID> {
    
    /**
     * Find latest portfolio snapshot for a user
     */
    @Query("SELECT p FROM PortfolioSnapshotEntity p WHERE p.userId = :userId ORDER BY p.snapshotAt DESC")
    List<PortfolioSnapshotEntity> findLatestByUserId(@Param("userId") UUID userId, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find latest portfolio snapshot for a user and symbol
     */
    @Query("SELECT p FROM PortfolioSnapshotEntity p WHERE p.userId = :userId AND p.symbol = :symbol ORDER BY p.snapshotAt DESC")
    List<PortfolioSnapshotEntity> findLatestByUserIdAndSymbol(@Param("userId") UUID userId, @Param("symbol") String symbol, org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find portfolio snapshots by user and time range
     */
    @Query("SELECT p FROM PortfolioSnapshotEntity p WHERE p.userId = :userId AND p.snapshotAt >= :startTime AND p.snapshotAt <= :endTime ORDER BY p.snapshotAt DESC")
    List<PortfolioSnapshotEntity> findByUserIdAndTimeRange(@Param("userId") UUID userId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    /**
     * Find portfolio snapshots by symbol and time range
     */
    @Query("SELECT p FROM PortfolioSnapshotEntity p WHERE p.symbol = :symbol AND p.snapshotAt >= :startTime AND p.snapshotAt <= :endTime ORDER BY p.snapshotAt DESC")
    List<PortfolioSnapshotEntity> findBySymbolAndTimeRange(@Param("symbol") String symbol, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    /**
     * Find portfolio snapshots by snapshot type
     */
    List<PortfolioSnapshotEntity> findBySnapshotTypeOrderBySnapshotAtDesc(PortfolioSnapshotEntity.SnapshotType snapshotType);
    
    /**
     * Find portfolio snapshots by user and snapshot type
     */
    List<PortfolioSnapshotEntity> findByUserIdAndSnapshotTypeOrderBySnapshotAtDesc(UUID userId, PortfolioSnapshotEntity.SnapshotType snapshotType);
    
    /**
     * Find portfolio snapshots by trade ID
     */
    List<PortfolioSnapshotEntity> findByTradeIdOrderBySnapshotAtDesc(String tradeId);
    
    /**
     * Get total portfolio value for a user at a specific time
     */
    @Query("SELECT COALESCE(SUM(p.totalPortfolioValue), 0) FROM PortfolioSnapshotEntity p WHERE p.userId = :userId AND p.snapshotAt <= :snapshotTime ORDER BY p.snapshotAt DESC")
    Double getTotalPortfolioValueAtTime(@Param("userId") UUID userId, @Param("snapshotTime") Instant snapshotTime);
    
    /**
     * Get portfolio performance over time for a user
     */
    @Query("SELECT p.snapshotAt, p.totalPortfolioValue, p.cashBalance FROM PortfolioSnapshotEntity p WHERE p.userId = :userId ORDER BY p.snapshotAt ASC")
    List<Object[]> getPortfolioPerformanceByUser(@Param("userId") UUID userId);
    
    /**
     * Get position summary for a user
     */
    @Query("SELECT p.symbol, p.quantity, p.averagePrice, p.currentPrice, p.marketValue, p.unrealizedPnl FROM PortfolioSnapshotEntity p WHERE p.userId = :userId AND p.snapshotAt = (SELECT MAX(p2.snapshotAt) FROM PortfolioSnapshotEntity p2 WHERE p2.userId = :userId AND p2.symbol = p.symbol)")
    List<Object[]> getPositionSummaryByUser(@Param("userId") UUID userId);
    
    /**
     * Get snapshot count by user
     */
    @Query("SELECT COUNT(p) FROM PortfolioSnapshotEntity p WHERE p.userId = :userId")
    Long getSnapshotCountByUser(@Param("userId") UUID userId);
    
    /**
     * Get snapshot count by symbol
     */
    @Query("SELECT COUNT(p) FROM PortfolioSnapshotEntity p WHERE p.symbol = :symbol")
    Long getSnapshotCountBySymbol(@Param("symbol") String symbol);
    
    /**
     * Find portfolio snapshots created after a specific trade
     */
    @Query("SELECT p FROM PortfolioSnapshotEntity p WHERE p.userId = :userId AND p.snapshotAt > (SELECT t.executedAt FROM TradeEntity t WHERE t.tradeId = :tradeId) ORDER BY p.snapshotAt ASC")
    List<PortfolioSnapshotEntity> findSnapshotsAfterTrade(@Param("userId") UUID userId, @Param("tradeId") String tradeId);
    
    /**
     * Get daily portfolio snapshots for a user
     */
    @Query("SELECT p FROM PortfolioSnapshotEntity p WHERE p.userId = :userId AND p.snapshotType = 'DAILY' ORDER BY p.snapshotAt DESC")
    List<PortfolioSnapshotEntity> findDailySnapshotsByUser(@Param("userId") UUID userId);
    
    /**
     * Get latest snapshot for each symbol for a user
     */
    @Query("SELECT p FROM PortfolioSnapshotEntity p WHERE p.userId = :userId AND p.snapshotAt = (SELECT MAX(p2.snapshotAt) FROM PortfolioSnapshotEntity p2 WHERE p2.userId = :userId AND p2.symbol = p.symbol)")
    List<PortfolioSnapshotEntity> findLatestSnapshotsByUser(@Param("userId") UUID userId);
}
