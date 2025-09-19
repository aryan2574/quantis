package com.quantis.risk_service.repository;

import com.quantis.risk_service.jpa.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BlacklistRepository extends JpaRepository<Blacklist, UUID> {
}
