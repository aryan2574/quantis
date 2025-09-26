package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Place Order Input Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderInput {
    private String userId;
    private String symbol;
    private String side;
    private BigDecimal quantity;
    private BigDecimal price;
    private String orderType;
}
