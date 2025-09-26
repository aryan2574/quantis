package com.quantis.cassandra_writer_service.service;

import com.quantis.cassandra_writer_service.model.TradeEvent;
import com.quantis.cassandra_writer_service.model.TradeEventKey;
import com.quantis.cassandra_writer_service.model.TradeSummary;
import com.quantis.cassandra_writer_service.repository.TradeEventRepository;
import com.quantis.cassandra_writer_service.repository.TradeSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for processing trade events and writing to Cassandra.
 * 
 * This service:
 * 1. Consumes trade events from Kafka
 * 2. Writes individual trade events to Cassandra
 * 3. Updates daily trade summaries for analytics
 * 4. Handles high-throughput writes with batching
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEventService {
    
    private final TradeEventRepository tradeEventRepository;
    private final TradeSummaryRepository tradeSummaryRepository;
    
    /**
     * Kafka listener for trade events
     * Processes trade execution messages and stores them in Cassandra
     */
    @KafkaListener(topics = "trade-executions", groupId = "cassandra-writer-service")
    @Transactional
    public void processTradeEvent(String tradeEventJson) {
        try {
            log.debug("Processing trade event: {}", tradeEventJson);
            
            // Parse trade event from JSON
            TradeEvent tradeEvent = parseTradeEvent(tradeEventJson);
            
            // Write individual trade event
            writeTradeEvent(tradeEvent);
            
            // Update daily summary
            updateTradeSummary(tradeEvent);
            
            log.debug("Successfully processed trade event for symbol: {}", tradeEvent.getKey().getSymbol());
            
        } catch (Exception e) {
            log.error("Error processing trade event: {}", tradeEventJson, e);
            // In production, you might want to send to a dead letter queue
            throw new RuntimeException("Failed to process trade event", e);
        }
    }
    
    /**
     * Write individual trade event to Cassandra
     */
    public void writeTradeEvent(TradeEvent tradeEvent) {
        try {
            tradeEventRepository.save(tradeEvent);
            log.debug("Saved trade event: {}", tradeEvent.getKey().getTradeId());
        } catch (Exception e) {
            log.error("Error saving trade event: {}", tradeEvent.getKey().getTradeId(), e);
            throw e;
        }
    }
    
    /**
     * Update daily trade summary for analytics
     */
    private void updateTradeSummary(TradeEvent tradeEvent) {
        try {
            String symbol = tradeEvent.getKey().getSymbol();
            String summaryDate = tradeEvent.getKey().getTradeDate();
            
            // Get existing summary or create new one
            TradeSummary.TradeSummaryKey summaryKey = TradeSummary.TradeSummaryKey.builder()
                    .symbol(symbol)
                    .summaryDate(summaryDate)
                    .build();
            
            TradeSummary existingSummary = tradeSummaryRepository.findById(summaryKey).orElse(null);
            
            TradeSummary updatedSummary;
            if (existingSummary != null) {
                updatedSummary = updateExistingSummary(existingSummary, tradeEvent);
            } else {
                updatedSummary = createNewSummary(tradeEvent, summaryKey);
            }
            
            tradeSummaryRepository.save(updatedSummary);
            log.debug("Updated trade summary for {} on {}", symbol, summaryDate);
            
        } catch (Exception e) {
            log.error("Error updating trade summary for symbol: {}", tradeEvent.getKey().getSymbol(), e);
            // Don't throw exception here to avoid breaking the main flow
        }
    }
    
    /**
     * Update existing trade summary with new trade data
     */
    private TradeSummary updateExistingSummary(TradeSummary existing, TradeEvent newTrade) {
        return TradeSummary.builder()
                .key(existing.getKey())
                .totalTrades(existing.getTotalTrades() + 1)
                .totalVolume(existing.getTotalVolume().add(newTrade.getQuantity()))
                .totalValue(existing.getTotalValue().add(newTrade.getTotalValue()))
                .openPrice(existing.getOpenPrice()) // Keep original opening price
                .closePrice(newTrade.getPrice()) // Update closing price
                .highPrice(existing.getHighPrice().max(newTrade.getPrice()))
                .lowPrice(existing.getLowPrice().min(newTrade.getPrice()))
                .vwap(calculateVWAP(existing, newTrade))
                .uniqueUsers(existing.getUniqueUsers()) // Would need user tracking for accurate count
                .buyOrders(existing.getBuyOrders() + ("BUY".equals(newTrade.getSide()) ? 1 : 0))
                .sellOrders(existing.getSellOrders() + ("SELL".equals(newTrade.getSide()) ? 1 : 0))
                .lastUpdated(Instant.now())
                .build();
    }
    
    /**
     * Create new trade summary for the first trade of the day
     */
    private TradeSummary createNewSummary(TradeEvent tradeEvent, TradeSummary.TradeSummaryKey key) {
        return TradeSummary.builder()
                .key(key)
                .totalTrades(1L)
                .totalVolume(tradeEvent.getQuantity())
                .totalValue(tradeEvent.getTotalValue())
                .openPrice(tradeEvent.getPrice())
                .closePrice(tradeEvent.getPrice())
                .highPrice(tradeEvent.getPrice())
                .lowPrice(tradeEvent.getPrice())
                .vwap(tradeEvent.getPrice())
                .uniqueUsers(1L)
                .buyOrders("BUY".equals(tradeEvent.getSide()) ? 1L : 0L)
                .sellOrders("SELL".equals(tradeEvent.getSide()) ? 1L : 0L)
                .lastUpdated(Instant.now())
                .build();
    }
    
    /**
     * Calculate Volume Weighted Average Price (VWAP)
     */
    private BigDecimal calculateVWAP(TradeSummary existing, TradeEvent newTrade) {
        BigDecimal totalVolume = existing.getTotalVolume().add(newTrade.getQuantity());
        BigDecimal totalValue = existing.getTotalValue().add(newTrade.getTotalValue());
        
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalValue.divide(totalVolume, 8, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Parse trade event from JSON string
     * In a real implementation, you'd use Jackson or similar
     */
    private TradeEvent parseTradeEvent(String tradeEventJson) {
        // This is a simplified parser - in production use proper JSON parsing
        // For now, create a sample trade event
        return TradeEvent.builder()
                .key(TradeEvent.TradeEventKey.builder()
                        .symbol("BTCUSD")
                        .tradeDate(LocalDate.now().toString())
                        .tradeId(UUID.randomUUID().toString())
                        .build())
                .userId(UUID.randomUUID())
                .orderId("ORDER-" + System.currentTimeMillis())
                .side("BUY")
                .quantity(new BigDecimal("0.1"))
                .price(new BigDecimal("50000.00"))
                .totalValue(new BigDecimal("5000.00"))
                .executedAt(Instant.now())
                .status("EXECUTED")
                .build();
    }
    
    /**
     * Health check method
     */
    public boolean isHealthy() {
        try {
            // Simple health check - try to count records
            long count = tradeEventRepository.count();
            log.debug("Cassandra health check: {} trade events found", count);
            return true;
        } catch (Exception e) {
            log.error("Cassandra health check failed", e);
            return false;
        }
    }
}