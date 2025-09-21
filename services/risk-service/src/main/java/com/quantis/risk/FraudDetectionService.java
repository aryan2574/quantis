package com.quantis.risk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Advanced Fraud Detection Service
 * 
 * Implements multiple fraud detection algorithms:
 * - Velocity-based detection
 * - Pattern recognition
 * - Anomaly detection
 * - Machine learning-based scoring
 * - Behavioral analysis
 */
@Service
public class FraudDetectionService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RiskMetricsService riskMetricsService;

    // Fraud detection thresholds
    private static final double VELOCITY_THRESHOLD = 0.8;
    private static final double ANOMALY_THRESHOLD = 0.7;
    private static final double PATTERN_THRESHOLD = 0.6;
    private static final int MAX_ORDERS_PER_MINUTE = 50;
    private static final int MAX_ORDERS_PER_HOUR = 200;
    private static final BigDecimal MAX_ORDER_VALUE = new BigDecimal("1000000");

    /**
     * Comprehensive fraud detection analysis
     */
    public FraudDetectionResult analyzeOrder(Order order, User user) {
        FraudDetectionResult result = new FraudDetectionResult();
        result.setOrderId(order.getOrderId());
        result.setUserId(user.getUserId());
        result.setTimestamp(LocalDateTime.now());

        try {
            // 1. Velocity-based detection
            double velocityScore = calculateVelocityScore(order, user);
            result.setVelocityScore(velocityScore);

            // 2. Pattern recognition
            double patternScore = analyzeTradingPatterns(order, user);
            result.setPatternScore(patternScore);

            // 3. Anomaly detection
            double anomalyScore = detectAnomalies(order, user);
            result.setAnomalyScore(anomalyScore);

            // 4. Behavioral analysis
            double behavioralScore = analyzeBehavior(order, user);
            result.setBehavioralScore(behavioralScore);

            // 5. ML-based scoring
            double mlScore = calculateMLScore(order, user);
            result.setMlScore(mlScore);

            // 6. Calculate overall risk score
            double overallScore = calculateOverallRiskScore(result);
            result.setOverallRiskScore(overallScore);

            // 7. Determine fraud probability
            FraudProbability probability = determineFraudProbability(overallScore);
            result.setFraudProbability(probability);

            // 8. Generate recommendations
            List<String> recommendations = generateRecommendations(result);
            result.setRecommendations(recommendations);

            // 9. Cache results for future analysis
            cacheFraudAnalysis(result);

            return result;

        } catch (Exception e) {
            result.setError("Fraud detection analysis failed: " + e.getMessage());
            result.setOverallRiskScore(1.0); // High risk on error
            result.setFraudProbability(FraudProbability.HIGH);
            return result;
        }
    }

    /**
     * Velocity-based fraud detection
     */
    private double calculateVelocityScore(Order order, User user) {
        String userId = user.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // Check orders per minute
        long ordersPerMinute = getOrderCount(userId, now.minus(1, ChronoUnit.MINUTES), now);
        double minuteScore = Math.min(ordersPerMinute / (double) MAX_ORDERS_PER_MINUTE, 1.0);

        // Check orders per hour
        long ordersPerHour = getOrderCount(userId, now.minus(1, ChronoUnit.HOURS), now);
        double hourScore = Math.min(ordersPerHour / (double) MAX_ORDERS_PER_HOUR, 1.0);

        // Check order value velocity
        BigDecimal totalValuePerHour = getTotalOrderValue(userId, now.minus(1, ChronoUnit.HOURS), now);
        double valueScore = totalValuePerHour.divide(MAX_ORDER_VALUE, 4, RoundingMode.HALF_UP).doubleValue();

        // Weighted average
        return (minuteScore * 0.4) + (hourScore * 0.3) + (Math.min(valueScore, 1.0) * 0.3);
    }

    /**
     * Pattern recognition analysis
     */
    private double analyzeTradingPatterns(Order order, User user) {
        String userId = user.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // Get recent trading patterns
        List<Order> recentOrders = getRecentOrders(userId, now.minus(24, ChronoUnit.HOURS), now);

        if (recentOrders.isEmpty()) {
            return 0.0; // No pattern data
        }

        double patternScore = 0.0;
        int patternCount = 0;

        // 1. Repetitive order patterns
        patternScore += detectRepetitivePatterns(recentOrders);
        patternCount++;

        // 2. Time-based patterns
        patternScore += detectTimePatterns(recentOrders);
        patternCount++;

        // 3. Price manipulation patterns
        patternScore += detectPriceManipulation(recentOrders);
        patternCount++;

        // 4. Volume patterns
        patternScore += detectVolumePatterns(recentOrders);
        patternCount++;

        return patternCount > 0 ? patternScore / patternCount : 0.0;
    }

    /**
     * Anomaly detection using statistical methods
     */
    private double detectAnomalies(Order order, User user) {
        String userId = user.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // Get historical data for comparison
        List<Order> historicalOrders = getRecentOrders(userId, now.minus(30, ChronoUnit.DAYS), now);

        if (historicalOrders.size() < 10) {
            return 0.0; // Insufficient data
        }

        double anomalyScore = 0.0;

        // 1. Order size anomaly
        anomalyScore += detectSizeAnomaly(order, historicalOrders);

        // 2. Timing anomaly
        anomalyScore += detectTimingAnomaly(order, historicalOrders);

        // 3. Symbol anomaly
        anomalyScore += detectSymbolAnomaly(order, historicalOrders);

        // 4. Price anomaly
        anomalyScore += detectPriceAnomaly(order, historicalOrders);

        return anomalyScore / 4.0;
    }

    /**
     * Behavioral analysis
     */
    private double analyzeBehavior(Order order, User user) {
        String userId = user.getUserId();
        LocalDateTime now = LocalDateTime.now();

        // Get user behavior metrics
        UserBehaviorMetrics metrics = getUserBehaviorMetrics(userId, now.minus(7, ChronoUnit.DAYS), now);

        double behaviorScore = 0.0;

        // 1. Login pattern analysis
        behaviorScore += analyzeLoginPatterns(metrics);

        // 2. Session duration analysis
        behaviorScore += analyzeSessionDuration(metrics);

        // 3. Device/location analysis
        behaviorScore += analyzeDeviceLocation(metrics);

        // 4. Trading behavior analysis
        behaviorScore += analyzeTradingBehavior(metrics);

        return behaviorScore / 4.0;
    }
    
    /**
     * Analyze login patterns for fraud detection
     */
    private double analyzeLoginPatterns(UserBehaviorMetrics metrics) {
        // TODO: Implement actual login pattern analysis
        return 0.1; // Default score
    }
    
    /**
     * Analyze session duration for fraud detection
     */
    private double analyzeSessionDuration(UserBehaviorMetrics metrics) {
        // TODO: Implement actual session duration analysis
        return 0.1; // Default score
    }
    
    /**
     * Analyze device and location for fraud detection
     */
    private double analyzeDeviceLocation(UserBehaviorMetrics metrics) {
        // TODO: Implement actual device/location analysis
        return 0.1; // Default score
    }
    
    /**
     * Analyze trading behavior for fraud detection
     */
    private double analyzeTradingBehavior(UserBehaviorMetrics metrics) {
        // TODO: Implement actual trading behavior analysis
        return 0.1; // Default score
    }

    /**
     * Machine learning-based scoring
     */
    private double calculateMLScore(Order order, User user) {
        // Feature extraction
        Map<String, Object> features = extractFeatures(order, user);

        // ML model prediction (simplified implementation)
        // In production, this would call a trained ML model
        double mlScore = 0.0;

        // Feature-based scoring
        mlScore += scoreFeature("order_size", (Double) features.get("order_size"));
        mlScore += scoreFeature("time_of_day", (Double) features.get("time_of_day"));
        mlScore += scoreFeature("day_of_week", (Double) features.get("day_of_week"));
        mlScore += scoreFeature("user_age", (Double) features.get("user_age"));
        mlScore += scoreFeature("account_balance", (Double) features.get("account_balance"));

        return Math.min(mlScore / 5.0, 1.0);
    }

    /**
     * Calculate overall risk score
     */
    private double calculateOverallRiskScore(FraudDetectionResult result) {
        double velocityWeight = 0.25;
        double patternWeight = 0.20;
        double anomalyWeight = 0.25;
        double behavioralWeight = 0.15;
        double mlWeight = 0.15;

        return (result.getVelocityScore() * velocityWeight) +
               (result.getPatternScore() * patternWeight) +
               (result.getAnomalyScore() * anomalyWeight) +
               (result.getBehavioralScore() * behavioralWeight) +
               (result.getMlScore() * mlWeight);
    }

    /**
     * Determine fraud probability
     */
    private FraudProbability determineFraudProbability(double overallScore) {
        if (overallScore >= 0.8) {
            return FraudProbability.HIGH;
        } else if (overallScore >= 0.6) {
            return FraudProbability.MEDIUM;
        } else if (overallScore >= 0.4) {
            return FraudProbability.LOW;
        } else {
            return FraudProbability.MINIMAL;
        }
    }

    /**
     * Generate recommendations based on analysis
     */
    private List<String> generateRecommendations(FraudDetectionResult result) {
        List<String> recommendations = new ArrayList<>();

        if (result.getVelocityScore() > VELOCITY_THRESHOLD) {
            recommendations.add("High order velocity detected - consider rate limiting");
        }

        if (result.getPatternScore() > PATTERN_THRESHOLD) {
            recommendations.add("Suspicious trading patterns detected - manual review required");
        }

        if (result.getAnomalyScore() > ANOMALY_THRESHOLD) {
            recommendations.add("Statistical anomalies detected - additional verification needed");
        }

        if (result.getBehavioralScore() > 0.7) {
            recommendations.add("Unusual behavioral patterns detected - user verification required");
        }

        if (result.getOverallRiskScore() > 0.8) {
            recommendations.add("HIGH RISK: Immediate manual review and potential account suspension");
        } else if (result.getOverallRiskScore() > 0.6) {
            recommendations.add("MEDIUM RISK: Enhanced monitoring and verification required");
        } else if (result.getOverallRiskScore() > 0.4) {
            recommendations.add("LOW RISK: Standard monitoring recommended");
        }

        return recommendations;
    }

    // Helper methods for pattern detection
    private double detectRepetitivePatterns(List<Order> orders) {
        // Detect identical orders repeated multiple times
        Map<String, Integer> orderPatterns = new HashMap<>();
        
        for (Order order : orders) {
            String pattern = order.getSymbol() + ":" + order.getSide() + ":" + order.getQuantity();
            orderPatterns.put(pattern, orderPatterns.getOrDefault(pattern, 0) + 1);
        }

        int maxRepetitions = orderPatterns.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        return Math.min(maxRepetitions / 10.0, 1.0);
    }

    private double detectTimePatterns(List<Order> orders) {
        // Detect orders placed at suspicious times (e.g., very early morning, weekends)
        long suspiciousOrders = orders.stream()
            .filter(order -> isSuspiciousTime(order.getCreatedAt()))
            .count();

        return Math.min(suspiciousOrders / (double) orders.size(), 1.0);
    }

    private double detectPriceManipulation(List<Order> orders) {
        // Detect potential price manipulation patterns
        // This is a simplified implementation
        return 0.0; // Placeholder for complex price manipulation detection
    }

    private double detectVolumePatterns(List<Order> orders) {
        // Detect unusual volume patterns
        if (orders.size() < 5) return 0.0;

        double avgVolume = orders.stream()
            .mapToDouble(Order::getQuantity)
            .average()
            .orElse(0.0);

        double maxVolume = orders.stream()
            .mapToDouble(Order::getQuantity)
            .max()
            .orElse(0.0);

        return Math.min(maxVolume / (avgVolume * 5), 1.0);
    }

    // Anomaly detection helper methods
    private double detectSizeAnomaly(Order currentOrder, List<Order> historicalOrders) {
        double avgSize = historicalOrders.stream()
            .mapToDouble(Order::getQuantity)
            .average()
            .orElse(0.0);

        double stdDev = calculateStandardDeviation(
            historicalOrders.stream().mapToDouble(Order::getQuantity).toArray()
        );

        double zScore = Math.abs(currentOrder.getQuantity() - avgSize) / stdDev;
        return Math.min(zScore / 3.0, 1.0); // Normalize to 0-1
    }

    private double detectTimingAnomaly(Order currentOrder, List<Order> historicalOrders) {
        // Analyze timing patterns
        return 0.0; // Placeholder for timing anomaly detection
    }

    private double detectSymbolAnomaly(Order currentOrder, List<Order> historicalOrders) {
        // Check if user is trading new symbols
        Set<String> historicalSymbols = historicalOrders.stream()
            .map(Order::getSymbol)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);

        return historicalSymbols.contains(currentOrder.getSymbol()) ? 0.0 : 0.5;
    }

    private double detectPriceAnomaly(Order currentOrder, List<Order> historicalOrders) {
        // Analyze price patterns
        return 0.0; // Placeholder for price anomaly detection
    }

    // Utility methods
    private long getOrderCount(String userId, LocalDateTime start, LocalDateTime end) {
        // Implementation would query database
        return 0; // Placeholder
    }

    private BigDecimal getTotalOrderValue(String userId, LocalDateTime start, LocalDateTime end) {
        // Implementation would query database
        return BigDecimal.ZERO; // Placeholder
    }

    private List<Order> getRecentOrders(String userId, LocalDateTime start, LocalDateTime end) {
        // Implementation would query database
        return new ArrayList<>(); // Placeholder
    }

    private UserBehaviorMetrics getUserBehaviorMetrics(String userId, LocalDateTime start, LocalDateTime end) {
        // Implementation would query database
        return new UserBehaviorMetrics(); // Placeholder
    }

    private Map<String, Object> extractFeatures(Order order, User user) {
        Map<String, Object> features = new HashMap<>();
        features.put("order_size", order.getQuantity());
        features.put("time_of_day", LocalDateTime.now().getHour());
        features.put("day_of_week", LocalDateTime.now().getDayOfWeek().getValue());
        features.put("user_age", calculateUserAge(user));
        features.put("account_balance", user.getAccountBalance());
        return features;
    }

    private double scoreFeature(String featureName, Double value) {
        // Simplified feature scoring
        return Math.random() * 0.2; // Placeholder
    }

    private boolean isSuspiciousTime(LocalDateTime time) {
        int hour = time.getHour();
        return hour < 6 || hour > 22; // Suspicious if outside normal trading hours
    }

    private double calculateStandardDeviation(double[] values) {
        double mean = Arrays.stream(values).average().orElse(0.0);
        double variance = Arrays.stream(values)
            .map(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    private int calculateUserAge(User user) {
        // Calculate user account age in days
        return (int) ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());
    }

    private void cacheFraudAnalysis(FraudDetectionResult result) {
        String key = "fraud_analysis:" + result.getOrderId();
        redisTemplate.opsForValue().set(key, result, 24, TimeUnit.HOURS);
    }

    // Data classes
    public static class FraudDetectionResult {
        private String orderId;
        private String userId;
        private LocalDateTime timestamp;
        private double velocityScore;
        private double patternScore;
        private double anomalyScore;
        private double behavioralScore;
        private double mlScore;
        private double overallRiskScore;
        private FraudProbability fraudProbability;
        private List<String> recommendations;
        private String error;

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public double getVelocityScore() { return velocityScore; }
        public void setVelocityScore(double velocityScore) { this.velocityScore = velocityScore; }
        
        public double getPatternScore() { return patternScore; }
        public void setPatternScore(double patternScore) { this.patternScore = patternScore; }
        
        public double getAnomalyScore() { return anomalyScore; }
        public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }
        
        public double getBehavioralScore() { return behavioralScore; }
        public void setBehavioralScore(double behavioralScore) { this.behavioralScore = behavioralScore; }
        
        public double getMlScore() { return mlScore; }
        public void setMlScore(double mlScore) { this.mlScore = mlScore; }
        
        public double getOverallRiskScore() { return overallRiskScore; }
        public void setOverallRiskScore(double overallRiskScore) { this.overallRiskScore = overallRiskScore; }
        
        public FraudProbability getFraudProbability() { return fraudProbability; }
        public void setFraudProbability(FraudProbability fraudProbability) { this.fraudProbability = fraudProbability; }
        
        public List<String> getRecommendations() { return recommendations; }
        public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    public enum FraudProbability {
        MINIMAL, LOW, MEDIUM, HIGH
    }

    public static class UserBehaviorMetrics {
        private int loginCount;
        private double avgSessionDuration;
        private Set<String> devices;
        private Set<String> locations;
        private double tradingFrequency;

        // Getters and setters
        public int getLoginCount() { return loginCount; }
        public void setLoginCount(int loginCount) { this.loginCount = loginCount; }
        
        public double getAvgSessionDuration() { return avgSessionDuration; }
        public void setAvgSessionDuration(double avgSessionDuration) { this.avgSessionDuration = avgSessionDuration; }
        
        public Set<String> getDevices() { return devices; }
        public void setDevices(Set<String> devices) { this.devices = devices; }
        
        public Set<String> getLocations() { return locations; }
        public void setLocations(Set<String> locations) { this.locations = locations; }
        
        public double getTradingFrequency() { return tradingFrequency; }
        public void setTradingFrequency(double tradingFrequency) { this.tradingFrequency = tradingFrequency; }
    }

    // Placeholder classes (would be defined elsewhere)
    public static class Order {
        private String orderId;
        private String symbol;
        private String side;
        private double quantity;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getSide() { return side; }
        public void setSide(String side) { this.side = side; }
        
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class User {
        private String userId;
        private BigDecimal accountBalance;
        private LocalDateTime createdAt;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public BigDecimal getAccountBalance() { return accountBalance; }
        public void setAccountBalance(BigDecimal accountBalance) { this.accountBalance = accountBalance; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
