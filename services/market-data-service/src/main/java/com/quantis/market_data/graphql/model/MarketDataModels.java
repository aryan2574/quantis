package com.quantis.market_data.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * GraphQL data models for market data service
 */
public class MarketDataModels {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketData {
        private String symbol;
        private double bestBid;
        private double bestAsk;
        private double lastPrice;
        private double spread;
        private long volume;
        private double change;
        private double changePercent;
        private long timestamp;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBook {
        private String symbol;
        private List<PriceLevel> bids;
        private List<PriceLevel> asks;
        private long timestamp;
        private String status;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceLevel {
        private double price;
        private long quantity;
        private int orderCount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trade {
        private String tradeId;
        private String symbol;
        private String side;
        private long quantity;
        private double price;
        private long timestamp;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalData {
        private long timestamp;
        private double open;
        private double high;
        private double low;
        private double close;
        private long volume;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketSummary {
        private String symbol;
        private double lastPrice;
        private double change;
        private double changePercent;
        private long volume;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketDataUpdate {
        private List<MarketData> symbols;
        private long timestamp;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeUpdate {
        private List<Trade> trades;
        private long timestamp;
    }
}
