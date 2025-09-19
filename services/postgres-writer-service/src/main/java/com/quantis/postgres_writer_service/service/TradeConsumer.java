package com.quantis.postgres_writer_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.postgres_writer_service.model.TradeEntity;
import com.quantis.postgres_writer_service.model.PortfolioSnapshotEntity;
import com.quantis.postgres_writer_service.repository.TradeRepository;
import com.quantis.postgres_writer_service.repository.PortfolioSnapshotRepository;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka consumer service for processing trade executions and persisting to PostgreSQL.
 * 
 * This service:
 * 1. Consumes trade messages from 'trades.out' topic
 * 2. Persists trade data to PostgreSQL with ACID compliance
 * 3. Creates portfolio snapshots for audit trails
 * 4. Maintains data consistency and integrity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeConsumer {
    
    private final ObjectMapper objectMapper;
    private final TradeRepository tradeRepository;
    private final PortfolioSnapshotRepository portfolioSnapshotRepository;
    private final PortfolioSnapshotService portfolioSnapshotService;
    
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
        groupId = "postgres-writer-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void processTrade(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TIMESTAMP) long timestamp) {
        
        long startTime = System.nanoTime();
        
        try {
            log.debug("Processing trade from partition {} offset {}: {}", partition, offset, message);
            
            // Deserialize trade message
            TradeMessage tradeMessage = objectMapper.readValue(message, TradeMessage.class);
            
            // Validate trade
            if (!isValidTrade(tradeMessage)) {
                log.warn("Invalid trade received: {}", tradeMessage);
                failedTradesProcessed.incrementAndGet();
                return;
            }
            
            // Check for duplicate trades
            if (tradeRepository.existsByTradeId(tradeMessage.getTradeId())) {
                log.warn("Duplicate trade detected: {}", tradeMessage.getTradeId());
                failedTradesProcessed.incrementAndGet();
                return;
            }
            
            // Convert to entity and persist
            TradeEntity tradeEntity = convertToEntity(tradeMessage);
            TradeEntity savedTrade = tradeRepository.save(tradeEntity);
            
            // Create portfolio snapshot
            portfolioSnapshotService.createSnapshotFromTrade(savedTrade);
            
            // Update metrics
            totalTradesProcessed.incrementAndGet();
            lastProcessedTimestamp.set(System.currentTimeMillis());
            
            // Log performance for high-volume processing
            long processingTimeNs = System.nanoTime() - startTime;
            if (processingTimeNs > 10_000_000) { // Log if processing takes > 10ms
                log.warn("Slow trade processing: {}ns for trade {}", processingTimeNs, tradeMessage.getTradeId());
            }
            
            log.debug("Successfully processed trade {} for symbol {} in {}ns", 
                tradeMessage.getTradeId(), tradeMessage.getSymbol(), processingTimeNs);
            
        } catch (Exception e) {
            failedTradesProcessed.incrementAndGet();
            log.error("Failed to process trade message from partition {} offset {}: {}", 
                partition, offset, message, e);
            
            // In production, you might want to send to a dead letter queue
            // or implement retry logic with exponential backoff
        }
    }
    
    /**
     * Convert TradeMessage to TradeEntity
     */
    private TradeEntity convertToEntity(TradeMessage tradeMessage) {
        return TradeEntity.builder()
            .tradeId(tradeMessage.getTradeId())
            .orderId(tradeMessage.getOrderId())
            .userId(UUID.fromString(tradeMessage.getUserId()))
            .symbol(tradeMessage.getSymbol())
            .side(TradeEntity.TradeSide.valueOf(tradeMessage.getSide()))
            .quantity(new BigDecimal(tradeMessage.getQuantity()))
            .price(new BigDecimal(tradeMessage.getPrice()))
            .totalValue(new BigDecimal(tradeMessage.getTotalValue()))
            .executedAt(tradeMessage.getExecutedAt())
            .status(TradeEntity.TradeStatus.valueOf(tradeMessage.getStatus()))
            .counterpartyOrderId(tradeMessage.getCounterpartyOrderId())
            .marketSession(tradeMessage.getMarketSession())
            .metadata(tradeMessage.getMetadata())
            .build();
    }
    
    /**
     * Validate trade message
     */
    private boolean isValidTrade(TradeMessage trade) {
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
        
        try {
            UUID.fromString(trade.getUserId());
        } catch (IllegalArgumentException e) {
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
    
    /**
     * Trade message model for Kafka deserialization
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
}
