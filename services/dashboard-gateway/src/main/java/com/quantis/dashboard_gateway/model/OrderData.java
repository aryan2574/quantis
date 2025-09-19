package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Order data models for GraphQL Gateway
 */
public class OrderData {
    
    // Static builder method for convenience
    public static OrderBuilder builder() {
        return new OrderBuilder();
    }
    
    public static class OrderBuilder {
        private String orderId;
        private String userId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private String orderType;
        private String timeInForce;
        private String status;
        private BigDecimal filledQuantity;
        private BigDecimal averagePrice;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant executedAt;
        private BigDecimal commission;
        private String metadata;
        
        public OrderBuilder orderId(String orderId) { this.orderId = orderId; return this; }
        public OrderBuilder userId(String userId) { this.userId = userId; return this; }
        public OrderBuilder symbol(String symbol) { this.symbol = symbol; return this; }
        public OrderBuilder side(String side) { this.side = side; return this; }
        public OrderBuilder quantity(BigDecimal quantity) { this.quantity = quantity; return this; }
        public OrderBuilder price(BigDecimal price) { this.price = price; return this; }
        public OrderBuilder orderType(String orderType) { this.orderType = orderType; return this; }
        public OrderBuilder timeInForce(String timeInForce) { this.timeInForce = timeInForce; return this; }
        public OrderBuilder status(String status) { this.status = status; return this; }
        public OrderBuilder filledQuantity(BigDecimal filledQuantity) { this.filledQuantity = filledQuantity; return this; }
        public OrderBuilder averagePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; return this; }
        public OrderBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public OrderBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public OrderBuilder executedAt(Instant executedAt) { this.executedAt = executedAt; return this; }
        public OrderBuilder commission(BigDecimal commission) { this.commission = commission; return this; }
        public OrderBuilder metadata(String metadata) { this.metadata = metadata; return this; }
        
        public Order build() {
            return Order.builder()
                .orderId(orderId)
                .userId(userId)
                .symbol(symbol)
                .side(side)
                .quantity(quantity)
                .price(price)
                .orderType(orderType)
                .timeInForce(timeInForce)
                .status(status)
                .filledQuantity(filledQuantity)
                .averagePrice(averagePrice)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .executedAt(executedAt)
                .commission(commission)
                .metadata(metadata)
                .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {
        private String orderId;
        private String userId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private String orderType;
        private String timeInForce;
        private String status;
        private BigDecimal filledQuantity;
        private BigDecimal averagePrice;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant executedAt;
        private BigDecimal commission;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaceOrderInput {
        private String userId;
        private String symbol;
        private String side;
        private BigDecimal quantity;
        private BigDecimal price;
        private String orderType;
        private String timeInForce;
        private BigDecimal stopPrice;
        private String metadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyOrderInput {
        private String orderId;
        private String userId;
        private BigDecimal quantity;
        private BigDecimal price;
        private BigDecimal stopPrice;
        private String timeInForce;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private Boolean success;
        private String orderId;
        private String message;
        private Order order;
        private java.util.List<String> errors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelResponse {
        private Boolean success;
        private String message;
        private String orderId;
        private java.util.List<String> errors;
    }
}
