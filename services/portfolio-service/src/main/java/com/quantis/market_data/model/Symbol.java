package com.quantis.market_data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Symbol Model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Symbol {
    private String symbol;
    private String baseSymbol;
    private String name;
    private String exchange;
    private AssetType assetType;
    private boolean isActive;
    private Double lastPrice;
    private String lastUpdate;
}
