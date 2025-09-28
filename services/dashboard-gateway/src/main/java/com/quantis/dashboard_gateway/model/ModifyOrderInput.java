package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Modify Order Input Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyOrderInput {
    private String orderId;
    private BigDecimal quantity;
    private BigDecimal price;
}
