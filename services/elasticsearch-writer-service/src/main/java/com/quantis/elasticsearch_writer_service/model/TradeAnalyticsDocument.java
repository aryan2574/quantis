package com.quantis.elasticsearch_writer_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Elasticsearch document for storing aggregated trade analytics.
 * 
 * This document stores pre-computed analytics and aggregations for efficient querying.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Document(indexName = "trade_analytics")
public class TradeAnalyticsDocument {
    
    /**
     * Unique analytics identifier (symbol + aggregation_type + time_period)
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String analyticsId;
    
    /**
     * Trading symbol
     */
    @Field(type = FieldType.Keyword)
    private String symbol;
    
    /**
     * Aggregation type (HOURLY, DAILY, WEEKLY, MONTHLY)
     */
    @Field(type = FieldType.Keyword)
    private String aggregationType;
    
    /**
     * Time period (YYYY-MM-DD-HH for hourly, YYYY-MM-DD for daily, etc.)
     */
    @Field(type = FieldType.Date)
    private String timePeriod;
    
    /**
     * Start timestamp of the aggregation period
     */
    @Field(type = FieldType.Date)
    private Instant periodStart;
    
    /**
     * End timestamp of the aggregation period
     */
    @Field(type = FieldType.Date)
    private Instant periodEnd;
    
    /**
     * Total number of trades in this period
     */
    @Field(type = FieldType.Long)
    private Long totalTrades;
    
    /**
     * Total volume traded
     */
    @Field(type = FieldType.Double)
    private Double totalVolume;
    
    /**
     * Total value traded
     */
    @Field(type = FieldType.Double)
    private Double totalValue;
    
    /**
     * Opening price
     */
    @Field(type = FieldType.Double)
    private Double openPrice;
    
    /**
     * Closing price
     */
    @Field(type = FieldType.Double)
    private Double closePrice;
    
    /**
     * Highest price during period
     */
    @Field(type = FieldType.Double)
    private Double highPrice;
    
    /**
     * Lowest price during period
     */
    @Field(type = FieldType.Double)
    private Double lowPrice;
    
    /**
     * Volume-weighted average price
     */
    @Field(type = FieldType.Double)
    private Double vwap;
    
    /**
     * Price change from previous period
     */
    @Field(type = FieldType.Double)
    private Double priceChange;
    
    /**
     * Price change percentage
     */
    @Field(type = FieldType.Double)
    private Double priceChangePercent;
    
    /**
     * Number of unique users who traded
     */
    @Field(type = FieldType.Long)
    private Long uniqueUsers;
    
    /**
     * Number of buy orders
     */
    @Field(type = FieldType.Long)
    private Long buyOrders;
    
    /**
     * Number of sell orders
     */
    @Field(type = FieldType.Long)
    private Long sellOrders;
    
    /**
     * Buy/Sell ratio
     */
    @Field(type = FieldType.Double)
    private Double buySellRatio;
    
    /**
     * Average trade size
     */
    @Field(type = FieldType.Double)
    private Double averageTradeSize;
    
    /**
     * Largest trade size
     */
    @Field(type = FieldType.Double)
    private Double largestTradeSize;
    
    /**
     * Standard deviation of trade sizes
     */
    @Field(type = FieldType.Double)
    private Double tradeSizeStdDev;
    
    /**
     * Price volatility (standard deviation of prices)
     */
    @Field(type = FieldType.Double)
    private Double priceVolatility;
    
    /**
     * Market depth (sum of all order quantities at best bid/ask)
     */
    @Field(type = FieldType.Double)
    private Double marketDepth;
    
    /**
     * Spread (difference between best bid and ask)
     */
    @Field(type = FieldType.Double)
    private Double spread;
    
    /**
     * Average spread during period
     */
    @Field(type = FieldType.Double)
    private Double averageSpread;
    
    /**
     * Trading intensity (trades per minute)
     */
    @Field(type = FieldType.Double)
    private Double tradingIntensity;
    
    /**
     * Volume intensity (volume per minute)
     */
    @Field(type = FieldType.Double)
    private Double volumeIntensity;
    
    /**
     * Market impact score
     */
    @Field(type = FieldType.Double)
    private Double marketImpactScore;
    
    /**
     * Liquidity score
     */
    @Field(type = FieldType.Double)
    private Double liquidityScore;
    
    /**
     * Last updated timestamp
     */
    @Field(type = FieldType.Date)
    private Instant lastUpdated;
    
    /**
     * Data quality score (0-1)
     */
    @Field(type = FieldType.Double)
    private Double dataQualityScore;
    
    /**
     * Additional computed metrics
     */
    @Field(type = FieldType.Object)
    private Object additionalMetrics;
}
