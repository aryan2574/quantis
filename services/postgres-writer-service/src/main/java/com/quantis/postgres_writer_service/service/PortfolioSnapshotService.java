package com.quantis.postgres_writer_service.service;

import com.quantis.postgres_writer_service.model.PortfolioSnapshotEntity;
import com.quantis.postgres_writer_service.model.TradeEntity;
import com.quantis.postgres_writer_service.repository.PortfolioSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing portfolio snapshots.
 * 
 * This service:
 * 1. Creates portfolio snapshots from trade executions
 * 2. Maintains position tracking and P&L calculations
 * 3. Provides portfolio state management
 * 4. Supports audit trails and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSnapshotService {
    
    private final PortfolioSnapshotRepository portfolioSnapshotRepository;
    
    /**
     * Create a portfolio snapshot from a trade execution
     */
    @Transactional
    public void createSnapshotFromTrade(TradeEntity trade) {
        try {
            UUID userId = trade.getUserId();
            String symbol = trade.getSymbol();
            
            // Get the latest snapshot for this user and symbol
            List<PortfolioSnapshotEntity> latestSnapshots = portfolioSnapshotRepository
                .findLatestByUserIdAndSymbol(userId, symbol, org.springframework.data.domain.PageRequest.of(0, 1));
            
            PortfolioSnapshotEntity previousSnapshot = latestSnapshots.isEmpty() ? null : latestSnapshots.get(0);
            
            // Calculate new position
            PositionCalculation positionCalc = calculateNewPosition(previousSnapshot, trade);
            
            // Create new snapshot
            PortfolioSnapshotEntity newSnapshot = PortfolioSnapshotEntity.builder()
                .userId(userId)
                .symbol(symbol)
                .quantity(positionCalc.newQuantity)
                .averagePrice(positionCalc.newAveragePrice)
                .currentPrice(trade.getPrice())
                .marketValue(positionCalc.newMarketValue)
                .unrealizedPnl(positionCalc.newUnrealizedPnl)
                .realizedPnl(positionCalc.realizedPnl)
                .cashBalance(positionCalc.newCashBalance)
                .totalPortfolioValue(positionCalc.newTotalPortfolioValue)
                .currency("USD") // Default currency
                .snapshotAt(Instant.now())
                .tradeId(trade.getTradeId())
                .snapshotType(PortfolioSnapshotEntity.SnapshotType.TRADE)
                .metadata(createSnapshotMetadata(trade))
                .build();
            
            portfolioSnapshotRepository.save(newSnapshot);
            
            log.debug("Created portfolio snapshot for user {} symbol {} after trade {}", 
                userId, symbol, trade.getTradeId());
            
        } catch (Exception e) {
            log.error("Failed to create portfolio snapshot for trade {}", trade.getTradeId(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }
    
    /**
     * Calculate new position based on previous snapshot and trade
     */
    private PositionCalculation calculateNewPosition(PortfolioSnapshotEntity previousSnapshot, TradeEntity trade) {
        BigDecimal previousQuantity = previousSnapshot != null ? previousSnapshot.getQuantity() : BigDecimal.ZERO;
        BigDecimal previousAveragePrice = previousSnapshot != null ? previousSnapshot.getAveragePrice() : BigDecimal.ZERO;
        BigDecimal previousCashBalance = previousSnapshot != null ? previousSnapshot.getCashBalance() : BigDecimal.ZERO;
        BigDecimal previousRealizedPnl = previousSnapshot != null ? previousSnapshot.getRealizedPnl() : BigDecimal.ZERO;
        
        BigDecimal tradeQuantity = trade.getQuantity();
        BigDecimal tradePrice = trade.getPrice();
        BigDecimal tradeValue = trade.getTotalValue();
        
        BigDecimal newQuantity;
        BigDecimal newAveragePrice;
        BigDecimal newCashBalance;
        BigDecimal realizedPnl = BigDecimal.ZERO;
        
        if (trade.getSide() == TradeEntity.TradeSide.BUY) {
            // Buying: increase position
            newQuantity = previousQuantity.add(tradeQuantity);
            newAveragePrice = calculateAveragePrice(previousQuantity, previousAveragePrice, tradeQuantity, tradePrice);
            newCashBalance = previousCashBalance.subtract(tradeValue); // Cash goes down
        } else {
            // Selling: decrease position
            newQuantity = previousQuantity.subtract(tradeQuantity);
            
            if (newQuantity.compareTo(BigDecimal.ZERO) >= 0) {
                // Still long position
                newAveragePrice = previousAveragePrice;
                newCashBalance = previousCashBalance.add(tradeValue); // Cash goes up
            } else {
                // Position went short or closed
                if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    // Position closed completely
                    newAveragePrice = BigDecimal.ZERO;
                    newCashBalance = previousCashBalance.add(tradeValue);
                    // Calculate realized P&L for closing position
                    realizedPnl = tradeValue.subtract(previousQuantity.multiply(previousAveragePrice));
                } else {
                    // Position went short
                    newAveragePrice = tradePrice; // New average for short position
                    newCashBalance = previousCashBalance.add(tradeValue);
                    // Calculate realized P&L for closing long and opening short
                    realizedPnl = previousQuantity.multiply(tradePrice.subtract(previousAveragePrice));
                }
            }
        }
        
        BigDecimal newMarketValue = newQuantity.multiply(tradePrice);
        BigDecimal newUnrealizedPnl = newMarketValue.subtract(newQuantity.multiply(newAveragePrice));
        BigDecimal newTotalPortfolioValue = newCashBalance.add(newMarketValue);
        
        return PositionCalculation.builder()
            .newQuantity(newQuantity)
            .newAveragePrice(newAveragePrice)
            .newCashBalance(newCashBalance)
            .newMarketValue(newMarketValue)
            .newUnrealizedPnl(newUnrealizedPnl)
            .newTotalPortfolioValue(newTotalPortfolioValue)
            .realizedPnl(previousRealizedPnl.add(realizedPnl))
            .build();
    }
    
    /**
     * Calculate average price when adding to position
     */
    private BigDecimal calculateAveragePrice(BigDecimal previousQuantity, BigDecimal previousAvgPrice, 
                                           BigDecimal newQuantity, BigDecimal newPrice) {
        if (previousQuantity.compareTo(BigDecimal.ZERO) == 0) {
            return newPrice;
        }
        
        BigDecimal previousValue = previousQuantity.multiply(previousAvgPrice);
        BigDecimal newValue = newQuantity.multiply(newPrice);
        BigDecimal totalQuantity = previousQuantity.add(newQuantity);
        BigDecimal totalValue = previousValue.add(newValue);
        
        return totalValue.divide(totalQuantity, 8, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Create snapshot metadata
     */
    private String createSnapshotMetadata(TradeEntity trade) {
        return String.format("{\"tradeId\":\"%s\",\"orderId\":\"%s\",\"side\":\"%s\",\"quantity\":%s,\"price\":%s,\"executedAt\":\"%s\"}", 
            trade.getTradeId(), trade.getOrderId(), trade.getSide(), trade.getQuantity(), trade.getPrice(), trade.getExecutedAt());
    }
    
    /**
     * Get latest portfolio snapshot for a user
     */
    public List<PortfolioSnapshotEntity> getLatestPortfolioSnapshot(UUID userId) {
        return portfolioSnapshotRepository.findLatestByUserId(userId, 
            org.springframework.data.domain.PageRequest.of(0, 1));
    }
    
    /**
     * Get latest portfolio snapshot for a user and symbol
     */
    public List<PortfolioSnapshotEntity> getLatestPortfolioSnapshot(UUID userId, String symbol) {
        return portfolioSnapshotRepository.findLatestByUserIdAndSymbol(userId, symbol, 
            org.springframework.data.domain.PageRequest.of(0, 1));
    }
    
    /**
     * Get position summary for a user
     */
    public List<Object[]> getPositionSummary(UUID userId) {
        return portfolioSnapshotRepository.getPositionSummaryByUser(userId);
    }
    
    /**
     * Position calculation result
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class PositionCalculation {
        private BigDecimal newQuantity;
        private BigDecimal newAveragePrice;
        private BigDecimal newCashBalance;
        private BigDecimal newMarketValue;
        private BigDecimal newUnrealizedPnl;
        private BigDecimal newTotalPortfolioValue;
        private BigDecimal realizedPnl;
    }
}
