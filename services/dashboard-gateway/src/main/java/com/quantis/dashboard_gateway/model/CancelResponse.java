package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cancel Response Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CancelResponse {
    private boolean success;
    private String message;
}
