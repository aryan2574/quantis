package com.quantis.portfolio_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a user's trading account with cash balance and metadata.
 */
@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    
    @Id
    @Column(name = "user_id")
    private UUID userId;
    
    @Column(name = "cash_balance", nullable = false, precision = 20, scale = 8)
    private BigDecimal cashBalance;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "account_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AccountStatus status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
    
    public enum AccountStatus {
        ACTIVE,
        SUSPENDED,
        CLOSED
    }
}
