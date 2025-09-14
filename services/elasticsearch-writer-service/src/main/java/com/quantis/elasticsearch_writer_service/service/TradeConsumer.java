package com.quantis.elasticsearch_writer_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.elasticsearch_writer_service.model.TradeDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Kafka consumer for processing trade events and writing to Elasticsearch.
 * 
 * This service consumes trades from the 'trades.out' topic and creates
 * searchable documents in Elasticsearch with enhanced analytics fields.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeConsumer {
    
    private final ObjectMapper objectMapper;
    private final TradeDocumentService tradeDocumentService;
    private final TradeAnalyticsService tradeAnalyticsService;
    
    // Performance metrics
    private final AtomicLong totalTradesProcessed = new AtomicLong(0);
    private final AtomicLong failedTradesProcessed = new AtomicLong(0);
    private final AtomicLong lastProcessedTimestamp = new AtomicLong(System.currentTimeMillis());
    
    /**
     * Process trade messages from Kafka
     */
    @KafkaListener(
        topics = "trades.out",
        groupId = "elasticsearch-writer-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
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
            
            // Convert to TradeDocument with enhanced analytics
            TradeDocument tradeDocument = convertToTradeDocument(tradeMessage, startTime);
            
            // Save to Elasticsearch
            tradeDocumentService.saveTradeDocument(tradeDocument);
            
            // Update analytics aggregations
            tradeAnalyticsService.updateAnalytics(tradeDocument);
            
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
     * Convert Kafka message to TradeDocument with enhanced analytics
     */
    private TradeDocument convertToTradeDocument(TradeMessage tradeMessage, long startTime) {
        Instant executedAt = tradeMessage.getExecutedAt();
        LocalDate tradeDate = executedAt.atZone(java.time.ZoneOffset.UTC).toLocalDate();
        LocalDateTime tradeDateTime = executedAt.atZone(java.time.ZoneOffset.UTC).toLocalDateTime();
        
        return TradeDocument.builder()
                .tradeId(tradeMessage.getTradeId())
                .orderId(tradeMessage.getOrderId())
                .userId(UUID.fromString(tradeMessage.getUserId()))
                .symbol(tradeMessage.getSymbol())
                .side(tradeMessage.getSide())
                .quantity(tradeMessage.getQuantity())
                .price(tradeMessage.getPrice())
                .totalValue(tradeMessage.getTotalValue())
                .executedAt(executedAt)
                .status(tradeMessage.getStatus())
                .counterpartyOrderId(tradeMessage.getCounterpartyOrderId())
                .marketSession(tradeMessage.getMarketSession())
                .metadata(tradeMessage.getMetadata())
                .processedAt(Instant.now())
                .sequenceNumber(System.currentTimeMillis())
                .eventType("EXECUTION")
                .marketDataSnapshot(null) // Could be populated from market data service
                .rawData(tradeMessage.toString())
                .tradeDate(tradeDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .tradeHour(tradeDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")))
                .priceRange(categorizePrice(tradeMessage.getPrice()))
                .volumeCategory(categorizeVolume(tradeMessage.getQuantity()))
                .userCategory(categorizeUser(tradeMessage.getUserId()))
                .symbolCategory(categorizeSymbol(tradeMessage.getSymbol()))
                .priceChange(0.0)
                .volumeWeightedPrice(calculateVWAP(tradeMessage))
                .isLargeTrade(isLargeTrade(tradeMessage))
                .isOffHoursTrade(isOffHoursTrade(executedAt))
                .tags(generateTags(tradeMessage))
                .region("US")
                .country("USA")
                .isHighRisk(calculateIsHighRisk(tradeMessage))
                .riskScore(calculateRiskScore(tradeMessage))
                .processingLatencyMs(System.currentTimeMillis() - startTime)
                .processingStatus("SUCCESS")
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
     * Categorize price for analytics
     */
    private String categorizePrice(double price) {
        if (price < 10) return "LOW";
        if (price < 50) return "MEDIUM";
        if (price < 100) return "HIGH";
        return "PREMIUM";
    }
    
    /**
     * Categorize volume for analytics
     */
    private String categorizeVolume(long quantity) {
        if (quantity < 100) return "SMALL";
        if (quantity < 1000) return "MEDIUM";
        if (quantity < 10000) return "LARGE";
        return "BLOCK";
    }
    
    /**
     * Categorize user for analytics
     */
    private String categorizeUser(String userId) {
        // Simple user categorization based on UUID characteristics
        // In a real system, this would query user profiles/database
        if (userId == null || userId.isEmpty()) {
            return "UNKNOWN";
        }
        
        // Extract numeric part from UUID for categorization
        String numericPart = userId.replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) {
            return "REGULAR";
        }
        
        // Use last digit for categorization
        int lastDigit = Integer.parseInt(numericPart.substring(numericPart.length() - 1));
        
        if (lastDigit >= 0 && lastDigit <= 2) {
            return "RETAIL";
        } else if (lastDigit >= 3 && lastDigit <= 5) {
            return "REGULAR";
        } else if (lastDigit >= 6 && lastDigit <= 7) {
            return "PREMIUM";
        } else {
            return "INSTITUTIONAL";
        }
    }
    
    /**
     * Categorize symbol for analytics
     */
    private String categorizeSymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return "UNKNOWN";
        }
        
        String upperSymbol = symbol.toUpperCase();
        
        // Categorize based on symbol patterns
        if (upperSymbol.endsWith(".TO") || upperSymbol.endsWith(".V")) {
            return "CANADIAN_EQUITY";
        } else if (upperSymbol.endsWith(".L") || upperSymbol.endsWith(".LN")) {
            return "UK_EQUITY";
        } else if (upperSymbol.endsWith(".DE") || upperSymbol.endsWith(".F")) {
            return "GERMAN_EQUITY";
        } else if (upperSymbol.endsWith(".PA")) {
            return "FRENCH_EQUITY";
        } else if (upperSymbol.contains("BTC") || upperSymbol.contains("ETH") || upperSymbol.contains("USDT")) {
            return "CRYPTO";
        } else if (upperSymbol.endsWith("=X") || upperSymbol.contains("USD") || upperSymbol.contains("EUR")) {
            return "FOREX";
        } else if (upperSymbol.contains("GC") || upperSymbol.contains("SI") || upperSymbol.contains("CL")) {
            return "COMMODITY";
        } else if (upperSymbol.length() <= 4 && upperSymbol.matches("[A-Z]+")) {
            return "US_EQUITY";
        } else if (upperSymbol.contains("ETF") || upperSymbol.contains("SPY") || upperSymbol.contains("QQQ")) {
            return "ETF";
        } else {
            return "EQUITY";
        }
    }
    
    /**
     * Check if trade is large
     */
    private boolean isLargeTrade(TradeMessage trade) {
        return trade.getTotalValue() > 100000; // $100k threshold
    }
    
    /**
     * Check if trade is off-hours
     */
    private boolean isOffHoursTrade(Instant executedAt) {
        int hour = executedAt.atZone(java.time.ZoneOffset.UTC).getHour();
        return hour < 9 || hour > 16; // Outside 9 AM - 4 PM UTC
    }
    
    /**
     * Generate tags for flexible querying
     */
    private String[] generateTags(TradeMessage trade) {
        java.util.List<String> tags = new java.util.ArrayList<>();
        
        // Add side tag
        tags.add(trade.getSide());
        
        // Add user category tag
        tags.add(categorizeUser(trade.getUserId()));
        
        // Add symbol category tag
        tags.add(categorizeSymbol(trade.getSymbol()));
        
        // Add price category tag
        tags.add(categorizePrice(trade.getPrice()));
        
        // Add volume category tag
        tags.add(categorizeVolume(trade.getQuantity()));
        
        // Add time-based tags
        if (isOffHoursTrade(trade.getExecutedAt())) {
            tags.add("OFF_HOURS");
        } else {
            tags.add("MARKET_HOURS");
        }
        
        // Add size-based tags
        if (isLargeTrade(trade)) {
            tags.add("LARGE_TRADE");
        } else {
            tags.add("STANDARD_TRADE");
        }
        
        return tags.toArray(new String[0]);
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
     * Calculate if trade is high risk
     */
    private boolean calculateIsHighRisk(TradeMessage trade) {
        double riskScore = calculateRiskScore(trade);
        return riskScore > 0.7; // Threshold for high risk
    }
    
    /**
     * Calculate comprehensive risk score for trade
     */
    private double calculateRiskScore(TradeMessage trade) {
        double riskScore = 0.0;
        
        // Size risk (larger trades = higher risk)
        if (trade.getTotalValue() > 1000000) { // $1M+
            riskScore += 0.3;
        } else if (trade.getTotalValue() > 100000) { // $100k+
            riskScore += 0.2;
        } else if (trade.getTotalValue() > 10000) { // $10k+
            riskScore += 0.1;
        }
        
        // Symbol risk (crypto and commodities are riskier)
        String symbolCategory = categorizeSymbol(trade.getSymbol());
        switch (symbolCategory) {
            case "CRYPTO":
                riskScore += 0.4;
                break;
            case "COMMODITY":
                riskScore += 0.3;
                break;
            case "FOREX":
                riskScore += 0.2;
                break;
            case "US_EQUITY":
                riskScore += 0.1;
                break;
            default:
                riskScore += 0.15;
        }
        
        // Time risk (off-hours trading is riskier)
        if (isOffHoursTrade(trade.getExecutedAt())) {
            riskScore += 0.2;
        }
        
        // User risk (institutional users have different risk profiles)
        String userCategory = categorizeUser(trade.getUserId());
        switch (userCategory) {
            case "INSTITUTIONAL":
                riskScore += 0.1; // Lower risk for institutional
                break;
            case "RETAIL":
                riskScore += 0.2; // Higher risk for retail
                break;
            case "UNKNOWN":
                riskScore += 0.3; // Highest risk for unknown users
                break;
        }
        
        // Price volatility risk (higher prices can be more volatile)
        if (trade.getPrice() > 1000) {
            riskScore += 0.1;
        } else if (trade.getPrice() < 1) {
            riskScore += 0.2; // Penny stocks are riskier
        }
        
        // Ensure score is between 0 and 1
        return Math.min(1.0, Math.max(0.0, riskScore));
    }
    
    /**
     * Calculate Volume Weighted Average Price (VWAP)
     * For individual trades, VWAP equals the trade price
     */
    private double calculateVWAP(TradeMessage trade) {
        // For individual trades, VWAP is simply the trade price
        // In a real system, this would calculate VWAP over a time window
        return trade.getPrice();
    }
}
