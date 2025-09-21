package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Position Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal unrealizedPnL;
    private BigDecimal realizedPnL;
    private LocalDateTime lastUpdate;
}
