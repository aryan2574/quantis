package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Order Update Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdate {
    private String userId;
    private String orderId;
    private String symbol;
    private String status;
    private BigDecimal filledQuantity;
    private BigDecimal averagePrice;
    private LocalDateTime timestamp;
}
