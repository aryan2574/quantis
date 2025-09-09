package com.quantis.trading_engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.trading_engine.model.Order;
import com.quantis.trading_engine.service.TradingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer for processing orders from Risk Service.
 * Consumes both orders.valid and orders.rejected topics.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderConsumer {
    
    private final ObjectMapper objectMapper;
    private final TradingEngine tradingEngine;
    
    /**
     * Process valid orders from Risk Service
     */
    @KafkaListener(topics = "orders.valid", groupId = "trading-engine-group")
    public void processValidOrder(String message) {
        try {
            log.info("Processing valid order: {}", message);
            
            Order order = objectMapper.readValue(message, Order.class);
            order.setReceivedAt(java.time.Instant.now());
            order.setStatus(Order.OrderStatus.PENDING);
            
            // Execute the order
            tradingEngine.executeOrder(order);
            
        } catch (Exception e) {
            log.error("Error processing valid order: {}", message, e);
        }
    }
    
    /**
     * Process rejected orders from Risk Service
     */
    @KafkaListener(topics = "orders.rejected", groupId = "trading-engine-group")
    public void processRejectedOrder(String message) {
        try {
            log.info("Processing rejected order: {}", message);
            
            // Parse rejection message
            var rejection = objectMapper.readTree(message);
            String orderId = rejection.get("orderId").asText();
            String reason = rejection.get("reason").asText();
            
            // Log rejection and potentially notify user
            log.warn("Order {} rejected by Risk Service. Reason: {}", orderId, reason);
            
            // Could send notification to user service here
            
        } catch (Exception e) {
            log.error("Error processing rejected order: {}", message, e);
        }
    }
}
