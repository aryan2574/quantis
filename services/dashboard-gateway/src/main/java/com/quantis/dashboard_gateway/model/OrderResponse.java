package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Order Response Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private boolean success;
    private String orderId;
    private String message;
}
