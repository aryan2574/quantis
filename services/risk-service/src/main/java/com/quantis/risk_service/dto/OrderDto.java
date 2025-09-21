package com.quantis.risk_service.dto;

import lombok.Data;

@Data
public class OrderDto {
    private String orderId;
    private String userId;
    private String symbol;
    private String side; // BUY|SELL
    private long quantity;
    private double price;
    
    // Manual getters in case Lombok isn't working
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getSymbol() { return symbol; }
    public String getSide() { return side; }
    public long getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
