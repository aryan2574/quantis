package com.quantis.portfolio_service.repository;

import com.quantis.portfolio_service.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Position entities.
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, UUID> {
    
    /**
     * Find position by user ID and symbol
     */
    Optional<Position> findByUserIdAndSymbol(UUID userId, String symbol);
    
    /**
     * Find all positions for a user
     */
    List<Position> findByUserId(UUID userId);
    
    /**
     * Check if position exists for user and symbol
     */
    boolean existsByUserIdAndSymbol(UUID userId, String symbol);
    
    /**
     * Get total portfolio value for a user
     */
    @Query("SELECT COALESCE(SUM(p.marketValue), 0) FROM Position p WHERE p.userId = :userId")
    Double getTotalPortfolioValue(@Param("userId") UUID userId);
    
    /**
     * Get total unrealized P&L for a user
     */
    @Query("SELECT COALESCE(SUM(p.unrealizedPnl), 0) FROM Position p WHERE p.userId = :userId")
    Double getTotalUnrealizedPnl(@Param("userId") UUID userId);
    
    /**
     * Find positions with non-zero quantity
     */
    @Query("SELECT p FROM Position p WHERE p.userId = :userId AND p.quantity != 0")
    List<Position> findActivePositions(@Param("userId") UUID userId);
}
