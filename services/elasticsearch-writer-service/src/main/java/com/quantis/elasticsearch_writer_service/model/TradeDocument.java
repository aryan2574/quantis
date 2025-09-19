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
import java.util.UUID;

/**
 * Elasticsearch document for storing searchable trade data.
 * 
 * This document is optimized for full-text search, analytics, and complex queries.
 * It includes all trade information with proper field mapping for Elasticsearch.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "trades")
public class TradeDocument {
    
    /**
     * Unique trade identifier
     */
    @Id
    @Field(type = FieldType.Keyword)
    private String tradeId;
    
    /**
     * Order ID that generated this trade
     */
    @Field(type = FieldType.Keyword)
    private String orderId;
    
    /**
     * User ID who executed the trade
     */
    @Field(type = FieldType.Keyword)
    private UUID userId;
    
    /**
     * Trading symbol (searchable)
     */
    @Field(type = FieldType.Keyword)
    private String symbol;
    
    /**
     * Trade side (BUY/SELL)
     */
    @Field(type = FieldType.Keyword)
    private String side;
    
    /**
     * Quantity traded
     */
    @Field(type = FieldType.Long)
    private Long quantity;
    
    /**
     * Price per unit
     */
    @Field(type = FieldType.Double)
    private Double price;
    
    /**
     * Total trade value
     */
    @Field(type = FieldType.Double)
    private Double totalValue;
    
    /**
     * Trade execution timestamp
     */
    @Field(type = FieldType.Date)
    private Instant executedAt;
    
    /**
     * Trade status
     */
    @Field(type = FieldType.Keyword)
    private String status;
    
    /**
     * Counterparty order ID
     */
    @Field(type = FieldType.Keyword)
    private String counterpartyOrderId;
    
    /**
     * Market session identifier
     */
    @Field(type = FieldType.Keyword)
    private String marketSession;
    
    /**
     * Additional metadata (searchable text)
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String metadata;
    
    /**
     * Processing timestamp when this document was indexed
     */
    @Field(type = FieldType.Date)
    private Instant processedAt;
    
    /**
     * Sequence number for ordering
     */
    @Field(type = FieldType.Long)
    private Long sequenceNumber;
    
    /**
     * Trade event type
     */
    @Field(type = FieldType.Keyword)
    private String eventType;
    
    /**
     * Market data snapshot at time of trade (JSON string)
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String marketDataSnapshot;
    
    /**
     * Raw trade data as received from Kafka
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String rawData;
    
    /**
     * Trade date for time-based queries (YYYY-MM-DD)
     */
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd")
    private String tradeDate;
    
    /**
     * Trade hour for hourly aggregations (YYYY-MM-DD-HH)
     */
    @Field(type = FieldType.Keyword)
    private String tradeHour;
    
    /**
     * Price range category for analytics
     */
    @Field(type = FieldType.Keyword)
    private String priceRange;
    
    /**
     * Volume category for analytics
     */
    @Field(type = FieldType.Keyword)
    private String volumeCategory;
    
    /**
     * User category for analytics
     */
    @Field(type = FieldType.Keyword)
    private String userCategory;
    
    /**
     * Symbol category for analytics
     */
    @Field(type = FieldType.Keyword)
    private String symbolCategory;
    
    /**
     * Calculated fields for analytics
     */
    @Field(type = FieldType.Double)
    private Double priceChange;
    
    @Field(type = FieldType.Double)
    private Double volumeWeightedPrice;
    
    @Field(type = FieldType.Boolean)
    private Boolean isLargeTrade;
    
    @Field(type = FieldType.Boolean)
    private Boolean isOffHoursTrade;
    
    /**
     * Tags for flexible querying
     */
    @Field(type = FieldType.Keyword)
    private String[] tags;
    
    /**
     * Geographic information (if available)
     */
    @Field(type = FieldType.Keyword)
    private String region;
    
    @Field(type = FieldType.Keyword)
    private String country;
    
    /**
     * Risk indicators
     */
    @Field(type = FieldType.Boolean)
    private Boolean isHighRisk;
    
    @Field(type = FieldType.Double)
    private Double riskScore;
    
    /**
     * Performance metrics
     */
    @Field(type = FieldType.Long)
    private Long processingLatencyMs;
    
    @Field(type = FieldType.Keyword)
    private String processingStatus;
}
