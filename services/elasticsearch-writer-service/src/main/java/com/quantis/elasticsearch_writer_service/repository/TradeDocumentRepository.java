package com.quantis.elasticsearch_writer_service.repository;

import com.quantis.elasticsearch_writer_service.model.TradeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for accessing trade documents in Elasticsearch.
 * 
 * This repository provides optimized queries for searchable trade data.
 */
@Repository
public interface TradeDocumentRepository extends ElasticsearchRepository<TradeDocument, String> {
    
    /**
     * Find trades by symbol
     */
    List<TradeDocument> findBySymbol(String symbol);
    
    /**
     * Find trades by user ID
     */
    List<TradeDocument> findByUserId(UUID userId);
    
    /**
     * Find trades by symbol and date range
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}, {\"range\": {\"executedAt\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}]}}")
    List<TradeDocument> findBySymbolAndExecutedAtBetween(String symbol, Instant startTime, Instant endTime);
    
    /**
     * Find trades by user and date range
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"userId\": \"?0\"}}, {\"range\": {\"executedAt\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}]}}")
    List<TradeDocument> findByUserIdAndExecutedAtBetween(UUID userId, Instant startTime, Instant endTime);
    
    /**
     * Find trades by symbol and side
     */
    List<TradeDocument> findBySymbolAndSide(String symbol, String side);
    
    /**
     * Find trades by status
     */
    List<TradeDocument> findByStatus(String status);
    
    /**
     * Find trades by order ID
     */
    List<TradeDocument> findByOrderId(String orderId);
    
    /**
     * Find large trades (above threshold)
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}, {\"range\": {\"totalValue\": {\"gte\": \"?1\"}}}]}}")
    List<TradeDocument> findLargeTradesBySymbol(String symbol, Double threshold);
    
    /**
     * Find trades by price range
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}, {\"range\": {\"price\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}]}}")
    List<TradeDocument> findBySymbolAndPriceBetween(String symbol, Double minPrice, Double maxPrice);
    
    /**
     * Find trades by volume range
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}, {\"range\": {\"quantity\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}]}}")
    List<TradeDocument> findBySymbolAndQuantityBetween(String symbol, Long minQuantity, Long maxQuantity);
    
    /**
     * Find trades with full-text search in metadata
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}, {\"match\": {\"metadata\": \"?1\"}}]}}")
    List<TradeDocument> findBySymbolAndMetadataContaining(String symbol, String searchText);
    
    /**
     * Find trades by multiple symbols
     */
    @Query("{\"terms\": {\"symbol\": ?0}}")
    List<TradeDocument> findBySymbolIn(List<String> symbols);
    
    /**
     * Find trades by tags
     */
    @Query("{\"terms\": {\"tags\": ?0}}")
    List<TradeDocument> findByTagsIn(List<String> tags);
    
    /**
     * Find high-risk trades
     */
    List<TradeDocument> findByIsHighRiskTrue();
    
    /**
     * Find trades by region
     */
    List<TradeDocument> findByRegion(String region);
    
    /**
     * Find trades by country
     */
    List<TradeDocument> findByCountry(String country);
    
    /**
     * Find trades by trade hour
     */
    List<TradeDocument> findByTradeHour(String tradeHour);
    
    /**
     * Find trades by price range category
     */
    List<TradeDocument> findByPriceRange(String priceRange);
    
    /**
     * Find trades by volume category
     */
    List<TradeDocument> findByVolumeCategory(String volumeCategory);
    
    /**
     * Find trades by user category
     */
    List<TradeDocument> findByUserCategory(String userCategory);
    
    /**
     * Find trades by symbol category
     */
    List<TradeDocument> findBySymbolCategory(String symbolCategory);
    
    /**
     * Find trades with pagination
     */
    Page<TradeDocument> findBySymbolOrderByExecutedAtDesc(String symbol, Pageable pageable);
    
    /**
     * Find trades by user with pagination
     */
    Page<TradeDocument> findByUserIdOrderByExecutedAtDesc(UUID userId, Pageable pageable);
    
    /**
     * Complex search query with multiple criteria
     */
    @Query("{\"bool\": {\"must\": [" +
           "{\"term\": {\"symbol\": \"?0\"}}," +
           "{\"range\": {\"executedAt\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}," +
           "{\"term\": {\"side\": \"?3\"}}," +
           "{\"range\": {\"totalValue\": {\"gte\": \"?4\"}}}" +
           "]}}")
    List<TradeDocument> findComplexQuery(String symbol, Instant startTime, Instant endTime, 
                                        String side, Double minValue);
    
    /**
     * Aggregation query for trade statistics
     */
    @Query("{\"size\": 0, \"aggs\": {\"symbols\": {\"terms\": {\"field\": \"symbol\", \"size\": 100}, \"aggs\": {\"total_value\": {\"sum\": {\"field\": \"totalValue\"}}, \"avg_price\": {\"avg\": {\"field\": \"price\"}}}}}}")
    Object getTradeStatistics();
    
    /**
     * Find recent trades for a symbol
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}], \"sort\": [{\"executedAt\": {\"order\": \"desc\"}}], \"size\": ?1}}")
    List<TradeDocument> findRecentTradesBySymbol(String symbol, int limit);
}
