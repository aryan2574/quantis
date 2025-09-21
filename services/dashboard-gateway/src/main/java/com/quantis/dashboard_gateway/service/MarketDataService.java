package com.quantis.dashboard_gateway.service;

import com.quantis.dashboard_gateway.client.MarketDataClient;
import com.quantis.dashboard_gateway.model.MarketDataModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Market Data Service
 * 
 * Service layer that wraps the MarketDataClient for business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final MarketDataClient marketDataClient;

    public Mono<MarketDataModel.MarketData> getMarketData(String symbol) {
        return marketDataClient.getMarketData(symbol);
    }

    public Mono<List<MarketDataModel.MarketData>> getMarketDataList(List<String> symbols) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<List<MarketDataModel.MarketData>> getHistoricalData(String symbol, String interval, Long startTime, Long endTime, Integer limit) {
        // TODO: Implement when client method is available - return MarketData instead of HistoricalData
        return Mono.just(List.of());
    }

    public Mono<List<MarketDataModel.MarketSummary>> getMarketSummary(List<String> symbols) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<List<MarketDataModel.OrderBook>> getOrderBook(String symbol, Integer depth) {
        // TODO: Implement when client method is available - return List<OrderBook> instead of single OrderBook
        return Mono.just(List.of());
    }

    public Mono<List<MarketDataModel.Trade>> getRecentTrades(String symbol, Integer limit) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<List<MarketDataModel.MarketDataUpdate>> getMarketDataUpdates(List<String> symbols) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<List<MarketDataModel.TradeUpdate>> getTradeUpdates(List<String> symbols) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }
}
