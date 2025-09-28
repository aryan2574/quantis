package com.quantis.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.quantis.market_data.model.AssetType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Position Model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private String userId;
    private String symbol;
    private BigDecimal quantity;
    private BigDecimal averagePrice;
    private BigDecimal currentPrice;
    private BigDecimal marketValue;
    private BigDecimal unrealizedPnl;
    private BigDecimal realizedPnl;
    private LocalDateTime lastUpdated;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private BigDecimal change;
    private BigDecimal changePercent;
    private AssetType assetType;
}
