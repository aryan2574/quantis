package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Risk Metrics Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskMetrics {
    private String userId;
    private BigDecimal var;
    private BigDecimal expectedShortfall;
    private BigDecimal maxDrawdown;
    private BigDecimal portfolioRisk;
    private BigDecimal marketRisk;
    private LocalDateTime lastUpdate;
}
