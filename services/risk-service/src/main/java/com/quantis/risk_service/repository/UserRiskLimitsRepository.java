package com.quantis.risk_service.repository;

import com.quantis.risk_service.jpa.UserRiskLimits;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRiskLimitsRepository extends JpaRepository<UserRiskLimits, UUID> {
}
