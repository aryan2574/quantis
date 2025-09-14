package com.quantis.elasticsearch_writer_service.repository;

import com.quantis.elasticsearch_writer_service.model.TradeAnalyticsDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for accessing trade analytics documents in Elasticsearch.
 * 
 * This repository provides queries for aggregated trade statistics and analytics.
 */
@Repository
public interface TradeAnalyticsRepository extends ElasticsearchRepository<TradeAnalyticsDocument, String> {
    
    /**
     * Find analytics by symbol
     */
    List<TradeAnalyticsDocument> findBySymbol(String symbol);
    
    /**
     * Find analytics by symbol and aggregation type
     */
    List<TradeAnalyticsDocument> findBySymbolAndAggregationType(String symbol, String aggregationType);
    
    /**
     * Find analytics by symbol and time period range
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}, {\"range\": {\"timePeriod\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}]}}")
    List<TradeAnalyticsDocument> findBySymbolAndTimePeriodBetween(String symbol, String startPeriod, String endPeriod);
    
    /**
     * Find analytics by aggregation type and time period
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"aggregationType\": \"?0\"}}, {\"range\": {\"timePeriod\": {\"gte\": \"?1\", \"lte\": \"?2\"}}}]}}")
    List<TradeAnalyticsDocument> findByAggregationTypeAndTimePeriodBetween(String aggregationType, String startPeriod, String endPeriod);
    
    /**
     * Find latest analytics for a symbol
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}], \"sort\": [{\"timePeriod\": {\"order\": \"desc\"}}], \"size\": ?1}}")
    List<TradeAnalyticsDocument> findLatestBySymbol(String symbol, int limit);
    
    /**
     * Find analytics by symbol and specific time period
     */
    TradeAnalyticsDocument findBySymbolAndTimePeriod(String symbol, String timePeriod);
    
    /**
     * Find top symbols by total value for a specific time period
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"timePeriod\": \"?0\"}}], \"sort\": [{\"totalValue\": {\"order\": \"desc\"}}], \"size\": ?1}}")
    List<TradeAnalyticsDocument> findTopByTotalValue(String timePeriod, int limit);
    
    /**
     * Find top symbols by total volume for a specific time period
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"timePeriod\": \"?0\"}}], \"sort\": [{\"totalVolume\": {\"order\": \"desc\"}}], \"size\": ?1}}")
    List<TradeAnalyticsDocument> findTopByTotalVolume(String timePeriod, int limit);
    
    /**
     * Find top symbols by trade count for a specific time period
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"timePeriod\": \"?0\"}}], \"sort\": [{\"totalTrades\": {\"order\": \"desc\"}}], \"size\": ?1}}")
    List<TradeAnalyticsDocument> findTopByTotalTrades(String timePeriod, int limit);
    
    /**
     * Find analytics with high volatility
     */
    @Query("{\"range\": {\"priceVolatility\": {\"gte\": \"?0\"}}}")
    List<TradeAnalyticsDocument> findByHighVolatility(Double volatilityThreshold);
    
    /**
     * Find analytics with high trading intensity
     */
    @Query("{\"range\": {\"tradingIntensity\": {\"gte\": \"?0\"}}}")
    List<TradeAnalyticsDocument> findByHighTradingIntensity(Double intensityThreshold);
    
    /**
     * Find analytics with high liquidity
     */
    @Query("{\"range\": {\"liquidityScore\": {\"gte\": \"?0\"}}}")
    List<TradeAnalyticsDocument> findByHighLiquidity(Double liquidityThreshold);
    
    /**
     * Find analytics by multiple symbols
     */
    @Query("{\"terms\": {\"symbol\": ?0}}")
    List<TradeAnalyticsDocument> findBySymbolIn(List<String> symbols);
    
    /**
     * Find analytics by aggregation type
     */
    List<TradeAnalyticsDocument> findByAggregationType(String aggregationType);
    
    /**
     * Find analytics with pagination
     */
    Page<TradeAnalyticsDocument> findBySymbolOrderByTimePeriodDesc(String symbol, Pageable pageable);
    
    /**
     * Find analytics by symbol and aggregation type with pagination
     */
    Page<TradeAnalyticsDocument> findBySymbolAndAggregationTypeOrderByTimePeriodDesc(
        String symbol, String aggregationType, Pageable pageable);
    
    /**
     * Complex analytics query with multiple criteria
     */
    @Query("{\"bool\": {\"must\": [" +
           "{\"term\": {\"symbol\": \"?0\"}}," +
           "{\"term\": {\"aggregationType\": \"?1\"}}," +
           "{\"range\": {\"totalValue\": {\"gte\": \"?2\"}}}," +
           "{\"range\": {\"priceVolatility\": {\"gte\": \"?3\"}}}" +
           "]}}")
    List<TradeAnalyticsDocument> findComplexAnalytics(String symbol, String aggregationType, 
                                                      Double minValue, Double minVolatility);
    
    /**
     * Get market overview for a specific time period
     */
    @Query("{\"size\": 0, \"aggs\": {" +
           "\"symbols\": {\"terms\": {\"field\": \"symbol\", \"size\": 100}, \"aggs\": {" +
           "\"total_value\": {\"sum\": {\"field\": \"totalValue\"}}," +
           "\"total_volume\": {\"sum\": {\"field\": \"totalVolume\"}}," +
           "\"total_trades\": {\"sum\": {\"field\": \"totalTrades\"}}," +
           "\"avg_volatility\": {\"avg\": {\"field\": \"priceVolatility\"}}," +
           "\"avg_liquidity\": {\"avg\": {\"field\": \"liquidityScore\"}}" +
           "}}}}")
    Object getMarketOverview(String timePeriod);
    
    /**
     * Get performance metrics for a symbol over time
     */
    @Query("{\"bool\": {\"must\": [{\"term\": {\"symbol\": \"?0\"}}, {\"term\": {\"aggregationType\": \"?1\"}}], \"sort\": [{\"timePeriod\": {\"order\": \"asc\"}}]}}")
    List<TradeAnalyticsDocument> getPerformanceMetrics(String symbol, String aggregationType);
    
    /**
     * Find analytics with data quality issues
     */
    @Query("{\"range\": {\"dataQualityScore\": {\"lt\": \"?0\"}}}")
    List<TradeAnalyticsDocument> findByLowDataQuality(Double qualityThreshold);
    
    /**
     * Find analytics by market impact score
     */
    @Query("{\"range\": {\"marketImpactScore\": {\"gte\": \"?0\"}}}")
    List<TradeAnalyticsDocument> findByHighMarketImpact(Double impactThreshold);
}
