package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Dashboard Overview Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardOverview {
    private String userId;
    private BigDecimal totalValue;
    private BigDecimal totalPnL;
    private BigDecimal dailyPnL;
    private BigDecimal cashBalance;
    private int activePositions;
    private int pendingOrders;
    private LocalDateTime lastUpdate;
}
