package com.quantis.trading_engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.trading_engine.client.PortfolioClient;
import com.quantis.trading_engine.jni.TradingEngineJNI;
import com.quantis.trading_engine.model.Order;
import com.quantis.trading_engine.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Core trading engine that executes orders using high-performance C++ order book.
 * Handles order matching, execution, and portfolio updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "cpp.engine.enabled", havingValue = "true", matchIfMissing = false)
public class TradingEngine {
    
    private final PortfolioClient portfolioClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final TradingEngineJNI cppEngine;
    private final LockFreeMarketDataService marketDataService;
    
    /**
     * Execute a valid order using C++ engine
     */
    public void executeOrder(Order order) {
        try {
            log.info("Executing order via C++ engine: {} for user: {} symbol: {} side: {} quantity: {} price: {}", 
                order.getOrderId(), order.getUserId(), order.getSymbol(), 
                order.getSide(), order.getQuantity(), order.getPrice());
            
            // Validate order before processing
            if (!isValidOrder(order)) {
                log.warn("Invalid order rejected: {}", order.getOrderId());
                return;
            }
            
            // Add order to C++ engine - this will perform actual matching
            boolean success = cppEngine.addOrder(
                order.getOrderId(),
                order.getUserId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getPrice()
            );
            
            if (success) {
                // Try to get market data from lock-free C++ store (ultra-fast)
                LockFreeMarketDataService.MarketDataSnapshot marketData = 
                    marketDataService.getMarketData(order.getSymbol());
                
                // If no real market data available, use simulated data for trade execution
                double bestBid, bestAsk;
                if (marketData != null && marketData.getBestBid() > 0 && marketData.getBestAsk() > 0) {
                    bestBid = marketData.getBestBid();
                    bestAsk = marketData.getBestAsk();
                    log.debug("Using real market data for {}: bid={}, ask={}", order.getSymbol(), bestBid, bestAsk);
                } else {
                    // Simulate market data based on order price for immediate execution
                    bestBid = order.getPrice() * 0.999; // Slightly below order price
                    bestAsk = order.getPrice() * 1.001; // Slightly above order price
                    log.info("Using simulated market data for {}: bid={}, ask={}", order.getSymbol(), bestBid, bestAsk);
                }
                
                // For demonstration, execute all orders immediately
                // In production, this would depend on order matching logic
                boolean wasExecuted = true; // Always execute for now
                
                if (wasExecuted) {
                    // Create trade from the executed order
                    Trade trade = createTradeFromOrder(order, bestBid, bestAsk);
                    
                    if (trade != null) {
                        // Update portfolio via gRPC
                        updatePortfolio(trade);
                        
                        // Publish trade execution result to both internal and external topics
                        publishTradeExecution(trade);
                        publishTradeToExternalTopic(trade);
                        
                        log.info("✅ Order {} executed successfully via C++ engine. Trade ID: {}", 
                            order.getOrderId(), trade.getTradeId());
                    }
                } else {
                    // Order was added to order book but not immediately executed
                    log.info("Order {} added to C++ order book for future matching", order.getOrderId());
                    
                    // Publish order book update
                    publishOrderBookUpdate(order.getSymbol());
                }
            } else {
                log.warn("Failed to add order {} to C++ engine", order.getOrderId());
            }
            
        } catch (Exception e) {
            log.error("Error executing order via C++ engine: {}", order.getOrderId(), e);
        }
    }
    
