package com.quantis.elasticsearch_writer_service.service;

import com.quantis.elasticsearch_writer_service.model.TradeAnalyticsDocument;
import com.quantis.elasticsearch_writer_service.model.TradeDocument;
import com.quantis.elasticsearch_writer_service.repository.TradeAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing trade analytics in Elasticsearch.
 * 
 * This service maintains pre-computed analytics and aggregations for efficient querying.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeAnalyticsService {
    
    private final TradeAnalyticsRepository tradeAnalyticsRepository;
    
    /**
     * Update analytics with a new trade document
     */
    public void updateAnalytics(TradeDocument tradeDocument) {
        try {
            // Update hourly analytics
            updateHourlyAnalytics(tradeDocument);
            
            // Update daily analytics
            updateDailyAnalytics(tradeDocument);
            
            log.debug("Updated analytics for trade: {}", tradeDocument.getTradeId());
            
        } catch (Exception e) {
            log.error("Error updating analytics for trade: {}", tradeDocument.getTradeId(), e);
            // Don't throw exception to avoid blocking trade processing
        }
    }
    
    /**
     * Update hourly analytics
     */
    private void updateHourlyAnalytics(TradeDocument tradeDocument) {
        String timePeriod = tradeDocument.getTradeHour();
        String analyticsId = String.format("%s_HOURLY_%s", tradeDocument.getSymbol(), timePeriod);
        
        TradeAnalyticsDocument existing = tradeAnalyticsRepository.findById(analyticsId).orElse(null);
        
        if (existing != null) {
            TradeAnalyticsDocument updated = updateExistingAnalytics(existing, tradeDocument);
            tradeAnalyticsRepository.save(updated);
        } else {
            TradeAnalyticsDocument newAnalytics = createNewAnalytics(tradeDocument, "HOURLY", timePeriod);
            tradeAnalyticsRepository.save(newAnalytics);
        }
    }
    
    /**
     * Update daily analytics
     */
    private void updateDailyAnalytics(TradeDocument tradeDocument) {
        String timePeriod = tradeDocument.getTradeDate();
        String analyticsId = String.format("%s_DAILY_%s", tradeDocument.getSymbol(), timePeriod);
        
        TradeAnalyticsDocument existing = tradeAnalyticsRepository.findById(analyticsId).orElse(null);
        
        if (existing != null) {
            TradeAnalyticsDocument updated = updateExistingAnalytics(existing, tradeDocument);
            tradeAnalyticsRepository.save(updated);
        } else {
            TradeAnalyticsDocument newAnalytics = createNewAnalytics(tradeDocument, "DAILY", timePeriod);
            tradeAnalyticsRepository.save(newAnalytics);
        }
    }
    
    /**
     * Update existing analytics document
     */
    private TradeAnalyticsDocument updateExistingAnalytics(TradeAnalyticsDocument existing, TradeDocument tradeDocument) {
        return existing.toBuilder()
                .totalTrades(existing.getTotalTrades() + 1)
                .totalVolume(existing.getTotalVolume() + tradeDocument.getQuantity())
                .totalValue(existing.getTotalValue() + tradeDocument.getTotalValue())
                .highPrice(Math.max(existing.getHighPrice(), tradeDocument.getPrice()))
                .lowPrice(Math.min(existing.getLowPrice(), tradeDocument.getPrice()))
                .closePrice(tradeDocument.getPrice()) // Last trade becomes close price
                .uniqueUsers(existing.getUniqueUsers())
                .buyOrders(existing.getBuyOrders() + ("BUY".equals(tradeDocument.getSide()) ? 1 : 0))
                .sellOrders(existing.getSellOrders() + ("SELL".equals(tradeDocument.getSide()) ? 1 : 0))
                .lastUpdated(Instant.now())
                .vwap(calculateVwap(existing.getTotalValue() + tradeDocument.getTotalValue(),
                                   existing.getTotalVolume() + tradeDocument.getQuantity()))
                .buySellRatio(calculateBuySellRatio(existing.getBuyOrders() + ("BUY".equals(tradeDocument.getSide()) ? 1 : 0),
                                                   existing.getSellOrders() + ("SELL".equals(tradeDocument.getSide()) ? 1 : 0)))
                .averageTradeSize(calculateAverageTradeSize(existing.getTotalVolume() + tradeDocument.getQuantity(),
                                                           existing.getTotalTrades() + 1))
                .largestTradeSize(Math.max(existing.getLargestTradeSize(), tradeDocument.getQuantity()))
                .dataQualityScore(calculateDataQualityScore(existing, tradeDocument))
                .build();
    }
    
    /**
     * Create new analytics document
     */
    private TradeAnalyticsDocument createNewAnalytics(TradeDocument tradeDocument, String aggregationType, String timePeriod) {
        return TradeAnalyticsDocument.builder()
                .analyticsId(String.format("%s_%s_%s", tradeDocument.getSymbol(), aggregationType, timePeriod))
                .symbol(tradeDocument.getSymbol())
                .aggregationType(aggregationType)
                .timePeriod(timePeriod)
                .periodStart(getPeriodStart(timePeriod, aggregationType))
                .periodEnd(getPeriodEnd(timePeriod, aggregationType))
                .totalTrades(1L)
                .totalVolume((double) tradeDocument.getQuantity())
                .totalValue(tradeDocument.getTotalValue())
                .openPrice(tradeDocument.getPrice())
                .closePrice(tradeDocument.getPrice())
                .highPrice(tradeDocument.getPrice())
                .lowPrice(tradeDocument.getPrice())
                .vwap(tradeDocument.getPrice())
                .priceChange(0.0)
                .priceChangePercent(0.0)
                .uniqueUsers(1L)
                .buyOrders("BUY".equals(tradeDocument.getSide()) ? 1L : 0L)
                .sellOrders("SELL".equals(tradeDocument.getSide()) ? 1L : 0L)
                .buySellRatio(calculateBuySellRatio(
                    "BUY".equals(tradeDocument.getSide()) ? 1L : 0L,
                    "SELL".equals(tradeDocument.getSide()) ? 1L : 0L))
                .averageTradeSize((double) tradeDocument.getQuantity())
                .largestTradeSize((double) tradeDocument.getQuantity())
                .tradeSizeStdDev(0.0)
                .priceVolatility(0.0)
                .marketDepth(calculateMarketDepth(tradeDocument.getSymbol()))
                .spread(calculateSpread(tradeDocument.getSymbol()))
                .averageSpread(calculateSpread(tradeDocument.getSymbol()))
                .tradingIntensity(calculateTradingIntensity(1L, aggregationType))
                .volumeIntensity(calculateVolumeIntensity((double) tradeDocument.getQuantity(), aggregationType))
                .marketImpactScore(calculateMarketImpactScore(tradeDocument))
                .liquidityScore(calculateLiquidityScore(tradeDocument.getSymbol(), calculateSpread(tradeDocument.getSymbol())))
                .lastUpdated(Instant.now())
                .dataQualityScore(1.0)
                .additionalMetrics(null)
                .build();
    }
    
    /**
     * Calculate Volume Weighted Average Price (VWAP)
     */
    private Double calculateVwap(Double totalValue, Double totalVolume) {
        return totalVolume > 0 ? totalValue / totalVolume : 0.0;
    }
    
    /**
     * Calculate Buy/Sell ratio
     */
    private Double calculateBuySellRatio(Long buyOrders, Long sellOrders) {
        return sellOrders > 0 ? (double) buyOrders / sellOrders : (buyOrders > 0 ? Double.MAX_VALUE : 0.0);
    }
    
    /**
     * Calculate average trade size
     */
    private Double calculateAverageTradeSize(Double totalVolume, Long totalTrades) {
        return totalTrades > 0 ? totalVolume / totalTrades : 0.0;
    }
    
    /**
     * Calculate data quality score
     */
    private Double calculateDataQualityScore(TradeAnalyticsDocument existing, TradeDocument tradeDocument) {
        // Simple data quality calculation based on completeness
        double score = 1.0;
        
        if (tradeDocument.getMetadata() == null || tradeDocument.getMetadata().isEmpty()) {
            score -= 0.1;
        }
        
        if (tradeDocument.getCounterpartyOrderId() == null || tradeDocument.getCounterpartyOrderId().isEmpty()) {
            score -= 0.1;
        }
        
        return Math.max(0.0, score);
    }
    
    /**
     * Get period start timestamp
     */
    private Instant getPeriodStart(String timePeriod, String aggregationType) {
        if ("HOURLY".equals(aggregationType)) {
            LocalDateTime dateTime = LocalDateTime.parse(timePeriod + ":00:00", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"));
            return dateTime.atZone(java.time.ZoneOffset.UTC).toInstant();
        } else if ("DAILY".equals(aggregationType)) {
            LocalDate date = LocalDate.parse(timePeriod);
            return date.atStartOfDay().atZone(java.time.ZoneOffset.UTC).toInstant();
        }
        return Instant.now();
    }
    
    /**
     * Get period end timestamp
     */
    private Instant getPeriodEnd(String timePeriod, String aggregationType) {
        if ("HOURLY".equals(aggregationType)) {
            LocalDateTime dateTime = LocalDateTime.parse(timePeriod + ":59:59", 
                DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mm:ss"));
            return dateTime.atZone(java.time.ZoneOffset.UTC).toInstant();
        } else if ("DAILY".equals(aggregationType)) {
            LocalDate date = LocalDate.parse(timePeriod);
            return date.atTime(23, 59, 59).atZone(java.time.ZoneOffset.UTC).toInstant();
        }
        return Instant.now();
    }
    
    /**
     * Get analytics by symbol
     */
    public List<TradeAnalyticsDocument> getAnalyticsBySymbol(String symbol) {
        return tradeAnalyticsRepository.findBySymbol(symbol);
    }
    
    /**
     * Get analytics by symbol and aggregation type
     */
    public List<TradeAnalyticsDocument> getAnalyticsBySymbolAndType(String symbol, String aggregationType) {
        return tradeAnalyticsRepository.findBySymbolAndAggregationType(symbol, aggregationType);
    }
    
    /**
     * Get analytics by symbol and time period range
     */
    public List<TradeAnalyticsDocument> getAnalyticsBySymbolAndTimeRange(String symbol, String startPeriod, String endPeriod) {
        return tradeAnalyticsRepository.findBySymbolAndTimePeriodBetween(symbol, startPeriod, endPeriod);
    }
    
    /**
     * Get latest analytics for a symbol
     */
    public List<TradeAnalyticsDocument> getLatestAnalytics(String symbol, int limit) {
        return tradeAnalyticsRepository.findLatestBySymbol(symbol, limit);
    }
    
    /**
     * Get top symbols by total value
     */
    public List<TradeAnalyticsDocument> getTopSymbolsByValue(String timePeriod, int limit) {
        return tradeAnalyticsRepository.findTopByTotalValue(timePeriod, limit);
    }
    
    /**
     * Get top symbols by volume
     */
    public List<TradeAnalyticsDocument> getTopSymbolsByVolume(String timePeriod, int limit) {
        return tradeAnalyticsRepository.findTopByTotalVolume(timePeriod, limit);
    }
    
    /**
     * Get top symbols by trade count
     */
    public List<TradeAnalyticsDocument> getTopSymbolsByTradeCount(String timePeriod, int limit) {
        return tradeAnalyticsRepository.findTopByTotalTrades(timePeriod, limit);
    }
    
    /**
     * Get market overview
     */
    public Object getMarketOverview(String timePeriod) {
        return tradeAnalyticsRepository.getMarketOverview(timePeriod);
    }
    
    /**
     * Get performance metrics for a symbol
     */
    public List<TradeAnalyticsDocument> getPerformanceMetrics(String symbol, String aggregationType) {
        return tradeAnalyticsRepository.getPerformanceMetrics(symbol, aggregationType);
    }
    
    /**
     * Calculate price change from previous period
     */
    private Double calculatePriceChange(TradeAnalyticsDocument existing, Double currentPrice) {
        if (existing == null || existing.getClosePrice() == null) {
            return 0.0;
        }
        return currentPrice - existing.getClosePrice();
    }
    
    /**
     * Calculate price change percentage from previous period
     */
    private Double calculatePriceChangePercent(TradeAnalyticsDocument existing, Double currentPrice) {
        if (existing == null || existing.getClosePrice() == null || existing.getClosePrice() == 0.0) {
            return 0.0;
        }
        return ((currentPrice - existing.getClosePrice()) / existing.getClosePrice()) * 100.0;
    }
    
    /**
     * Calculate market depth from order book data
     */
    private Double calculateMarketDepth(String symbol) {
        // Integrate with order book service to get real market depth
        // For now, return a simulated value based on symbol
        return symbol.length() * 1000.0; // Simple simulation
    }
    
    /**
     * Calculate spread from market data
     */
    private Double calculateSpread(String symbol) {
        // Integrate with market data service to get real spread
        // For now, return a simulated value
        return 0.01; // 1 cent spread simulation
    }
    
    /**
     * Calculate trading intensity based on period duration
     */
    private Double calculateTradingIntensity(Long totalTrades, String aggregationType) {
        if (totalTrades == null || totalTrades == 0) {
            return 0.0;
        }
        
        long periodSeconds = getPeriodDurationSeconds(aggregationType);
        return (double) totalTrades / periodSeconds;
    }
    
    /**
     * Calculate volume intensity based on period duration
     */
    private Double calculateVolumeIntensity(Double totalVolume, String aggregationType) {
        if (totalVolume == null || totalVolume == 0) {
            return 0.0;
        }
        
        long periodSeconds = getPeriodDurationSeconds(aggregationType);
        return totalVolume / periodSeconds;
    }
    
    /**
     * Calculate market impact score
     */
    private Double calculateMarketImpactScore(TradeDocument tradeDocument) {
        // Simple market impact calculation based on trade size relative to average
        double tradeValue = tradeDocument.getTotalValue();
        if (tradeValue > 100000) { // Large trade threshold
            return Math.min(1.0, tradeValue / 1000000.0); // Scale to 0-1
        }
        return 0.0;
    }
    
    /**
     * Calculate liquidity score
     */
    private Double calculateLiquidityScore(String symbol, Double spread) {
        // Simple liquidity calculation based on spread
        if (spread == null || spread == 0) {
            return 1.0; // Perfect liquidity
        }
        return Math.max(0.0, 1.0 - (spread / 0.1)); // Scale based on spread
    }
    
    /**
     * Get period duration in seconds
     */
    private long getPeriodDurationSeconds(String aggregationType) {
        switch (aggregationType.toUpperCase()) {
            case "HOURLY":
                return 3600; // 1 hour
            case "DAILY":
                return 86400; // 24 hours
            case "WEEKLY":
                return 604800; // 7 days
            case "MONTHLY":
                return 2592000; // 30 days
            default:
                return 3600; // Default to hourly
        }
    }
}
