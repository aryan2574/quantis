package com.quantis.risk_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.risk_service.dto.OrderDto;
import com.quantis.risk_service.client.PortfolioClient;
import com.quantis.risk_service.client.PortfolioValue;
import com.quantis.risk_service.client.PositionInfo;
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

        // 1. Blacklist check (Redis cache + DB fallback)
        String blKey = "blacklist:user:" + order.getUserId();
        if (Boolean.TRUE.equals(redis.hasKey(blKey) ? Boolean.valueOf(redis.opsForValue().get(blKey)) : null)) {
            return "user_blacklisted";
        }
        if (blacklistRepo.existsById(userId)) {
            redis.opsForValue().set(blKey, "true", Duration.ofHours(1));
            return "user_blacklisted";
        }

        // 2. Basic order validation
        if (order.getQuantity() <= 0 || order.getQuantity() > 1_000_000L) {
            return "invalid_quantity";
        }
        if (order.getPrice() <= 0 || order.getPrice() > 1_000_000.0) {
            return "invalid_price";
        }
        if (!"BUY".equalsIgnoreCase(order.getSide()) && !"SELL".equalsIgnoreCase(order.getSide())) {
            return "invalid_side";
        }

        // 3. Price sanity check - deviation from last trade price
        String lastTradeKey = "last_trade:" + order.getSymbol();
        String last = redis.opsForValue().get(lastTradeKey);
        if (last != null) {
            try {
                double lastPrice = Double.parseDouble(last);
                double deviation = Math.abs(order.getPrice() - lastPrice) / lastPrice;
                if (deviation > 0.10) { // 10% deviation threshold
                    return "price_deviation_too_large";
                }
            } catch (NumberFormatException ignored) { }
        }

        // 4. Get portfolio data via gRPC
        PortfolioValue portfolio = portfolioClient.getPortfolioValue(order.getUserId());
        PositionInfo position = portfolioClient.getPosition(order.getUserId(), order.getSymbol());
        
        // 5. Cash balance check for BUY orders
        double orderValue = order.getPrice() * order.getQuantity();
        if ("BUY".equalsIgnoreCase(order.getSide()) && portfolio.getCashBalance() < orderValue) {
            return "insufficient_funds";
        }

        // 6. Position limit check
        Optional<com.quantis.risk_service.jpa.UserRiskLimits> limits = limitsRepo.findById(userId);
        BigDecimal userMaxPosition = limits.map(l -> l.getMaxPositionValue()).orElse(BigDecimal.valueOf(100_000));
        
        double newPositionValue;
        if ("BUY".equalsIgnoreCase(order.getSide())) {
            newPositionValue = position.getMarketValue() + orderValue;
        } else {
            newPositionValue = Math.max(0, position.getMarketValue() - orderValue);
        }
        
        if (BigDecimal.valueOf(newPositionValue).compareTo(userMaxPosition) > 0) {
            return "position_limit_exceeded";
        }

        // 7. Concentration risk check - single position vs total portfolio
        double totalPortfolioValue = portfolio.getTotalValue();
        if (totalPortfolioValue > 0) {
            double concentrationRatio = newPositionValue / totalPortfolioValue;
            if (concentrationRatio > 0.5) { // 50% concentration limit
                return "concentration_risk_too_high";
            }
        }

        // 8. Daily loss limit check
        String pnlKey = "pnl:daily:" + order.getUserId();
        String pnlStr = redis.opsForValue().get(pnlKey);
        double dailyPnl = pnlStr == null ? 0.0 : Double.parseDouble(pnlStr);
        double dailyLossLimit = limits.map(l -> l.getDailyLossLimit().doubleValue()).orElse(5000.0);
        
        if (dailyPnl <= -dailyLossLimit) {
            return "daily_loss_limit_exceeded";
        }

        // 9. Velocity check - too many orders in short time
        String velocityKey = "order_velocity:" + order.getUserId();
        String velocityStr = redis.opsForValue().get(velocityKey);
        int orderCount = velocityStr == null ? 0 : Integer.parseInt(velocityStr);
        
        if (orderCount > 10) { // More than 10 orders in the time window
            return "order_velocity_too_high";
        }
        
        // Increment velocity counter (expires in 1 hour)
        redis.opsForValue().increment(velocityKey);
        redis.expire(velocityKey, Duration.ofHours(1));

        // 10. Market hours check (optional - can be disabled)
        String marketHoursKey = "market_hours:" + order.getSymbol();
        String marketStatus = redis.opsForValue().get(marketHoursKey);
        if ("CLOSED".equals(marketStatus)) {
            return "market_closed";
        }

        // 11. Symbol-specific risk checks
        if (isHighRiskSymbol(order.getSymbol())) {
            // Additional checks for high-risk symbols
            if (orderValue > 50_000) { // Lower limit for high-risk symbols
                return "high_risk_symbol_limit_exceeded";
            }
        }

        return null; // All checks passed
    }
    
    /**
     * Check if symbol is considered high-risk
     */
    private boolean isHighRiskSymbol(String symbol) {
        // Define high-risk symbols (volatile stocks, crypto, etc.)
        String[] highRiskSymbols = {"TSLA", "NVDA", "BTC", "ETH", "DOGE"};
        for (String riskSymbol : highRiskSymbols) {
            if (riskSymbol.equalsIgnoreCase(symbol)) {
                return true;
            }
        }
        return false;
    }
}
