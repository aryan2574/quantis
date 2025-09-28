package com.quantis.portfolio.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Asset Allocation Model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetAllocation {
    private String assetType;
    private BigDecimal percentage;
    private BigDecimal value;
    private BigDecimal targetPercentage;
    private BigDecimal deviation;
}
