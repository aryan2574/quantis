package com.quantis.risk_service.client;

/**
 * Simple data class for portfolio value information
 */
public class PortfolioValue {
    private final double cashBalance;
    private final double totalValue;
    
    public PortfolioValue(double cashBalance, double totalValue) {
        this.cashBalance = cashBalance;
        this.totalValue = totalValue;
    }
    
    public double getCashBalance() { return cashBalance; }
    public double getTotalValue() { return totalValue; }
}
