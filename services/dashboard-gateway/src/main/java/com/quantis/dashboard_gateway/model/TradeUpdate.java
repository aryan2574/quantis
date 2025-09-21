package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trade Update Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeUpdate {
    private String userId;
    private String tradeId;
    private String symbol;
    private BigDecimal price;
    private BigDecimal quantity;
    private String side;
    private LocalDateTime timestamp;
}
