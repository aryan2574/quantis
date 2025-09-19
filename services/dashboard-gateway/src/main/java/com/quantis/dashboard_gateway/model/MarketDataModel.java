package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Market data models for GraphQL Gateway
 */
public class MarketDataModel {
    
    // Static builder method for convenience
    public static MarketDataBuilder builder() {
        return new MarketDataBuilder();
    }
    
    public static class MarketDataBuilder {
        private String symbol;
        private BigDecimal bestBid;
        private BigDecimal bestAsk;
        private BigDecimal lastPrice;
        private BigDecimal spread;
        private Long volume;
        private BigDecimal change;
        private BigDecimal changePercent;
        private Instant timestamp;
        private String status;
        private BigDecimal high24h;
        private BigDecimal low24h;
        private BigDecimal open24h;
        
        public MarketDataBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public MarketDataBuilder bestBid(BigDecimal bestBid) { this.bestBid = bestBid; return this; }
        public MarketDataBuilder bestAsk(BigDecimal bestAsk) { this.bestAsk = bestAsk; return this; }
        public MarketDataBuilder lastPrice(BigDecimal lastPrice) { this.lastPrice = lastPrice; return this; }
        public MarketDataBuilder spread(BigDecimal spread) { this.spread = spread; return this; }
        public MarketDataBuilder volume(Long volume) { this.volume = volume; return this; }
        public MarketDataBuilder change(BigDecimal change) { this.change = change; return this; }
        public MarketDataBuilder changePercent(BigDecimal changePercent) { this.changePercent = changePercent; return this; }
        public MarketDataBuilder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public MarketDataBuilder status(String status) { this.status = status; return this; }
        public MarketDataBuilder high24h(BigDecimal high24h) { this.high24h = high24h; return this; }
        public MarketDataBuilder low24h(BigDecimal low24h) { this.low24h = low24h; return this; }
        public MarketDataBuilder open24h(BigDecimal open24h) { this.open24h = open24h; return this; }
        
        public MarketData build() {
            return MarketData.builder()
                .symbol(symbol)
                .bestBid(bestBid)
                .bestAsk(bestAsk)
                .lastPrice(lastPrice)
                .spread(spread)
                .volume(volume)
                .change(change)
                .changePercent(changePercent)
                .timestamp(timestamp)
                .status(status)
                .high24h(high24h)
                .low24h(low24h)
                .open24h(open24h)
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketData {
        private String symbol;
        private BigDecimal bestBid;
        private BigDecimal bestAsk;
        private BigDecimal lastPrice;
        private BigDecimal spread;
        private Long volume;
        private BigDecimal change;
        private BigDecimal changePercent;
        private Instant timestamp;
        private String status;
        private BigDecimal high24h;
        private BigDecimal low24h;
        private BigDecimal open24h;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderBook {
        private String symbol;
        private List<PriceLevel> bids;
        private List<PriceLevel> asks;
        private Instant timestamp;
        private String status;
        private BigDecimal spread;
        private BigDecimal midPrice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceLevel {
        private BigDecimal price;
        private BigDecimal quantity;
        private Integer orderCount;
        private BigDecimal totalValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Trade {
        private String tradeId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private Instant timestamp;
        private BigDecimal totalValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoricalData {
        private Instant timestamp;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;
        private BigDecimal vwap;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MarketSummary {
        private String symbol;
        private BigDecimal lastPrice;
        private BigDecimal change;
        private BigDecimal changePercent;
        private Long volume;
        private BigDecimal high24h;
        private BigDecimal low24h;
        private BigDecimal marketCap;
    }
}
