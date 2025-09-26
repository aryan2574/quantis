package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trade Analytics Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeAnalytics {
    private String userId;
    private int totalTrades;
    private BigDecimal winRate;
    private BigDecimal averageWin;
    private BigDecimal averageLoss;
    private BigDecimal profitFactor;
    private LocalDateTime lastUpdate;
}
