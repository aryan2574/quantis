package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Performance Metrics Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    private String userId;
    private BigDecimal totalReturn;
    private BigDecimal sharpeRatio;
    private BigDecimal volatility;
    private BigDecimal maxDrawdown;
    private BigDecimal winRate;
    private BigDecimal profitFactor;
    private LocalDateTime lastUpdate;
}
