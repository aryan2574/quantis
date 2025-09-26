package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trade Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {
    private String tradeId;
    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private String side;
    private LocalDateTime timestamp;
    private String orderId;
}
