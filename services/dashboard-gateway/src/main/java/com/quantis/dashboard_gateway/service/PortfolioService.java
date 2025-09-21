package com.quantis.dashboard_gateway.service;

import com.quantis.dashboard_gateway.client.PortfolioClient;
import com.quantis.dashboard_gateway.model.PortfolioData;
import com.quantis.dashboard_gateway.model.TradeRecordData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Portfolio Service
 * 
 * Service layer that wraps the PortfolioClient for business logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final PortfolioClient portfolioClient;

    public Mono<PortfolioData.Portfolio> getPortfolio(String userId) {
        return portfolioClient.getPortfolio(userId);
    }

    public Mono<List<PortfolioData.Position>> getPositions(String userId) {
        return portfolioClient.getPositions(userId);
    }

    public Mono<PortfolioData.Position> getPosition(String userId, String symbol) {
        return portfolioClient.getPosition(userId, symbol);
    }

    public Mono<PortfolioData.CashBalance> getCashBalance(String userId) {
        return portfolioClient.getCashBalance(userId);
    }

    public Mono<List<TradeRecordData.TradeRecord>> getTradeHistory(String userId, int limit) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<Boolean> updateWatchlist(String userId, List<String> symbols) {
        // TODO: Implement when client method is available
        return Mono.just(true);
    }

    public Mono<List<PortfolioData.PortfolioUpdate>> getPortfolioUpdates(String userId) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<List<PortfolioData.PositionUpdate>> getPositionUpdates(String userId) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }
}