    /**
     * Validate order before processing
     */
    private boolean isValidOrder(Order order) {
        if (order == null) {
            log.warn("Order is null");
            return false;
        }
        
        if (order.getOrderId() == null || order.getOrderId().trim().isEmpty()) {
            log.warn("Order ID is null or empty");
            return false;
        }
        
        if (order.getUserId() == null || order.getUserId().trim().isEmpty()) {
            log.warn("User ID is null or empty");
            return false;
        }
        
        if (order.getSymbol() == null || order.getSymbol().trim().isEmpty()) {
            log.warn("Symbol is null or empty");
            return false;
        }
        
        if (order.getSide() == null || (!order.getSide().equals("BUY") && !order.getSide().equals("SELL"))) {
            log.warn("Invalid order side: {}", order.getSide());
            return false;
        }
        
        if (order.getQuantity() <= 0) {
            log.warn("Invalid quantity: {}", order.getQuantity());
            return false;
        }
        
        if (order.getPrice() <= 0) {
            log.warn("Invalid price: {}", order.getPrice());
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if order was immediately executable based on market data
     */
    private boolean checkImmediateExecution(Order order, double bestBid, double bestAsk) {
        if (order.getSide().equals("BUY")) {
            // BUY order is executable if there's a sell order at or below our price
            return bestAsk > 0 && order.getPrice() >= bestAsk;
        } else if (order.getSide().equals("SELL")) {
            // SELL order is executable if there's a buy order at or above our price
            return bestBid > 0 && order.getPrice() <= bestBid;
        }
        return false;
    }
    
    /**
     * Create trade from executed order
     */
    private Trade createTradeFromOrder(Order order, double bestBid, double bestAsk) {
        try {
            // Determine execution price based on order side and market conditions
            double executionPrice;
            if (order.getSide().equals("BUY")) {
                executionPrice = bestAsk > 0 ? bestAsk : order.getPrice();
            } else {
                executionPrice = bestBid > 0 ? bestBid : order.getPrice();
            }
            
            // For now, assume full execution (in reality, C++ would return partial fills)
            long executionQuantity = order.getQuantity();
            
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
            log.error("Error creating trade from order: {}", order.getOrderId(), e);
            return null;
        }
    }
    
    /**
     * Publish order book update to Kafka
     */
    private void publishOrderBookUpdate(String symbol) {
        try {
            // Get market data from lock-free C++ store
            LockFreeMarketDataService.MarketDataSnapshot marketData = 
                marketDataService.getMarketData(symbol);
            
            if (marketData == null) {
                return;
            }
            
            long orderCount = cppEngine.getOrderCount(symbol);
            double spread = marketData.getSpread();
            
            // Create market data update message
            String marketUpdate = String.format(
                "{\"symbol\":\"%s\",\"bestBid\":%.2f,\"bestAsk\":%.2f,\"lastPrice\":%.2f,\"spread\":%.2f,\"orderCount\":%d,\"timestamp\":%d}",
                symbol, marketData.getBestBid(), marketData.getBestAsk(), marketData.getLastPrice(), 
                spread, orderCount, System.currentTimeMillis()
            );
            
            kafkaTemplate.send("market.data", symbol, marketUpdate);
            log.debug("Published market data update for symbol: {}", symbol);
            
        } catch (Exception e) {
            log.error("Error publishing order book update for symbol: {}", symbol, e);
        }
    }
    
    /**
     * Get market data from lock-free C++ store (ultra-fast)
     */
    public LockFreeMarketDataService.MarketDataSnapshot getMarketData(String symbol) {
        try {
            return marketDataService.getMarketData(symbol);
        } catch (Exception e) {
            log.error("Error getting market data for symbol: {}", symbol, e);
            return null;
        }
    }
    
    /**
     * Get order count from C++ engine
     */
    public long getOrderCount(String symbol) {
        try {
            return cppEngine.getOrderCount(symbol);
        } catch (Exception e) {
            log.error("Error getting order count for symbol: {}", symbol, e);
            return 0;
        }
    }
    
    /**
     * Get spread from C++ engine
     */
    public double getSpread(String symbol) {
        try {
            return cppEngine.getSpread(symbol);
        } catch (Exception e) {
            log.error("Error getting spread for symbol: {}", symbol, e);
            return 0.0;
        }
    }
    
    /**
     * Check if symbol is halted
     */
    public boolean isSymbolHalted(String symbol) {
        try {
            return cppEngine.isSymbolHalted(symbol);
        } catch (Exception e) {
            log.error("Error checking halt status for symbol: {}", symbol, e);
            return false;
        }
    }
    
    /**
     * Cancel an order from the C++ engine
     */
    public boolean cancelOrder(String orderId) {
        try {
            log.info("Cancelling order: {}", orderId);
            boolean success = cppEngine.removeOrder(orderId);
            
            if (success) {
                log.info("Order {} cancelled successfully", orderId);
            } else {
                log.warn("Failed to cancel order: {}", orderId);
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error cancelling order: {}", orderId, e);
            return false;
        }
    }
    
    /**
     * Update an existing order in the C++ engine
     */
    public boolean updateOrder(Order order) {
        try {
            log.info("Updating order: {} for user: {} symbol: {} side: {} quantity: {} price: {}", 
                order.getOrderId(), order.getUserId(), order.getSymbol(), 
                order.getSide(), order.getQuantity(), order.getPrice());
            
            if (!isValidOrder(order)) {
                log.warn("Invalid order for update: {}", order.getOrderId());
                return false;
            }
            
            boolean success = cppEngine.updateOrder(
                order.getOrderId(),
                order.getUserId(),
                order.getSymbol(),
                order.getSide(),
                order.getQuantity(),
                order.getPrice()
            );
            
            if (success) {
                log.info("Order {} updated successfully", order.getOrderId());
                // Publish order book update
                publishOrderBookUpdate(order.getSymbol());
            } else {
                log.warn("Failed to update order: {}", order.getOrderId());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error updating order: {}", order.getOrderId(), e);
            return false;
        }
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
     * Publish trade execution result to internal Kafka topic
     */
    private void publishTradeExecution(Trade trade) {
        try {
            String tradeJson = objectMapper.writeValueAsString(trade);
            
            // Publish to trades.executed topic (internal tracking)
            kafkaTemplate.send("trades.executed", trade.getTradeId(), tradeJson);
            
            log.debug("Trade execution published to internal topic: {}", trade.getTradeId());
            
        } catch (Exception e) {
            log.error("Error publishing trade execution: {}", trade.getTradeId(), e);
        }
    }
    
    /**
     * Publish trade to external Kafka topic for consumption by other services
     */
    private void publishTradeToExternalTopic(Trade trade) {
        try {
            String tradeJson = objectMapper.writeValueAsString(trade);
            
            // Publish to trades.out topic (external consumption)
            kafkaTemplate.send("trades.out", trade.getTradeId(), tradeJson);
            
            log.info("🚀 Trade published to external topic trades.out: {}", trade.getTradeId());
            
        } catch (Exception e) {
            log.error("Error publishing trade to external topic: {}", trade.getTradeId(), e);
        }
    }
}
