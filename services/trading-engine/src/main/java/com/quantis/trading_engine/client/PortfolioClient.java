package com.quantis.trading_engine.client;

import com.quantis.portfolio_service.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

/**
 * gRPC client for communicating with Portfolio Service.
 * Used by Trading Engine to update positions after trade execution.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioClient {
    
    @GrpcClient("portfolio-service")
    private PortfolioServiceGrpc.PortfolioServiceBlockingStub portfolioStub;
    
    /**
     * Update position after trade execution
     */
    public void updatePosition(String userId, String symbol, long quantityChange, 
                              double price, String side, String orderId) {
        try {
            UpdatePositionRequest request = UpdatePositionRequest.newBuilder()
                .setUserId(userId)
                .setSymbol(symbol)
                .setQuantityChange(quantityChange)
                .setPrice(price)
                .setSide(side)
                .setOrderId(orderId)
                .build();
            
            UpdatePositionResponse response = portfolioStub.updatePosition(request);
            
            if (response.getSuccess()) {
                log.info("Position updated successfully for user: {} symbol: {}", userId, symbol);
            } else {
                log.warn("Position update failed for user: {} symbol: {}. Reason: {}", 
                    userId, symbol, response.getMessage());
            }
            
        } catch (StatusRuntimeException e) {
            log.error("gRPC error updating position for user: {} symbol: {}", userId, symbol, e);
            throw new RuntimeException("Failed to update position", e);
        } catch (Exception e) {
            log.error("Error updating position for user: {} symbol: {}", userId, symbol, e);
            throw new RuntimeException("Failed to update position", e);
        }
    }
    
    /**
     * Get current position for validation
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
     * Data class for position information
     */
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
