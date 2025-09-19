package com.quantis.elasticsearch_writer_service.service;

import com.quantis.elasticsearch_writer_service.model.TradeDocument;
import com.quantis.elasticsearch_writer_service.repository.TradeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing trade documents in Elasticsearch.
 * 
 * This service provides high-performance operations for searchable trade data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeDocumentService {
    
    private final TradeDocumentRepository tradeDocumentRepository;
    
    /**
     * Save a trade document to Elasticsearch
     */
    public void saveTradeDocument(TradeDocument tradeDocument) {
        try {
            tradeDocumentRepository.save(tradeDocument);
            log.debug("Saved trade document: {}", tradeDocument.getTradeId());
        } catch (Exception e) {
            log.error("Error saving trade document: {}", tradeDocument.getTradeId(), e);
            throw new RuntimeException("Failed to save trade document", e);
        }
    }
    
    /**
     * Find trades by symbol
     */
    public List<TradeDocument> findTradesBySymbol(String symbol) {
        return tradeDocumentRepository.findBySymbol(symbol);
    }
    
    /**
     * Find trades by user ID
     */
    public List<TradeDocument> findTradesByUser(UUID userId) {
        return tradeDocumentRepository.findByUserId(userId);
    }
    
    /**
     * Find trades by symbol and date range
     */
    public List<TradeDocument> findTradesBySymbolAndDateRange(String symbol, Instant startTime, Instant endTime) {
        return tradeDocumentRepository.findBySymbolAndExecutedAtBetween(symbol, startTime, endTime);
    }
    
    /**
     * Find trades by user and date range
     */
    public List<TradeDocument> findTradesByUserAndDateRange(UUID userId, Instant startTime, Instant endTime) {
        return tradeDocumentRepository.findByUserIdAndExecutedAtBetween(userId, startTime, endTime);
    }
    
    /**
     * Find trades by symbol and side
     */
    public List<TradeDocument> findTradesBySymbolAndSide(String symbol, String side) {
        return tradeDocumentRepository.findBySymbolAndSide(symbol, side);
    }
    
    /**
     * Find trades by status
     */
    public List<TradeDocument> findTradesByStatus(String status) {
        return tradeDocumentRepository.findByStatus(status);
    }
    
    /**
     * Find trades by order ID
     */
    public List<TradeDocument> findTradesByOrderId(String orderId) {
        return tradeDocumentRepository.findByOrderId(orderId);
    }
    
    /**
     * Find large trades for a symbol
     */
    public List<TradeDocument> findLargeTrades(String symbol, Double threshold) {
        return tradeDocumentRepository.findLargeTradesBySymbol(symbol, threshold);
    }
    
    /**
     * Find trades by price range
     */
    public List<TradeDocument> findTradesByPriceRange(String symbol, Double minPrice, Double maxPrice) {
        return tradeDocumentRepository.findBySymbolAndPriceBetween(symbol, minPrice, maxPrice);
    }
    
    /**
     * Find trades by volume range
     */
    public List<TradeDocument> findTradesByVolumeRange(String symbol, Long minQuantity, Long maxQuantity) {
        return tradeDocumentRepository.findBySymbolAndQuantityBetween(symbol, minQuantity, maxQuantity);
    }
    
    /**
     * Search trades with full-text search
     */
    public List<TradeDocument> searchTradesByMetadata(String symbol, String searchText) {
        return tradeDocumentRepository.findBySymbolAndMetadataContaining(symbol, searchText);
    }
    
    /**
     * Find trades by multiple symbols
     */
    public List<TradeDocument> findTradesBySymbols(List<String> symbols) {
        return tradeDocumentRepository.findBySymbolIn(symbols);
    }
    
    /**
     * Find trades by tags
     */
    public List<TradeDocument> findTradesByTags(List<String> tags) {
        return tradeDocumentRepository.findByTagsIn(tags);
    }
    
    /**
     * Find high-risk trades
     */
    public List<TradeDocument> findHighRiskTrades() {
        return tradeDocumentRepository.findByIsHighRiskTrue();
    }
    
    /**
     * Find trades by region
     */
    public List<TradeDocument> findTradesByRegion(String region) {
        return tradeDocumentRepository.findByRegion(region);
    }
    
    /**
     * Find trades by country
     */
    public List<TradeDocument> findTradesByCountry(String country) {
        return tradeDocumentRepository.findByCountry(country);
    }
    
    /**
     * Find trades by trade hour
     */
    public List<TradeDocument> findTradesByHour(String tradeHour) {
        return tradeDocumentRepository.findByTradeHour(tradeHour);
    }
    
    /**
     * Find trades by price range category
     */
    public List<TradeDocument> findTradesByPriceCategory(String priceRange) {
        return tradeDocumentRepository.findByPriceRange(priceRange);
    }
    
    /**
     * Find trades by volume category
     */
    public List<TradeDocument> findTradesByVolumeCategory(String volumeCategory) {
        return tradeDocumentRepository.findByVolumeCategory(volumeCategory);
    }
    
    /**
     * Find trades by user category
     */
    public List<TradeDocument> findTradesByUserCategory(String userCategory) {
        return tradeDocumentRepository.findByUserCategory(userCategory);
    }
    
    /**
     * Find trades by symbol category
     */
    public List<TradeDocument> findTradesBySymbolCategory(String symbolCategory) {
        return tradeDocumentRepository.findBySymbolCategory(symbolCategory);
    }
    
    /**
     * Find trades with pagination
     */
    public Page<TradeDocument> findTradesBySymbolWithPagination(String symbol, Pageable pageable) {
        return tradeDocumentRepository.findBySymbolOrderByExecutedAtDesc(symbol, pageable);
    }
    
    /**
     * Find trades by user with pagination
     */
    public Page<TradeDocument> findTradesByUserWithPagination(UUID userId, Pageable pageable) {
        return tradeDocumentRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable);
    }
    
    /**
     * Complex search query
     */
    public List<TradeDocument> complexSearch(String symbol, Instant startTime, Instant endTime, 
                                           String side, Double minValue) {
        return tradeDocumentRepository.findComplexQuery(symbol, startTime, endTime, side, minValue);
    }
    
    /**
     * Get trade statistics
     */
    public Object getTradeStatistics() {
        return tradeDocumentRepository.getTradeStatistics();
    }
    
    /**
     * Find recent trades for a symbol
     */
    public List<TradeDocument> findRecentTrades(String symbol, int limit) {
        return tradeDocumentRepository.findRecentTradesBySymbol(symbol, limit);
    }
    
    /**
     * Get trade document by ID
     */
    public TradeDocument getTradeById(String tradeId) {
        return tradeDocumentRepository.findById(tradeId).orElse(null);
    }
    
    /**
     * Update trade document
     */
    public void updateTradeDocument(TradeDocument tradeDocument) {
        try {
            tradeDocumentRepository.save(tradeDocument);
            log.debug("Updated trade document: {}", tradeDocument.getTradeId());
        } catch (Exception e) {
            log.error("Error updating trade document: {}", tradeDocument.getTradeId(), e);
            throw new RuntimeException("Failed to update trade document", e);
        }
    }
    
    /**
     * Delete trade document
     */
    public void deleteTradeDocument(String tradeId) {
        try {
            tradeDocumentRepository.deleteById(tradeId);
            log.debug("Deleted trade document: {}", tradeId);
        } catch (Exception e) {
            log.error("Error deleting trade document: {}", tradeId, e);
            throw new RuntimeException("Failed to delete trade document", e);
        }
    }
    
    /**
     * Check if trade document exists
     */
    public boolean existsTradeDocument(String tradeId) {
        return tradeDocumentRepository.existsById(tradeId);
    }
    
    /**
     * Get total count of trade documents
     */
    public long getTotalTradeCount() {
        return tradeDocumentRepository.count();
    }
}
