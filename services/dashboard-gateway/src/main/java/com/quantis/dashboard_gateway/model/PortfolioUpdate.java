package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Portfolio Update Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUpdate {
    private String userId;
    private BigDecimal totalValue;
    private BigDecimal totalPnL;
    private BigDecimal dailyPnL;
    private LocalDateTime timestamp;
}
