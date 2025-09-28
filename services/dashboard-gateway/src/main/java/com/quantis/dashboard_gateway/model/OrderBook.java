package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order Book Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderBook {
    private String symbol;
    private List<OrderLevel> bids;
    private List<OrderLevel> asks;
    private LocalDateTime lastUpdate;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderLevel {
        private BigDecimal price;
        private BigDecimal quantity;
        private int orders;
    }
}
