package com.quantis.dashboard_gateway.resolver;

import com.quantis.dashboard_gateway.model.*;
import com.quantis.dashboard_gateway.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main GraphQL Resolver for Dashboard Operations
 * 
 * This resolver aggregates data from all microservices and provides
 * a unified GraphQL API for the trading dashboard
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardResolver {

    private final PortfolioService portfolioService;
    private final MarketDataService marketDataService;
    private final TradingService tradingService;
    private final RiskService riskService;
    private final AnalyticsService analyticsService;

    // ==================== DASHBOARD OVERVIEW ====================

    @QueryMapping
    public Mono<DashboardOverview> dashboardOverview(@Argument String userId) {
        log.debug("GraphQL query: dashboardOverview for user: {}", userId);
        
        return Mono.zip(
            portfolioService.getPortfolio(userId),
            marketDataService.getMarketSummary(List.of("AAPL", "GOOGL", "TSLA", "MSFT")),
            tradingService.getRecentTrades(userId, 10),
            tradingService.getActiveOrders(userId),
            analyticsService.getPortfolioPerformance(userId, "1M"),
            riskService.getRiskMetrics(userId)
        ).map(tuple -> DashboardOverview.builder()
            .userId(userId)
            .totalValue(BigDecimal.valueOf(50000)) // TODO: Get from portfolio
            .totalPnL(BigDecimal.valueOf(2500)) // TODO: Get from portfolio
            .dailyPnL(BigDecimal.valueOf(150)) // TODO: Get from portfolio
            .cashBalance(BigDecimal.valueOf(10000)) // TODO: Get from portfolio
            .activePositions(5) // TODO: Get from portfolio
            .pendingOrders(2) // TODO: Get from orders
            .lastUpdate(LocalDateTime.now())
            .build());
    }

    // ==================== PORTFOLIO QUERIES ====================

    @QueryMapping
    public Mono<PortfolioData.Portfolio> portfolio(@Argument String userId) {
        log.debug("GraphQL query: portfolio for user: {}", userId);
        return portfolioService.getPortfolio(userId);
    }

    @QueryMapping
    public Mono<List<PortfolioData.Position>> positions(@Argument String userId) {
        log.debug("GraphQL query: positions for user: {}", userId);
        return portfolioService.getPositions(userId);
    }

    @QueryMapping
    public Mono<PortfolioData.Position> position(@Argument String userId, @Argument String symbol) {
        log.debug("GraphQL query: position for user: {}, symbol: {}", userId, symbol);
        return portfolioService.getPosition(userId, symbol);
    }

    @QueryMapping
    public Mono<PortfolioData.CashBalance> cashBalance(@Argument String userId) {
        log.debug("GraphQL query: cashBalance for user: {}", userId);
        return portfolioService.getCashBalance(userId);
    }

    // ==================== TRADING QUERIES ====================

    @QueryMapping
    public Mono<List<OrderData.Order>> tradingHistory(
            @Argument String userId,
            @Argument Integer limit,
            @Argument Long startTime,
            @Argument Long endTime) {
        log.debug("GraphQL query: tradingHistory for user: {}, limit: {}", userId, limit);
        return tradingService.getTradingHistory(userId, limit, startTime, endTime);
    }

    // Note: Order queries (orderHistory, activeOrders) are handled by OrderResolver to avoid conflicts

    // ==================== MARKET DATA QUERIES ====================
    // Note: Market data queries are handled by MarketDataResolver to avoid conflicts

    // ==================== ANALYTICS QUERIES ====================

    @QueryMapping
    public Mono<Map<String, BigDecimal>> portfolioPerformance(
            @Argument String userId,
            @Argument String period) {
        log.debug("GraphQL query: portfolioPerformance for user: {}, period: {}", userId, period);
        return analyticsService.getPortfolioPerformance(userId, period);
    }

    @QueryMapping
    public Mono<Map<String, BigDecimal>> riskMetrics(@Argument String userId) {
        log.debug("GraphQL query: riskMetrics for user: {}", userId);
        return riskService.getRiskMetrics(userId);
    }

    @QueryMapping
    public Mono<Map<String, Object>> tradeAnalytics(@Argument String userId) {
        log.debug("GraphQL query: tradeAnalytics for user: {}", userId);
        return analyticsService.getTradeAnalytics(userId);
    }

    // ==================== MUTATIONS ====================
    // Note: Order mutations are handled by OrderResolver to avoid conflicts

    @MutationMapping
    public Mono<Boolean> updateWatchlist(
            @Argument String userId,
            @Argument List<String> symbols) {
        log.debug("GraphQL mutation: updateWatchlist for user: {}, symbols: {}", userId, symbols);
        return portfolioService.updateWatchlist(userId, symbols);
    }

    // ==================== SUBSCRIPTIONS ====================

    @SubscriptionMapping
    public Flux<PortfolioData.PortfolioUpdate> portfolioUpdates(@Argument String userId) {
        log.debug("GraphQL subscription: portfolioUpdates for user: {}", userId);
        return portfolioService.getPortfolioUpdates(userId).flatMapMany(Flux::fromIterable);
    }

    @SubscriptionMapping
    public Flux<PortfolioData.PositionUpdate> positionUpdates(@Argument String userId) {
        log.debug("GraphQL subscription: positionUpdates for user: {}", userId);
        return portfolioService.getPositionUpdates(userId).flatMapMany(Flux::fromIterable);
    }

    // Note: Market data subscriptions are handled by MarketDataResolver to avoid conflicts

    // Note: Order subscriptions (orderUpdates) are handled by OrderResolver to avoid conflicts

    @SubscriptionMapping
    public Flux<DashboardUpdate> dashboardUpdates(@Argument String userId) {
        log.debug("GraphQL subscription: dashboardUpdates for user: {}", userId);
        return Flux.interval(java.time.Duration.ofSeconds(5))
            .flatMap(tick -> dashboardOverview(userId))
            .map(overview -> DashboardUpdate.builder()
                .userId(userId)
                .data(overview)
                .timestamp(LocalDateTime.now())
                .updateType("PERIODIC")
                .build());
    }
}
