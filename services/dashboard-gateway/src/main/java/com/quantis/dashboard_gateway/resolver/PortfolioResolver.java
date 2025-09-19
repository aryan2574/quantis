package com.quantis.dashboard_gateway.resolver;

import com.quantis.dashboard_gateway.client.PortfolioClient;
import com.quantis.dashboard_gateway.model.PortfolioData;
import com.quantis.dashboard_gateway.model.TradeRecordData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GraphQL Resolver for Portfolio Operations
 * 
 * Provides portfolio data aggregation from Portfolio Service via gRPC
 * while maintaining high performance for dashboard operations
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class PortfolioResolver {

    private final PortfolioClient portfolioClient;

    /**
     * Get user's portfolio overview
     */
    @QueryMapping
    public Mono<PortfolioData.Portfolio> portfolio(@Argument String userId) {
        log.debug("GraphQL query: portfolio for user: {}", userId);
        
        return portfolioClient.getPortfolio(userId);
    }

    /**
     * Get all positions for a user
     */
    @QueryMapping
    public Mono<List<PortfolioData.Position>> positions(@Argument String userId) {
        log.debug("GraphQL query: positions for user: {}", userId);
        
        return portfolioClient.getPositions(userId)
            .map(positions -> positions.stream()
                .map(pos -> PortfolioData.Position.builder()
                    .userId(pos.getUserId())
                    .symbol(pos.getSymbol())
                    .quantity(pos.getQuantity())
                    .averagePrice(pos.getAveragePrice())
                    .currentPrice(pos.getCurrentPrice())
                    .marketValue(pos.getMarketValue())
                    .unrealizedPnl(pos.getUnrealizedPnl())
                    .realizedPnl(BigDecimal.ZERO) // Will be calculated
                    .lastUpdated(pos.getLastUpdated())
                    .change(BigDecimal.ZERO) // Will be calculated
                    .changePercent(BigDecimal.ZERO) // Will be calculated
                    .build())
                .toList());
    }

    /**
     * Get specific position for user and symbol
     */
    @QueryMapping
    public Mono<PortfolioData.Position> position(@Argument String userId, @Argument String symbol) {
        log.debug("GraphQL query: position for user: {}, symbol: {}", userId, symbol);
        
        return portfolioClient.getPosition(userId, symbol)
            .map(pos -> PortfolioData.Position.builder()
                .userId(pos.getUserId())
                .symbol(pos.getSymbol())
                .quantity(pos.getQuantity())
                .averagePrice(pos.getAveragePrice())
                .currentPrice(pos.getCurrentPrice())
                .marketValue(pos.getMarketValue())
                .unrealizedPnl(pos.getUnrealizedPnl())
                .realizedPnl(BigDecimal.ZERO) // Will be calculated
                .lastUpdated(pos.getLastUpdated())
                .change(BigDecimal.ZERO) // Will be calculated
                .changePercent(BigDecimal.ZERO) // Will be calculated
                .build());
    }

    /**
     * Get cash balance for user
     */
    @QueryMapping
    public Mono<PortfolioData.CashBalance> cashBalance(@Argument String userId) {
        log.debug("GraphQL query: cashBalance for user: {}", userId);
        
        return portfolioClient.getCashBalance(userId)
            .map(cashBalance -> PortfolioData.CashBalance.builder()
                .userId(cashBalance.getUserId())
                .balance(cashBalance.getBalance())
                .currency(cashBalance.getCurrency())
                .availableBalance(cashBalance.getAvailableBalance())
                .pendingBalance(cashBalance.getPendingBalance())
                .lastUpdated(cashBalance.getLastUpdated())
                .build());
    }

    /**
     * Get trading history for user
     */
    @QueryMapping
    public Mono<List<TradeRecordData.TradeRecord>> tradingHistory(
            @Argument String userId,
            @Argument Integer limit,
            @Argument Long startTime,
            @Argument Long endTime) {
        log.debug("GraphQL query: tradingHistory for user: {}, limit: {}", userId, limit);
        
        return portfolioClient.getTradingHistory(userId, limit != null ? limit : 50, startTime, endTime);
    }

    /**
     * Get portfolio performance metrics
     */
    @QueryMapping
    public Mono<PortfolioData.PerformanceMetrics> portfolioPerformance(
            @Argument String userId,
            @Argument String period) {
        log.debug("GraphQL query: portfolioPerformance for user: {}, period: {}", userId, period);
        
        // This would typically call an analytics service
        // For now, return default metrics
        return Mono.just(PortfolioData.PerformanceMetrics.builder()
            .userId(userId)
            .period(period)
            .totalReturn(BigDecimal.ZERO)
            .totalReturnPercent(BigDecimal.ZERO)
            .sharpeRatio(BigDecimal.ZERO)
            .maxDrawdown(BigDecimal.ZERO)
            .volatility(BigDecimal.ZERO)
            .winRate(BigDecimal.ZERO)
            .profitFactor(BigDecimal.ZERO)
            .totalTrades(0)
            .winningTrades(0)
            .losingTrades(0)
            .averageWin(BigDecimal.ZERO)
            .averageLoss(BigDecimal.ZERO)
            .largestWin(BigDecimal.ZERO)
            .largestLoss(BigDecimal.ZERO)
            .startValue(BigDecimal.ZERO)
            .endValue(BigDecimal.ZERO)
            .lastUpdated(java.time.Instant.now())
            .build());
    }

    /**
     * Subscribe to portfolio updates
     */
    @SubscriptionMapping
    public Flux<PortfolioData.Portfolio> portfolioUpdates(@Argument String userId) {
        log.info("GraphQL subscription: portfolioUpdates for user: {}", userId);
        
        return Flux.interval(Duration.ofSeconds(5)) // Update every 5 seconds
            .flatMap(tick -> portfolioClient.getPortfolio(userId));
    }

    /**
     * Subscribe to position updates
     */
    @SubscriptionMapping
    public Flux<PortfolioData.Position> positionUpdates(@Argument String userId) {
        log.info("GraphQL subscription: positionUpdates for user: {}", userId);
        
        return Flux.interval(Duration.ofSeconds(3)) // Update every 3 seconds
            .flatMap(tick -> portfolioClient.getPositions(userId))
            .flatMap(Flux::fromIterable);
    }
}
