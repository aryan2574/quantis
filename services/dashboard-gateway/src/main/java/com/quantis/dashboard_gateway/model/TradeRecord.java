package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trade Record Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeRecord {
    private String tradeId;
    private String symbol;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalValue;
    private LocalDateTime timestamp;
    private String orderId;
}
