package com.quantis.trading_engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.trading_engine.client.PortfolioClient;
import com.quantis.trading_engine.model.Order;
import com.quantis.trading_engine.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Core trading engine that executes orders against simulated market.
 * Handles order matching, execution, and portfolio updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradingEngine {
    
    private final PortfolioClient portfolioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Execute a valid order
     */
    public void executeOrder(Order order) {
        try {
            log.info("Executing order: {} for user: {} symbol: {} side: {} quantity: {} price: {}", 
                order.getOrderId(), order.getUserId(), order.getSymbol(), 
                order.getSide(), order.getQuantity(), order.getPrice());
            
            // Simulate market execution
            Trade trade = simulateMarketExecution(order);

            if (trade != null) {
                // Update portfolio via gRPC
                updatePortfolio(trade);
                
                // Publish trade execution result
                publishTradeExecution(trade);
                
                log.info("Order {} executed successfully. Trade ID: {}", 
                    order.getOrderId(), trade.getTradeId());
            } else {
                log.warn("Order {} could not be executed", order.getOrderId());
            }
        } catch (Exception e) {
            log.error("Error executing order: {}", order.getOrderId(), e);
        }
    }
    
    /**
     * Simulate market execution with realistic market conditions
     */
    private Trade simulateMarketExecution(Order order) {
        try {
            // Simulate market conditions
            double executionPrice = calculateExecutionPrice(order);
            long executionQuantity = calculateExecutionQuantity(order);
            
            if (executionQuantity <= 0) {
                return null; // No execution possible
            }
            
            // Create trade
            Trade trade = Trade.builder()
                .tradeId(UUID.randomUUID().toString())
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .quantity(executionQuantity)
                .price(executionPrice)
                .totalValue(executionQuantity * executionPrice)
                .executedAt(Instant.now())
                .status(Trade.TradeStatus.EXECUTED)
                .build();
            
            return trade;
            
        } catch (Exception e) {
            log.error("Error simulating market execution for order: {}", order.getOrderId(), e);
            return null;
        }
    }
    
    /**
     * Calculate execution price based on market simulation
     */
    private double calculateExecutionPrice(Order order) {
        // Simple market simulation - in reality this would be much more complex
        double basePrice = order.getPrice();
        
        // Add some market volatility (±0.5%)
        double volatility = (Math.random() - 0.5) * 0.01;
        double executionPrice = basePrice * (1 + volatility);
        
        // Ensure price is reasonable
        executionPrice = Math.max(executionPrice, basePrice * 0.95);
        executionPrice = Math.min(executionPrice, basePrice * 1.05);
        
        return Math.round(executionPrice * 100.0) / 100.0; // Round to 2 decimal places
    }
    
    /**
     * Calculate execution quantity based on market liquidity
     */
    private long calculateExecutionQuantity(Order order) {
        // Simulate market liquidity - in reality this would check order book
        double liquidityFactor = Math.random() * 0.2 + 0.8; // 80-100% liquidity
        long executionQuantity = Math.round(order.getQuantity() * liquidityFactor);
        
        // Ensure we don't exceed the order quantity
        return Math.min(executionQuantity, order.getQuantity());
    }
    
    /**
     * Update portfolio via gRPC call to Portfolio Service
     */
    private void updatePortfolio(Trade trade) {
        try {
            log.info("Updating portfolio for trade: {}", trade.getTradeId());
            
            // Call Portfolio Service via gRPC
            portfolioClient.updatePosition(
                trade.getUserId(),
                trade.getSymbol(),
                trade.getSide().equals("BUY") ? trade.getQuantity() : -trade.getQuantity(),
                trade.getPrice(),
                trade.getSide(),
                trade.getOrderId()
            );
            
            log.info("Portfolio updated successfully for trade: {}", trade.getTradeId());
            
        } catch (Exception e) {
            log.error("Error updating portfolio for trade: {}", trade.getTradeId(), e);
            // In production, you might want to retry or send to dead letter queue
        }
    }
    
    /**
     * Publish trade execution result to Kafka
     */
    private void publishTradeExecution(Trade trade) {
        try {
            String tradeJson = objectMapper.writeValueAsString(trade);
            
            // Publish to trades.executed topic
            kafkaTemplate.send("trades.executed", trade.getTradeId(), tradeJson);
            
            log.info("Trade execution published: {}", trade.getTradeId());
            
        } catch (Exception e) {
            log.error("Error publishing trade execution: {}", trade.getTradeId(), e);
        }
    }
}
