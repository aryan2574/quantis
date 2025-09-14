package com.quantis.update_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.update_service.model.Trade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka consumer service for processing trade executions from the trading engine.
 * 
 * This service:
 * 1. Consumes trade messages from 'trades.out' topic
 * 2. Processes each trade to update order book state
 * 3. Maintains real-time order book snapshots in Redis
 * 4. Provides metrics for monitoring trade processing performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeConsumer {
    
    private final ObjectMapper objectMapper;
    private final OrderBookUpdateService orderBookUpdateService;
    
    // Performance metrics
    private final AtomicLong totalTradesProcessed = new AtomicLong(0);
    private final AtomicLong failedTradesProcessed = new AtomicLong(0);
    private final AtomicLong lastProcessedTimestamp = new AtomicLong(System.currentTimeMillis());
    
    /**
     * Kafka listener for processing trade executions.
     * 
     * @param message JSON trade message from Kafka
     * @param partition Kafka partition number
     * @param offset Kafka offset
     * @param timestamp Kafka message timestamp
     */
    @KafkaListener(
        topics = "trades.out",
        groupId = "update-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void processTrade(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        
        long startTime = System.nanoTime();
        
        try {
            log.debug("Processing trade from partition {} offset {}: {}", partition, offset, message);
            
            // Deserialize trade message
            Trade trade = objectMapper.readValue(message, Trade.class);
            
            // Validate trade
            if (!isValidTrade(trade)) {
                log.warn("Invalid trade received: {}", trade);
                failedTradesProcessed.incrementAndGet();
                return;
            }
            
            // Update order book with trade information
            orderBookUpdateService.updateOrderBookFromTrade(trade);
            
            // Update metrics
            totalTradesProcessed.incrementAndGet();
            lastProcessedTimestamp.set(System.currentTimeMillis());
            
            // Log performance for high-volume processing
            long processingTimeNs = System.nanoTime() - startTime;
            if (processingTimeNs > 1_000_000) { // Log if processing takes > 1ms
                log.warn("Slow trade processing: {}ns for trade {}", processingTimeNs, trade.getTradeId());
            }
            
            log.debug("Successfully processed trade {} for symbol {} in {}ns", 
                trade.getTradeId(), trade.getSymbol(), processingTimeNs);
            
        } catch (Exception e) {
            failedTradesProcessed.incrementAndGet();
            log.error("Failed to process trade message from partition {} offset {}: {}", 
                partition, offset, message, e);
            
            // In production, you might want to send to a dead letter queue
            // or implement retry logic with exponential backoff
        }
    }
    
    /**
     * Validate trade message
     */
    private boolean isValidTrade(Trade trade) {
        if (trade == null) {
            return false;
        }
        
        if (trade.getTradeId() == null || trade.getTradeId().isBlank()) {
            return false;
        }
        
        if (trade.getSymbol() == null || trade.getSymbol().isBlank()) {
            return false;
        }
        
        if (trade.getUserId() == null || trade.getUserId().isBlank()) {
            return false;
        }
        
        if (trade.getQuantity() <= 0) {
            return false;
        }
        
        if (trade.getPrice() <= 0) {
            return false;
        }
        
        if (trade.getSide() == null || (!trade.getSide().equals("BUY") && !trade.getSide().equals("SELL"))) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get processing metrics
     */
    public ProcessingMetrics getProcessingMetrics() {
        return ProcessingMetrics.builder()
            .totalTradesProcessed(totalTradesProcessed.get())
            .failedTradesProcessed(failedTradesProcessed.get())
            .lastProcessedTimestamp(lastProcessedTimestamp.get())
            .successRate(calculateSuccessRate())
            .build();
    }
    
    private double calculateSuccessRate() {
        long total = totalTradesProcessed.get() + failedTradesProcessed.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) totalTradesProcessed.get() / total * 100.0;
    }
    
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ProcessingMetrics {
        private long totalTradesProcessed;
        private long failedTradesProcessed;
        private long lastProcessedTimestamp;
        private double successRate;
    }
}
