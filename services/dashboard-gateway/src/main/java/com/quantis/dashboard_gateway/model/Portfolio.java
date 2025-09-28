package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Portfolio Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    private String userId;
    private BigDecimal totalValue;
    private BigDecimal totalPnL;
    private BigDecimal dailyPnL;
    private BigDecimal cashBalance;
    private List<Position> positions;
    private LocalDateTime lastUpdate;
}
