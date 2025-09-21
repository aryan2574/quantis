package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Dashboard Update Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardUpdate {
    private String userId;
    private String updateType;
    private Object data;
    private LocalDateTime timestamp;
}
