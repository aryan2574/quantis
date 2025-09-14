package com.quantis.cassandra_writer_service.service;

import com.quantis.cassandra_writer_service.model.TradeEvent;
import com.quantis.cassandra_writer_service.repository.TradeEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing trade events in Cassandra.
 * 
 * This service provides high-performance operations for time-series trade data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeEventService {
    
    private final TradeEventRepository tradeEventRepository;
    
    /**
     * Save a trade event to Cassandra
     */
    public void saveTradeEvent(TradeEvent tradeEvent) {
        try {
            tradeEventRepository.save(tradeEvent);
            log.debug("Saved trade event: {}", tradeEvent.getKey().getTradeId());
        } catch (Exception e) {
            log.error("Error saving trade event: {}", tradeEvent.getKey().getTradeId(), e);
            throw new RuntimeException("Failed to save trade event", e);
        }
    }
    
    /**
     * Find trade events for a symbol within date range
     */
    public List<TradeEvent> findTradesBySymbolAndDateRange(String symbol, String startDate, String endDate) {
        return tradeEventRepository.findBySymbolAndDateRange(symbol, startDate, endDate);
    }
    
    /**
     * Find trade events for a user
     */
    public List<TradeEvent> findTradesByUser(UUID userId) {
        return tradeEventRepository.findByUserId(userId);
    }
    
    /**
     * Find trade events by symbol and time range
     */
    public List<TradeEvent> findTradesBySymbolAndTimeRange(String symbol, Instant startTime, Instant endTime) {
        return tradeEventRepository.findBySymbolAndExecutedAtRange(symbol, startTime, endTime);
    }
    
    /**
     * Find latest trades for a symbol
     */
    public List<TradeEvent> findLatestTrades(String symbol, int limit) {
        return tradeEventRepository.findLatestBySymbol(symbol, limit);
    }
    
    /**
     * Find trades by order ID
     */
    public List<TradeEvent> findTradesByOrderId(String orderId) {
        return tradeEventRepository.findByOrderId(orderId);
    }
    
    /**
     * Count trades for a symbol on a specific date
     */
    public Long countTradesBySymbolAndDate(String symbol, String tradeDate) {
        return tradeEventRepository.countBySymbolAndDate(symbol, tradeDate);
    }
    
    /**
     * Find trades by status
     */
    public List<TradeEvent> findTradesByStatus(String status) {
        return tradeEventRepository.findByStatus(status);
    }
    
    /**
     * Find trades by symbol and side
     */
    public List<TradeEvent> findTradesBySymbolAndSide(String symbol, String side) {
        return tradeEventRepository.findBySymbolAndSide(symbol, side);
    }
}
