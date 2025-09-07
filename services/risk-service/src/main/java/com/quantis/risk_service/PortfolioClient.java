package com.quantis.risk_service;

import org.springframework.stereotype.Component;

@Component
public class PortfolioClient {
    // temporary in-memory mock for early testing
    public double getCashBalance(String userId) {
        return 100_000.0; // give default for dev
    }
    public double getPositionValue(String userId, String symbol) {
        return 0.0;
    }
}
