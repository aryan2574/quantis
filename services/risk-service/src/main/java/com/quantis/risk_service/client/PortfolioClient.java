package com.quantis.risk_service.client;

import com.quantis.portfolio_service.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * gRPC client for communicating with Portfolio Service.
 * Provides fast, efficient access to portfolio data for risk calculations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioClient {
    
    @GrpcClient("portfolio-service")
    private PortfolioServiceGrpc.PortfolioServiceBlockingStub portfolioStub;
    
    /**
     * Get user's cash balance via gRPC
     */
    public double getCashBalance(String userId) {
        try {
            GetCashBalanceRequest request = GetCashBalanceRequest.newBuilder()
                .setUserId(userId)
                .build();
            
            GetCashBalanceResponse response = portfolioStub.getCashBalance(request);
            return response.getCashBalance();
        } catch (StatusRuntimeException e) {
            log.error("gRPC error getting cash balance for user: {}", userId, e);
            return 100_000.0; // Default fallback
        } catch (Exception e) {
            log.error("Error getting cash balance for user: {}", userId, e);
            return 100_000.0; // Default fallback
        }
    }
    
    /**
     * Get user's position value for a specific symbol via gRPC
     */
    public double getPositionValue(String userId, String symbol) {
        try {
            GetPositionValueRequest request = GetPositionValueRequest.newBuilder()
                .setUserId(userId)
                .setSymbol(symbol)
                .build();
            
            GetPositionValueResponse response = portfolioStub.getPositionValue(request);
            return response.getPositionValue();
        } catch (StatusRuntimeException e) {
            log.error("gRPC error getting position value for user: {} symbol: {}", userId, symbol, e);
            return 0.0; // Default fallback
        } catch (Exception e) {
            log.error("Error getting position value for user: {} symbol: {}", userId, symbol, e);
            return 0.0; // Default fallback
        }
    }
    
    /**
     * Get user's total portfolio value via gRPC
     */
    public PortfolioValue getPortfolioValue(String userId) {
        try {
            GetPortfolioValueRequest request = GetPortfolioValueRequest.newBuilder()
                .setUserId(userId)
                .build();
            
            GetPortfolioValueResponse response = portfolioStub.getPortfolioValue(request);
            
            return PortfolioValue.builder()
                .totalValue(response.getTotalValue())
                .cashBalance(response.getCashBalance())
                .positionsValue(response.getPositionsValue())
                .unrealizedPnl(response.getUnrealizedPnl())
                .currency(response.getCurrency())
                .timestamp(response.getTimestamp())
                .build();
        } catch (StatusRuntimeException e) {
            log.error("gRPC error getting portfolio value for user: {}", userId, e);
            return getDefaultPortfolioValue();
        } catch (Exception e) {
            log.error("Error getting portfolio value for user: {}", userId, e);
            return getDefaultPortfolioValue();
        }
    }
    
    /**
     * Get detailed position information via gRPC
     */
    public PositionInfo getPosition(String userId, String symbol) {
        try {
            GetPositionRequest request = GetPositionRequest.newBuilder()
                .setUserId(userId)
                .setSymbol(symbol)
                .build();
            
            GetPositionResponse response = portfolioStub.getPosition(request);
            
            return PositionInfo.builder()
                .userId(response.getUserId())
                .symbol(response.getSymbol())
                .quantity(response.getQuantity())
                .averagePrice(response.getAveragePrice())
                .currentPrice(response.getCurrentPrice())
                .marketValue(response.getMarketValue())
                .unrealizedPnl(response.getUnrealizedPnl())
                .lastUpdated(response.getLastUpdated())
                .build();
        } catch (StatusRuntimeException e) {
            log.error("gRPC error getting position for user: {} symbol: {}", userId, symbol, e);
            return getDefaultPosition(userId, symbol);
        } catch (Exception e) {
            log.error("Error getting position for user: {} symbol: {}", userId, symbol, e);
            return getDefaultPosition(userId, symbol);
        }
    }
    
    /**
     * Default portfolio value when gRPC fails
     */
    private PortfolioValue getDefaultPortfolioValue() {
        return PortfolioValue.builder()
            .totalValue(100_000.0)
            .cashBalance(100_000.0)
            .positionsValue(0.0)
            .unrealizedPnl(0.0)
            .currency("USD")
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    /**
     * Default position when gRPC fails
     */
    private PositionInfo getDefaultPosition(String userId, String symbol) {
        return PositionInfo.builder()
            .userId(userId)
            .symbol(symbol)
            .quantity(0.0)
            .averagePrice(0.0)
            .currentPrice(0.0)
            .marketValue(0.0)
            .unrealizedPnl(0.0)
            .lastUpdated(System.currentTimeMillis())
            .build();
    }
    
    /**
     * Data classes for portfolio information
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PortfolioValue {
        private double totalValue;
        private double cashBalance;
        private double positionsValue;
        private double unrealizedPnl;
        private String currency;
        private long timestamp;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PositionInfo {
        private String userId;
        private String symbol;
        private double quantity;
        private double averagePrice;
        private double currentPrice;
        private double marketValue;
        private double unrealizedPnl;
        private long lastUpdated;
    }
}
