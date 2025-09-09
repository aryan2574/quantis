package com.quantis.portfolio_service.repository;

import com.quantis.portfolio_service.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for managing Account entities.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    /**
     * Check if account exists for user
     */
    boolean existsByUserId(UUID userId);
}
