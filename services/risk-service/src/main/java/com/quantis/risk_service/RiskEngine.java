package com.quantis.risk_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.risk_service.dto.OrderDto;
import com.quantis.risk_service.PortfolioClient;
import com.quantis.risk_service.repository.BlacklistRepository;
import com.quantis.risk_service.repository.UserRiskLimitsRepository;
import com.quantis.risk_service.OrderProducer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Service
public class RiskEngine {

    private final BlacklistRepository blacklistRepo;
    private final UserRiskLimitsRepository limitsRepo;
    private final StringRedisTemplate redis;
    private final PortfolioClient portfolioClient;
    private final OrderProducer producer;
    private final ObjectMapper om = new ObjectMapper();

    public RiskEngine(BlacklistRepository blacklistRepo,
                      UserRiskLimitsRepository limitsRepo,
                      StringRedisTemplate redis,
                      PortfolioClient portfolioClient,
                      OrderProducer producer) {
        this.blacklistRepo = blacklistRepo;
        this.limitsRepo = limitsRepo;
        this.redis = redis;
        this.portfolioClient = portfolioClient;
        this.producer = producer;
    }

    public void process(OrderDto order) {
        try {
            String reason = runChecks(order);
            var json = om.writeValueAsString(order);
            if (reason == null) {
                producer.publishValid(order.getOrderId(), json);
            } else {
                // augment rejection with reason
                var rej = String.format("{\"orderId\":\"%s\",\"reason\":\"%s\"}", order.getOrderId(), reason);
                producer.publishRejected(order.getOrderId(), rej);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // on unexpected error, reject for safety
            var rej = String.format("{\"orderId\":\"%s\",\"reason\":\"internal_error\"}", order.getOrderId());
            producer.publishRejected(order.getOrderId(), rej);
        }
    }

    private String runChecks(OrderDto order) {
        // 0. Validate userId format (must be valid UUID)
        java.util.UUID userId;
        try {
            userId = java.util.UUID.fromString(order.getUserId());
        } catch (IllegalArgumentException e) {
            return "invalid_user_id_format";
        }

        // 1. Blacklist (Redis cache)
        String blKey = "blacklist:user:" + order.getUserId();
        if (Boolean.TRUE.equals(redis.hasKey(blKey) ? Boolean.valueOf(redis.opsForValue().get(blKey)) : null)) {
            return "user_blacklisted";
        }
        // fallback to DB
        if (blacklistRepo.existsById(userId)) {
            // cache in redis
            redis.opsForValue().set(blKey, "true", Duration.ofHours(1));
            return "user_blacklisted";
        }

        // 2. Price sanity - check last trade price from Redis
        String lastTradeKey = "last_trade:" + order.getSymbol();
        String last = redis.opsForValue().get(lastTradeKey);
        if (last != null) {
            try {
                double lastPrice = Double.parseDouble(last);
                if (Math.abs(order.getPrice() - lastPrice) / lastPrice > 0.10) {
                    return "price_deviation_too_large";
                }
            } catch (NumberFormatException ignored) { }
        }

        // 3. Quantity limits
        if (order.getQuantity() <= 0 || order.getQuantity() > 1_000_000L) {
            return "invalid_quantity";
        }

        // 4. Cash balance check (use portfolio client)
        double required = order.getPrice() * order.getQuantity();
        double balance = portfolioClient.getCashBalance(order.getUserId()); // stubbed
        if ("BUY".equalsIgnoreCase(order.getSide()) && balance < required) {
            return "insufficient_funds";
        }

        // 5. Position limit check - current position + new value
        double currentPos = portfolioClient.getPositionValue(order.getUserId(), order.getSymbol());
        Optional<com.quantis.risk_service.jpa.UserRiskLimits> limits = limitsRepo.findById(userId);
        BigDecimal userMax = limits.map(l -> l.getMaxPositionValue()).orElse(BigDecimal.valueOf(100_000));
        double newPos = currentPos + required;
        if (BigDecimal.valueOf(newPos).compareTo(userMax) > 0) {
            return "position_limit_exceeded";
        }

        // 6. Daily loss check - query Redis
        String pnlKey = "pnl:daily:" + order.getUserId();
        String pnlStr = redis.opsForValue().get(pnlKey);
        double pnl = pnlStr == null ? 0.0 : Double.parseDouble(pnlStr);
        double dailyLimit = limits.map(l -> l.getDailyLossLimit().doubleValue()).orElse(5000.0);
        if (pnl <= -dailyLimit) {
            return "daily_loss_limit_exceeded";
        }

        return null; // passed all checks
    }
}
