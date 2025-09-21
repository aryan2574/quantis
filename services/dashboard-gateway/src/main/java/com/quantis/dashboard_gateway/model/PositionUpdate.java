package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Position Update Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PositionUpdate {
    private String userId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private BigDecimal unrealizedPnL;
    private LocalDateTime timestamp;
}
