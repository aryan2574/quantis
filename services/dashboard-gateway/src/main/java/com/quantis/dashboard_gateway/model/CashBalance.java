package com.quantis.dashboard_gateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cash Balance Model
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashBalance {
    private String userId;
    private BigDecimal availableBalance;
    private BigDecimal pendingBalance;
    private BigDecimal totalBalance;
    private String currency;
    private LocalDateTime lastUpdate;
}
