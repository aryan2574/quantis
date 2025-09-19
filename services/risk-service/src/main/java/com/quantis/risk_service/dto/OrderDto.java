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
}
