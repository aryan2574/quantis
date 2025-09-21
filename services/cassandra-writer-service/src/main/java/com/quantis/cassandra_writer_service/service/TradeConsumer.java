package com.quantis.cassandra_writer_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.cassandra_writer_service.model.TradeEvent;
import com.quantis.cassandra_writer_service.model.TradeSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka consumer for processing trade events and writing to Cassandra.
 * 
 * This service consumes trades from the 'trades.out' topic and writes
 * both individual trade events and aggregated summaries to Cassandra.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeConsumer {
    
    private final ObjectMapper objectMapper;
    private final TradeEventService tradeEventService;
    private final TradeSummaryService tradeSummaryService;
    
    // Performance metrics
    private final AtomicLong totalTradesProcessed = new AtomicLong(0);
    private final AtomicLong failedTradesProcessed = new AtomicLong(0);
    private final AtomicLong lastProcessedTimestamp = new AtomicLong(System.currentTimeMillis());
    
    /**
     * Process trade messages from Kafka
     */
    @KafkaListener(
        topics = "trades.out",
        groupId = "cassandra-writer-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void processTrade(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Parse the trade message
            TradeMessage tradeMessage = objectMapper.readValue(message, TradeMessage.class);
            
            // Validate the trade
            if (!isValidTrade(tradeMessage)) {
                log.warn("Invalid trade message received: {}", tradeMessage);
                failedTradesProcessed.incrementAndGet();
                return;
            }
            
            // Convert to TradeEvent entity
            TradeEvent tradeEvent = convertToTradeEvent(tradeMessage, partition, offset);
            
            // Write to Cassandra
            tradeEventService.writeTradeEvent(tradeEvent);
            
            // Update daily summary
            tradeSummaryService.updateDailySummary(tradeEvent);
            
            // Update metrics
            totalTradesProcessed.incrementAndGet();
            lastProcessedTimestamp.set(System.currentTimeMillis());
            
            long processingTime = System.currentTimeMillis() - startTime;
            if (processingTime > 100) { // Log slow processing
                log.debug("Trade processing took {}ms for trade: {}", processingTime, tradeMessage.getTradeId());
            }
            
        } catch (Exception e) {
            log.error("Error processing trade message: {}", message, e);
            failedTradesProcessed.incrementAndGet();
            
            // Could implement dead letter queue here for failed messages
        }
    }
    
    /**
     * Convert Kafka message to TradeEvent entity
     */
    private TradeEvent convertToTradeEvent(TradeMessage tradeMessage, int partition, long offset) {
        String tradeDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        
        return TradeEvent.builder()
                .key(TradeEvent.TradeEventKey.builder()
                        .symbol(tradeMessage.getSymbol())
                        .tradeDate(tradeDate)
                        .tradeId(tradeMessage.getTradeId())
                        .build())
                .userId(UUID.fromString(tradeMessage.getUserId()))
                .orderId(tradeMessage.getOrderId())
                .side(tradeMessage.getSide())
                .quantity(BigDecimal.valueOf(tradeMessage.getQuantity()))
                .price(BigDecimal.valueOf(tradeMessage.getPrice()))
                .totalValue(BigDecimal.valueOf(tradeMessage.getTotalValue()))
                .executedAt(tradeMessage.getExecutedAt())
                .status(tradeMessage.getStatus())
                .counterpartyOrderId(tradeMessage.getCounterpartyOrderId())
                .marketSession(tradeMessage.getMarketSession())
                .metadata(tradeMessage.getMetadata())
                .rawData(tryWriteValueAsString(tradeMessage))
                .processedAt(Instant.now())
                .sequenceNumber(System.currentTimeMillis())
                .eventType("EXECUTION")
                .marketDataSnapshot(null) // Could be populated from market data service
                .build();
    }
    
    /**
     * Validate trade message
     */
    private boolean isValidTrade(TradeMessage trade) {
        return trade != null
                && trade.getTradeId() != null && !trade.getTradeId().isEmpty()
                && trade.getSymbol() != null && !trade.getSymbol().isEmpty()
                && trade.getUserId() != null && !trade.getUserId().isEmpty()
                && trade.getSide() != null && (trade.getSide().equals("BUY") || trade.getSide().equals("SELL"))
                && trade.getQuantity() > 0
                && trade.getPrice() > 0
                && trade.getExecutedAt() != null;
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
        return total > 0 ? (double) totalTradesProcessed.get() / total * 100.0 : 0.0;
    }
    
    /**
     * Trade message DTO for Kafka deserialization
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TradeMessage {
        private String tradeId;
        private String orderId;
        private String userId;
        private String symbol;
        private String side;
        private long quantity;
        private double price;
        private double totalValue;
        private Instant executedAt;
        private String status;
        private String counterpartyOrderId;
        private String marketSession;
        private String metadata;
    }
    
    /**
     * Processing metrics DTO
     */
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
    
    /**
     * Helper method to safely convert object to JSON string
     */
    private String tryWriteValueAsString(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            log.warn("Failed to serialize object to JSON, using toString: {}", e.getMessage());
            return value.toString();
        }
    }
}
