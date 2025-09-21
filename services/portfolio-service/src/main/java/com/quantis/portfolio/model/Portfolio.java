package com.quantis.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;

/**
 * Portfolio Model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {
    private String userId;
    private BigDecimal totalValue;
    private BigDecimal cashBalance;
    private BigDecimal positionsValue;
    private BigDecimal unrealizedPnl;
    private BigDecimal realizedPnl;
    private BigDecimal totalPnL;
    private String currency;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private Instant lastUpdatedInstant;
    private List<Position> positions;
}
