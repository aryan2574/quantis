package com.quantis.risk_service.client;

/**
 * Simple data class for position information
 */
public class PositionInfo {
    private final String symbol;
    private final double quantity;
    private final double marketValue;
    private final double averagePrice;
    
    public PositionInfo(String symbol, double quantity, double marketValue, double averagePrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.marketValue = marketValue;
        this.averagePrice = averagePrice;
    }
    
    public String getSymbol() { return symbol; }
    public double getQuantity() { return quantity; }
    public double getMarketValue() { return marketValue; }
    public double getAveragePrice() { return averagePrice; }
}
