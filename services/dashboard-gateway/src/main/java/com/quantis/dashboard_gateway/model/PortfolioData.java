package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Portfolio data models for GraphQL Gateway
 */
public class PortfolioData {
    
    // Static builder method for convenience
    public static PortfolioBuilder builder() {
        return new PortfolioBuilder();
    }
    
    public static class PortfolioBuilder {
        private String userId;
        private BigDecimal totalValue;
        private BigDecimal cashBalance;
        private BigDecimal positionsValue;
        private BigDecimal unrealizedPnl;
        private BigDecimal realizedPnl;
        private String currency;
        private Instant lastUpdated;
        private List<Position> positions;
        private PerformanceMetrics performance;
        
        public PortfolioBuilder userId(String userId) { this.userId = userId; return this; }
        public PortfolioBuilder totalValue(BigDecimal totalValue) { this.totalValue = totalValue; return this; }
        public PortfolioBuilder cashBalance(BigDecimal cashBalance) { this.cashBalance = cashBalance; return this; }
        public PortfolioBuilder positionsValue(BigDecimal positionsValue) { this.positionsValue = positionsValue; return this; }
        public PortfolioBuilder unrealizedPnl(BigDecimal unrealizedPnl) { this.unrealizedPnl = unrealizedPnl; return this; }
        public PortfolioBuilder realizedPnl(BigDecimal realizedPnl) { this.realizedPnl = realizedPnl; return this; }
        public PortfolioBuilder currency(String currency) { this.currency = currency; return this; }
        public PortfolioBuilder lastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; return this; }
        public PortfolioBuilder positions(List<Position> positions) { this.positions = positions; return this; }
        public PortfolioBuilder performance(PerformanceMetrics performance) { this.performance = performance; return this; }
        
        public Portfolio build() {
            return Portfolio.builder()
                .userId(userId)
                .totalValue(totalValue)
                .cashBalance(cashBalance)
                .positionsValue(positionsValue)
                .unrealizedPnl(unrealizedPnl)
                .realizedPnl(realizedPnl)
                .currency(currency)
                .lastUpdated(lastUpdated)
                .positions(positions)
                .performance(performance)
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Portfolio {
        private String userId;
        private BigDecimal totalValue;
        private BigDecimal cashBalance;
        private BigDecimal positionsValue;
        private BigDecimal unrealizedPnl;
        private BigDecimal realizedPnl;
        private String currency;
        private Instant lastUpdated;
        private List<Position> positions;
        private PerformanceMetrics performance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Position {
        private String userId;
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal averagePrice;
        private BigDecimal currentPrice;
        private BigDecimal marketValue;
        private BigDecimal unrealizedPnl;
        private BigDecimal realizedPnl;
        private Instant lastUpdated;
        private BigDecimal change;
        private BigDecimal changePercent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashBalance {
        private String userId;
        private BigDecimal balance;
        private String currency;
        private BigDecimal availableBalance;
        private BigDecimal pendingBalance;
        private Instant lastUpdated;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private String userId;
        private String period;
        private BigDecimal totalReturn;
        private BigDecimal totalReturnPercent;
        private BigDecimal sharpeRatio;
        private BigDecimal maxDrawdown;
        private BigDecimal volatility;
        private BigDecimal winRate;
        private BigDecimal profitFactor;
        private Integer totalTrades;
        private Integer winningTrades;
        private Integer losingTrades;
        private BigDecimal averageWin;
        private BigDecimal averageLoss;
        private BigDecimal largestWin;
        private BigDecimal largestLoss;
        private BigDecimal startValue;
        private BigDecimal endValue;
        private Instant lastUpdated;
    }
}
