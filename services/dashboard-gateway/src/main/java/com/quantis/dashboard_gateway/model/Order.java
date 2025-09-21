package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private String symbol;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private String orderType;
    private String status;
    private LocalDateTime timestamp;
    private BigDecimal filledQuantity;
    private BigDecimal averagePrice;
}
