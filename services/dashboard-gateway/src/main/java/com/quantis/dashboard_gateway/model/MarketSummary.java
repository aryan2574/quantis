package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Market Summary Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketSummary {
    private String symbol;
    private BigDecimal lastPrice;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal volume;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal open;
    private LocalDateTime lastUpdate;
}
