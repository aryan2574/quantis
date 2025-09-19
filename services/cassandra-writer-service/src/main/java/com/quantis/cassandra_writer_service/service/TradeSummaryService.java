package com.quantis.cassandra_writer_service.service;

import com.quantis.cassandra_writer_service.model.TradeEvent;
import com.quantis.cassandra_writer_service.model.TradeSummary;
import com.quantis.cassandra_writer_service.repository.TradeSummaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing trade summaries in Cassandra.
 * 
 * This service maintains daily aggregated statistics for efficient reporting.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSummaryService {
    
    private final TradeSummaryRepository tradeSummaryRepository;
    
    /**
     * Update daily summary with a new trade event
     */
    public void updateDailySummary(TradeEvent tradeEvent) {
        try {
            String summaryDate = tradeEvent.getKey().getTradeDate();
            String symbol = tradeEvent.getKey().getSymbol();
            
            // Get existing summary or create new one
            TradeSummary existingSummary = tradeSummaryRepository.findBySymbolAndDate(symbol, summaryDate);
            
            TradeSummary summary;
            if (existingSummary != null) {
                summary = updateExistingSummary(existingSummary, tradeEvent);
            } else {
                summary = createNewSummary(tradeEvent);
            }
            
            tradeSummaryRepository.save(summary);
            log.debug("Updated daily summary for {} on {}", symbol, summaryDate);
            
        } catch (Exception e) {
            log.error("Error updating daily summary for trade: {}", tradeEvent.getKey().getTradeId(), e);
            // Don't throw exception to avoid blocking trade event processing
        }
    }
    
    /**
     * Update existing summary with new trade data
     */
    private TradeSummary updateExistingSummary(TradeSummary existing, TradeEvent tradeEvent) {
        return existing.toBuilder()
                .totalTrades(existing.getTotalTrades() + 1)
                .totalVolume(existing.getTotalVolume().add(tradeEvent.getQuantity()))
                .totalValue(existing.getTotalValue().add(tradeEvent.getTotalValue()))
                .highPrice(tradeEvent.getPrice().max(existing.getHighPrice()))
                .lowPrice(tradeEvent.getPrice().min(existing.getLowPrice()))
                .closePrice(tradeEvent.getPrice()) // Last trade becomes close price
                .uniqueUsers(existing.getUniqueUsers())
                .buyOrders(existing.getBuyOrders() + ("BUY".equals(tradeEvent.getSide()) ? 1 : 0))
                .sellOrders(existing.getSellOrders() + ("SELL".equals(tradeEvent.getSide()) ? 1 : 0))
                .lastUpdated(Instant.now())
                .vwap(calculateVwap(existing.getTotalValue().add(tradeEvent.getTotalValue()),
                                   existing.getTotalVolume().add(tradeEvent.getQuantity())))
                .build();
    }
    
    /**
     * Create new summary from trade event
     */
    private TradeSummary createNewSummary(TradeEvent tradeEvent) {
        return TradeSummary.builder()
                .key(TradeSummary.TradeSummaryKey.builder()
                        .symbol(tradeEvent.getKey().getSymbol())
                        .summaryDate(tradeEvent.getKey().getTradeDate())
                        .build())
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
    private BigDecimal calculateVwap(BigDecimal totalValue, BigDecimal totalVolume) {
        if (totalVolume.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return totalValue.divide(totalVolume, 8, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Get trade summary for a symbol and date
     */
    public TradeSummary getSummary(String symbol, String date) {
        return tradeSummaryRepository.findBySymbolAndDate(symbol, date);
    }
    
    /**
     * Get trade summaries for a symbol within date range
     */
    public List<TradeSummary> getSummariesBySymbolAndDateRange(String symbol, String startDate, String endDate) {
        return tradeSummaryRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }
    
    /**
     * Get top symbols by volume for a specific date
     */
    public List<TradeSummary> getTopSymbolsByVolume(String date, int limit) {
        return tradeSummaryRepository.findTopByVolumeOnDate(date, limit);
    }
    
    /**
     * Get top symbols by trade count for a specific date
     */
    public List<TradeSummary> getTopSymbolsByTradeCount(String date, int limit) {
        return tradeSummaryRepository.findTopByTradeCountOnDate(date, limit);
    }
    
    /**
     * Get all summaries for a specific date
     */
    public List<TradeSummary> getSummariesByDate(String date) {
        return tradeSummaryRepository.findByDate(date);
    }
}
