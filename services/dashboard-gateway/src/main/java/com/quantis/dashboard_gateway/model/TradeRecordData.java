package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Trade record data models for GraphQL Gateway
 */
public class TradeRecordData {
    
    // Static builder method for convenience
    public static TradeRecordBuilder builder() {
        return new TradeRecordBuilder();
    }
    
    public static class TradeRecordBuilder {
        private String tradeId;
        private String orderId;
        private String userId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal totalValue;
        private Instant executedAt;
        private String status;
        private BigDecimal commission;
        private String counterpartyOrderId;
        
        public TradeRecordBuilder tradeId(String tradeId) { this.tradeId = tradeId; return this; }
        public TradeRecordBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public TradeRecordBuilder userId(String userId) { this.userId = userId; return this; }
        public TradeRecordBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public TradeRecordBuilder side(String side) { this.side = side; return this; }
        public TradeRecordBuilder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
        public TradeRecordBuilder price(BigDecimal price) { this.price = price; return this; }
        public TradeRecordBuilder totalValue(BigDecimal totalValue) { this.totalValue = totalValue; return this; }
        public TradeRecordBuilder executedAt(Instant executedAt) { this.executedAt = executedAt; return this; }
        public TradeRecordBuilder status(String status) { this.status = status; return this; }
        public TradeRecordBuilder commission(BigDecimal commission) { this.commission = commission; return this; }
        public TradeRecordBuilder counterpartyOrderId(String counterpartyOrderId) { this.counterpartyOrderId = counterpartyOrderId; return this; }
        
        public TradeRecord build() {
            return TradeRecord.builder()
                .tradeId(tradeId)
                .orderId(orderId)
                .userId(userId)
                .symbol(symbol)
                .side(side)
                .quantity(quantity)
                .price(price)
                .totalValue(totalValue)
                .executedAt(executedAt)
                .status(status)
                .commission(commission)
                .counterpartyOrderId(counterpartyOrderId)
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeRecord {
        private String tradeId;
        private String orderId;
        private String userId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal totalValue;
        private Instant executedAt;
        private String status;
        private BigDecimal commission;
        private String counterpartyOrderId;
    }
}
