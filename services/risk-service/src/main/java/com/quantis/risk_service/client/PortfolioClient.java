package com.quantis.risk_service.client;

import com.quantis.portfolio_service.grpc.*;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * gRPC client for communicating with Portfolio Service.
 * Provides access to portfolio data for risk calculations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioClient {
    
    @GrpcClient("portfolio-service")
    private PortfolioServiceGrpc.PortfolioServiceBlockingStub portfolioStub;
    
    /**
     * Get user's cash balance
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
     * Get user's position value for a specific symbol
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
            return 50_000.0; // Default fallback
        } catch (Exception e) {
            log.error("Error getting position value for user: {} symbol: {}", userId, symbol, e);
            return 50_000.0; // Default fallback
        }
    }
    
    /**
     * Get user's total portfolio value
     */
    public double getTotalPortfolioValue(String userId) {
        try {
            GetPortfolioValueRequest request = GetPortfolioValueRequest.newBuilder()
                .setUserId(userId)
                .build();
            
            GetPortfolioValueResponse response = portfolioStub.getPortfolioValue(request);
            return response.getTotalValue();
            
        } catch (StatusRuntimeException e) {
            log.error("gRPC error getting total portfolio value for user: {}", userId, e);
            return 150_000.0; // Default fallback
        } catch (Exception e) {
            log.error("Error getting total portfolio value for user: {}", userId, e);
            return 150_000.0; // Default fallback
        }
    }
    
    /**
     * Get user's positions
     * TODO: Implement actual portfolio service communication
     */
    public List<Map<String, Object>> getPositions(String userId) {
        log.debug("Getting positions for user: {}", userId);
        // TODO: Implement actual portfolio service communication
        return List.of(
            Map.of("symbol", "AAPL", "quantity", 100, "value", 15000.0),
            Map.of("symbol", "GOOGL", "quantity", 50, "value", 7500.0)
        );
    }
    
    /**
     * Get user's leverage ratio
     * TODO: Implement actual portfolio service communication
     */
    public double getLeverageRatio(String userId) {
        log.debug("Getting leverage ratio for user: {}", userId);
        // TODO: Implement actual portfolio service communication
        return 1.5; // Default fallback
    }
    
    /**
     * Get user's margin requirements
     * TODO: Implement actual portfolio service communication
     */
    public double getMarginRequirement(String userId) {
        log.debug("Getting margin requirement for user: {}", userId);
        // TODO: Implement actual portfolio service communication
        return 25_000.0; // Default fallback
    }
    
    /**
     * Check if user has sufficient margin for a trade
     * TODO: Implement actual portfolio service communication
     */
    public boolean hasSufficientMargin(String userId, double tradeValue) {
        log.debug("Checking margin sufficiency for user: {}, trade value: {}", userId, tradeValue);
        // TODO: Implement actual portfolio service communication
        return tradeValue <= 50_000.0; // Default fallback
    }
    
    /**
     * Get user's portfolio allocation by asset type
     * TODO: Implement actual portfolio service communication
     */
    public Map<String, Double> getPortfolioAllocation(String userId) {
        log.debug("Getting portfolio allocation for user: {}", userId);
        // TODO: Implement actual portfolio service communication
        return Map.of(
            "STOCKS", 0.6,
            "CRYPTO", 0.3,
            "CASH", 0.1
        );
    }
    
    /**
     * Get user's concentration risk by symbol
     * TODO: Implement actual portfolio service communication
     */
    public Map<String, Double> getConcentrationRisk(String userId) {
        log.debug("Getting concentration risk for user: {}", userId);
        // TODO: Implement actual portfolio service communication
        return Map.of(
            "AAPL", 0.4,
            "GOOGL", 0.3,
            "TSLA", 0.2,
            "OTHER", 0.1
        );
    }
    
    /**
     * Get user's portfolio performance metrics
     * TODO: Implement actual portfolio service communication
     */
    public Map<String, Double> getPerformanceMetrics(String userId) {
        log.debug("Getting performance metrics for user: {}", userId);
        // TODO: Implement actual portfolio service communication
        return Map.of(
            "totalReturn", 0.15,
            "sharpeRatio", 1.2,
            "volatility", 0.18,
            "maxDrawdown", 0.12
        );
    }
    
    /**
     * Get portfolio value information
     * TODO: Implement actual portfolio service communication
     */
    public PortfolioValue getPortfolioValue(String userId) {
        log.debug("Getting portfolio value for user: {}", userId);
        double cashBalance = getCashBalance(userId);
        double totalValue = getTotalPortfolioValue(userId);
        return new PortfolioValue(cashBalance, totalValue);
    }
    
    /**
     * Get position information for a specific symbol
     * TODO: Implement actual portfolio service communication
     */
    public PositionInfo getPosition(String userId, String symbol) {
        log.debug("Getting position for user: {}, symbol: {}", userId, symbol);
        // TODO: Implement actual portfolio service communication
        return new PositionInfo(symbol, 100, 15000.0, 150.0);
    }
}