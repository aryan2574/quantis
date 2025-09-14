package com.quantis.dashboard_gateway.resolver;

import com.quantis.dashboard_gateway.client.MarketDataClient;
import com.quantis.dashboard_gateway.model.MarketDataModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * GraphQL Resolver for Market Data Operations
 * 
 * Provides market data aggregation from Market Data Service
 * with real-time updates via Redis and GraphQL subscriptions
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class MarketDataResolver {

    private final MarketDataClient marketDataClient;

    /**
     * Get current market data for a symbol
     */
    @QueryMapping
    public Mono<MarketDataModel.MarketData> marketData(@Argument String symbol) {
        log.debug("GraphQL query: marketData for symbol: {}", symbol);
        
        return marketDataClient.getMarketData(symbol)
            .map(data -> MarketDataModel.MarketData.builder()
                .symbol(data.getSymbol())
                .bestBid(data.getBestBid())
                .bestAsk(data.getBestAsk())
                .lastPrice(data.getLastPrice())
                .spread(data.getSpread())
                .volume(data.getVolume())
                .change(data.getChange())
                .changePercent(data.getChangePercent())
                .timestamp(data.getTimestamp())
                .status(data.getStatus())
                .high24h(data.getHigh24h())
                .low24h(data.getLow24h())
                .open24h(data.getOpen24h())
                .build());
    }

    /**
     * Get historical data for a symbol
     */
    @QueryMapping
    public Mono<List<MarketDataModel.HistoricalData>> historicalData(
            @Argument String symbol,
            @Argument String interval,
            @Argument Long startTime,
            @Argument Long endTime,
            @Argument Integer limit) {
        log.debug("GraphQL query: historicalData for symbol: {}, interval: {}", symbol, interval);
        
        return marketDataClient.getHistoricalData(symbol, interval, startTime, endTime, limit)
            .map(historicalData -> historicalData.stream()
                .map(data -> MarketDataModel.HistoricalData.builder()
                    .timestamp(data.getTimestamp())
                    .open(data.getOpen())
                    .high(data.getHigh())
                    .low(data.getLow())
                    .close(data.getClose())
                    .volume(data.getVolume())
                    .vwap(data.getVwap())
                    .build())
                .toList());
    }

    /**
     * Get order book for a symbol
     */
    @QueryMapping
    public Mono<MarketDataModel.OrderBook> orderBook(@Argument String symbol, @Argument Integer depth) {
        log.debug("GraphQL query: orderBook for symbol: {}, depth: {}", symbol, depth);
        
        return marketDataClient.getOrderBook(symbol, depth)
            .map(orderBook -> MarketDataModel.OrderBook.builder()
                .symbol(orderBook.getSymbol())
                .bids(orderBook.getBids())
                .asks(orderBook.getAsks())
                .timestamp(orderBook.getTimestamp())
                .status(orderBook.getStatus())
                .spread(orderBook.getSpread())
                .midPrice(orderBook.getMidPrice())
                .build());
    }

    /**
     * Get recent trades for a symbol
     */
    @QueryMapping
    public Mono<List<MarketDataModel.Trade>> recentTrades(@Argument String symbol, @Argument Integer limit) {
        log.debug("GraphQL query: recentTrades for symbol: {}, limit: {}", symbol, limit);
        
        return marketDataClient.getRecentTrades(symbol, limit)
            .map(trades -> trades.stream()
                .map(trade -> MarketDataModel.Trade.builder()
                    .tradeId(trade.getTradeId())
                    .symbol(trade.getSymbol())
                    .side(trade.getSide())
                    .quantity(trade.getQuantity())
                    .price(trade.getPrice())
                    .timestamp(trade.getTimestamp())
                    .totalValue(trade.getTotalValue())
                    .build())
                .toList());
    }

    /**
     * Get market summary for multiple symbols
     */
    @QueryMapping
    public Mono<List<MarketDataModel.MarketSummary>> marketSummary(@Argument List<String> symbols) {
        log.debug("GraphQL query: marketSummary for symbols: {}", symbols);
        
        return marketDataClient.getMarketSummary(symbols)
            .map(summaries -> summaries.stream()
                .map(summary -> MarketDataModel.MarketSummary.builder()
                    .symbol(summary.getSymbol())
                    .lastPrice(summary.getLastPrice())
                    .change(summary.getChange())
                    .changePercent(summary.getChangePercent())
                    .volume(summary.getVolume())
                    .high24h(summary.getHigh24h())
                    .low24h(summary.getLow24h())
                    .marketCap(summary.getMarketCap())
                    .build())
                .toList());
    }

    /**
     * Subscribe to real-time market data updates
     */
    @SubscriptionMapping
    public Flux<MarketDataModel.MarketData> marketDataUpdates(@Argument List<String> symbols) {
        log.info("GraphQL subscription: marketDataUpdates for symbols: {}", symbols);
        
        return Flux.interval(Duration.ofMillis(500)) // 2 updates per second
            .flatMap(tick -> Flux.fromIterable(symbols)
                .flatMap(symbol -> marketDataClient.getMarketData(symbol))
                .collectList())
            .flatMap(Flux::fromIterable)
            .map(data -> MarketDataModel.MarketData.builder()
                .symbol(data.getSymbol())
                .bestBid(data.getBestBid())
                .bestAsk(data.getBestAsk())
                .lastPrice(data.getLastPrice())
                .spread(data.getSpread())
                .volume(data.getVolume())
                .change(data.getChange())
                .changePercent(data.getChangePercent())
                .timestamp(data.getTimestamp())
                .status(data.getStatus())
                .high24h(data.getHigh24h())
                .low24h(data.getLow24h())
                .open24h(data.getOpen24h())
                .build());
    }

    /**
     * Subscribe to real-time trade updates
     */
    @SubscriptionMapping
    public Flux<MarketDataModel.Trade> tradeUpdates(@Argument List<String> symbols) {
        log.info("GraphQL subscription: tradeUpdates for symbols: {}", symbols);
        
        return Flux.interval(Duration.ofMillis(1000)) // 1 update per second
            .flatMap(tick -> Flux.fromIterable(symbols)
                .flatMap(symbol -> marketDataClient.getRecentTrades(symbol, 5))
                .flatMap(Flux::fromIterable)
                .collectList())
            .flatMap(Flux::fromIterable)
            .map(trade -> MarketDataModel.Trade.builder()
                .tradeId(trade.getTradeId())
                .symbol(trade.getSymbol())
                .side(trade.getSide())
                .quantity(trade.getQuantity())
                .price(trade.getPrice())
                .timestamp(trade.getTimestamp())
                .totalValue(trade.getTotalValue())
                .build());
    }
}
