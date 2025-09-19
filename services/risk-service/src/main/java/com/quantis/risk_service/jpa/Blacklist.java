package com.quantis.risk_service.jpa;

import jakarta.persistence.*;
import java.util.Objects;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "blacklist")
public class Blacklist {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String reason;

    @Column(name = "blacklisted_at")
    private Instant blacklistedAt = Instant.now();

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(Instant blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    @Override
    public String toString() {
        return "Blacklist{" +
                "userId=" + userId +
                ", reason='" + reason + '\'' +
                ", blacklistedAt=" + blacklistedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blacklist blacklist = (Blacklist) o;
        return Objects.equals(userId, blacklist.userId) && Objects.equals(reason, blacklist.reason) && Objects.equals(blacklistedAt, blacklist.blacklistedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, reason, blacklistedAt);
    }
}
