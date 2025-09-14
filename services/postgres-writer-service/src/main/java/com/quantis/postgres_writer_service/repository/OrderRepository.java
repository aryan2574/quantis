package com.quantis.postgres_writer_service.repository;

import com.quantis.postgres_writer_service.model.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for OrderEntity operations.
 * Provides data access methods for order persistence and queries.
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
    
    /**
     * Find order by order ID (unique identifier from order ingress)
     */
    Optional<OrderEntity> findByOrderId(String orderId);
    
    /**
     * Find orders by user ID
     */
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find orders by symbol
     */
    List<OrderEntity> findBySymbolOrderByCreatedAtDesc(String symbol);
    
    /**
     * Find orders by user ID and symbol
     */
    List<OrderEntity> findByUserIdAndSymbolOrderByCreatedAtDesc(UUID userId, String symbol);
    
    /**
     * Find orders by status
     */
    List<OrderEntity> findByStatusOrderByCreatedAtDesc(OrderEntity.OrderStatus status);
    
    /**
     * Find orders by user and status
     */
    List<OrderEntity> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, OrderEntity.OrderStatus status);
    
    /**
     * Find orders within time range
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt >= :startTime AND o.createdAt <= :endTime ORDER BY o.createdAt DESC")
    List<OrderEntity> findOrdersByTimeRange(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    /**
     * Find orders by user within time range
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId AND o.createdAt >= :startTime AND o.createdAt <= :endTime ORDER BY o.createdAt DESC")
    List<OrderEntity> findOrdersByUserAndTimeRange(@Param("userId") UUID userId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    /**
     * Get order count by user
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.userId = :userId")
    Long getOrderCountByUser(@Param("userId") UUID userId);
    
    /**
     * Get order count by symbol
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.symbol = :symbol")
    Long getOrderCountBySymbol(@Param("symbol") String symbol);
    
    /**
     * Get order count by status
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.status = :status")
    Long getOrderCountByStatus(@Param("status") OrderEntity.OrderStatus status);
    
    /**
     * Get total order value by user
     */
    @Query("SELECT COALESCE(SUM(o.quantity * o.price), 0) FROM OrderEntity o WHERE o.userId = :userId")
    Double getTotalOrderValueByUser(@Param("userId") UUID userId);
    
    /**
     * Get total order value by user and symbol
     */
    @Query("SELECT COALESCE(SUM(o.quantity * o.price), 0) FROM OrderEntity o WHERE o.userId = :userId AND o.symbol = :symbol")
    Double getTotalOrderValueByUserAndSymbol(@Param("userId") UUID userId, @Param("symbol") String symbol);
    
    /**
     * Check if order exists by order ID
     */
    boolean existsByOrderId(String orderId);
    
    /**
     * Get order statistics for a user
     */
    @Query("SELECT " +
           "COUNT(o) as totalOrders, " +
           "SUM(o.quantity * o.price) as totalValue, " +
           "AVG(o.price) as averagePrice, " +
           "MIN(o.createdAt) as firstOrder, " +
           "MAX(o.createdAt) as lastOrder " +
           "FROM OrderEntity o WHERE o.userId = :userId")
    Object[] getOrderStatisticsByUser(@Param("userId") UUID userId);
    
    /**
     * Find active orders (PENDING or PARTIALLY_EXECUTED)
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.status IN ('PENDING', 'PARTIALLY_EXECUTED') ORDER BY o.createdAt ASC")
    List<OrderEntity> findActiveOrders();
    
    /**
     * Find active orders by user
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.userId = :userId AND o.status IN ('PENDING', 'PARTIALLY_EXECUTED') ORDER BY o.createdAt ASC")
    List<OrderEntity> findActiveOrdersByUser(@Param("userId") UUID userId);
}
