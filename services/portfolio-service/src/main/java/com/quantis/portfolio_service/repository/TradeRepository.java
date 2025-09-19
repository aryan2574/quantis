package com.quantis.portfolio_service.repository;

import com.quantis.portfolio_service.entity.Trade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Trade entities.
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, UUID> {
    
    /**
     * Find trade by order ID
     */
    Optional<Trade> findByOrderId(String orderId);
    
    /**
     * Find trades for a user with pagination
     */
    Page<Trade> findByUserIdOrderByExecutedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Find trades for a user within time range
     */
    @Query("SELECT t FROM Trade t WHERE t.userId = :userId " +
           "AND t.executedAt >= :startTime AND t.executedAt <= :endTime " +
           "ORDER BY t.executedAt DESC")
    Page<Trade> findTradesByUserAndTimeRange(
        @Param("userId") UUID userId,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime,
        Pageable pageable
    );
    
    /**
     * Get total trading volume for a user
     */
    @Query("SELECT COALESCE(SUM(t.totalValue), 0) FROM Trade t WHERE t.userId = :userId")
    Double getTotalTradingVolume(@Param("userId") UUID userId);
    
    /**
     * Check if trade exists by order ID
     */
    boolean existsByOrderId(String orderId);
}
