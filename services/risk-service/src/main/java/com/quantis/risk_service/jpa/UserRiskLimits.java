package com.quantis.risk_service.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "user_risk_limits")
public class UserRiskLimits {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "max_position_value")
    private BigDecimal maxPositionValue = BigDecimal.valueOf(100_000);

    @Column(name = "daily_loss_limit")
    private BigDecimal dailyLossLimit = BigDecimal.valueOf(5_000);

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BigDecimal getMaxPositionValue() {
        return maxPositionValue;
    }

    public void setMaxPositionValue(BigDecimal maxPositionValue) {
        this.maxPositionValue = maxPositionValue;
    }

    public BigDecimal getDailyLossLimit() {
        return dailyLossLimit;
    }

    public void setDailyLossLimit(BigDecimal dailyLossLimit) {
        this.dailyLossLimit = dailyLossLimit;
    }
}
