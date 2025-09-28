package com.quantis.risk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Real-time Risk Monitoring Service
 * 
 * Provides:
 * - Real-time risk metrics calculation
 * - Market-wide risk monitoring
 * - User-specific risk tracking
 * - Automated risk alerts
 * - Risk dashboard data streaming
 */
@Service
public class RealTimeRiskMonitor {
    private static final Logger log = LoggerFactory.getLogger(RealTimeRiskMonitor.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FraudDetectionService fraudDetectionService;

    @Autowired
    private RiskMetricsService riskMetricsService;

    // Real-time risk data storage
    private final Map<String, UserRiskProfile> userRiskProfiles = new ConcurrentHashMap<>();
    private final Map<String, MarketRiskMetrics> marketRiskMetrics = new ConcurrentHashMap<>();
    private final AtomicReference<SystemRiskStatus> systemRiskStatus = new AtomicReference<>();

    // Risk thresholds
    private static final double HIGH_RISK_THRESHOLD = 0.8;
    private static final double MEDIUM_RISK_THRESHOLD = 0.6;
    private static final double LOW_RISK_THRESHOLD = 0.4;
    private static final BigDecimal MAX_PORTFOLIO_RISK = new BigDecimal("1000000");
    private static final double MAX_CORRELATION_RISK = 0.8;

    /**
     * Initialize real-time monitoring
     */
    @Scheduled(fixedRate = 5000) // Update every 5 seconds
    public void updateRealTimeRiskMetrics() {
        try {
            // Update user risk profiles
            updateUserRiskProfiles();

            // Update market risk metrics
            updateMarketRiskMetrics();

            // Update system risk status
            updateSystemRiskStatus();

            // Broadcast updates to WebSocket clients
            broadcastRiskUpdates();

        } catch (Exception e) {
            System.err.println("Error updating real-time risk metrics: " + e.getMessage());
        }
    }

    /**
     * Process incoming trade events for risk monitoring
     */
    @KafkaListener(topics = "trades", groupId = "risk-monitor")
    public void processTradeEvent(TradeEvent tradeEvent) {
        try {
            String userId = tradeEvent.getUserId();
            
            // Update user risk profile
            updateUserRiskProfileFromTrade(userId, tradeEvent);

            // Check for immediate risk alerts
            checkImmediateRiskAlerts(userId, tradeEvent);

            // Update market risk metrics
            updateMarketRiskFromTrade(tradeEvent);

        } catch (Exception e) {
            System.err.println("Error processing trade event: " + e.getMessage());
        }
    }
    
    /**
     * Update market risk metrics from trade event
     */
    private void updateMarketRiskFromTrade(TradeEvent tradeEvent) {
        // TODO: Implement actual market risk update logic
        log.debug("Updating market risk from trade event: {}", tradeEvent);
    }

    /**
     * Process incoming order events for risk monitoring
     */
    @KafkaListener(topics = "orders", groupId = "risk-monitor")
    public void processOrderEvent(OrderEvent orderEvent) {
        try {
            String userId = orderEvent.getUserId();
            
            // Perform fraud detection
            FraudDetectionService.FraudDetectionResult fraudResult = 
                fraudDetectionService.analyzeOrder((FraudDetectionService.Order) orderEvent.getOrder(), (FraudDetectionService.User) orderEvent.getUser());

            // Update user risk profile
            updateUserRiskProfileFromOrder(userId, orderEvent, fraudResult);

            // Check for fraud alerts
            if (fraudResult.getFraudProbability() == FraudDetectionService.FraudProbability.HIGH) {
                triggerFraudAlert(userId, fraudResult);
            }

        } catch (Exception e) {
            System.err.println("Error processing order event: " + e.getMessage());
        }
    }

    /**
     * Get real-time risk dashboard data
     */
    public RiskDashboardData getRiskDashboardData() {
        RiskDashboardData dashboardData = new RiskDashboardData();
        dashboardData.setTimestamp(LocalDateTime.now());
        dashboardData.setSystemRiskStatus(systemRiskStatus.get());
        dashboardData.setMarketRiskMetrics(new HashMap<>(marketRiskMetrics));
        dashboardData.setHighRiskUsers(getHighRiskUsers());
        dashboardData.setRecentAlerts(getRecentAlerts());
        dashboardData.setRiskTrends(calculateRiskTrends());
        
        return dashboardData;
    }

    /**
     * Get user-specific risk profile
     */
    public UserRiskProfile getUserRiskProfile(String userId) {
        return userRiskProfiles.getOrDefault(userId, createDefaultUserRiskProfile(userId));
    }

    /**
     * Get market-wide risk metrics
     */
    public MarketRiskMetrics getMarketRiskMetrics(String symbol) {
        return marketRiskMetrics.getOrDefault(symbol, createDefaultMarketRiskMetrics(symbol));
    }

    /**
     * Update user risk profiles
     */
    private void updateUserRiskProfiles() {
        // Get all active users from Redis
        Set<String> activeUsers = redisTemplate.keys("user:*");
        
        for (String userKey : activeUsers) {
            String userId = userKey.replace("user:", "");
            UserRiskProfile profile = userRiskProfiles.computeIfAbsent(userId, this::createDefaultUserRiskProfile);
            
            // Update risk metrics
            updateUserRiskMetrics(profile);
            
            // Check risk thresholds
            checkUserRiskThresholds(profile);
        }
    }

    /**
     * Update market risk metrics
     */
    private void updateMarketRiskMetrics() {
        // Get all active symbols
        Set<String> symbols = getActiveSymbols();
        
        for (String symbol : symbols) {
            MarketRiskMetrics metrics = marketRiskMetrics.computeIfAbsent(symbol, this::createDefaultMarketRiskMetrics);
            
            // Update market risk calculations
            updateMarketRiskCalculations(metrics);
            
            // Check market risk thresholds
            checkMarketRiskThresholds(metrics);
        }
    }

    /**
     * Update system risk status
     */
    private void updateSystemRiskStatus() {
        SystemRiskStatus status = systemRiskStatus.get();
        if (status == null) {
            status = new SystemRiskStatus();
            systemRiskStatus.set(status);
        }

        // Calculate system-wide risk metrics
        status.setTotalUsers(userRiskProfiles.size());
        status.setHighRiskUsers(countHighRiskUsers());
        status.setSystemRiskScore(calculateSystemRiskScore());
        status.setMarketVolatility(calculateMarketVolatility());
        status.setLastUpdated(LocalDateTime.now());

        // Determine overall system risk level
        status.setRiskLevel(determineSystemRiskLevel(status.getSystemRiskScore()));
    }

    /**
     * Update user risk profile from trade
     */
    private void updateUserRiskProfileFromTrade(String userId, TradeEvent tradeEvent) {
        UserRiskProfile profile = userRiskProfiles.computeIfAbsent(userId, this::createDefaultUserRiskProfile);
        
        // Update trade-based metrics
        profile.setTotalTrades(profile.getTotalTrades() + 1);
        profile.setTotalVolume(profile.getTotalVolume().add(tradeEvent.getQuantity()));
        profile.setLastTradeTime(LocalDateTime.now());
        
        // Update position risk
        updatePositionRisk(profile, tradeEvent);
        
        // Update portfolio risk
        updatePortfolioRisk(profile);
        
        // Update correlation risk
        updateCorrelationRisk(profile, tradeEvent);
    }

    /**
     * Update user risk profile from order
     */
    private void updateUserRiskProfileFromOrder(String userId, OrderEvent orderEvent, 
                                              FraudDetectionService.FraudDetectionResult fraudResult) {
        UserRiskProfile profile = userRiskProfiles.computeIfAbsent(userId, this::createDefaultUserRiskProfile);
        
        // Update fraud risk score
        profile.setFraudRiskScore(fraudResult.getOverallRiskScore());
        
        // Update order-based metrics
        profile.setTotalOrders(profile.getTotalOrders() + 1);
        profile.setLastOrderTime(LocalDateTime.now());
        
        // Update velocity metrics
        updateVelocityMetrics(profile, orderEvent);
    }

    /**
     * Check for immediate risk alerts
     */
    private void checkImmediateRiskAlerts(String userId, TradeEvent tradeEvent) {
        UserRiskProfile profile = userRiskProfiles.get(userId);
        if (profile == null) return;

        // Check for excessive trading
        if (profile.getTradesPerMinute() > 10) {
            triggerRiskAlert(userId, "EXCESSIVE_TRADING", "User trading rate exceeds threshold");
        }

        // Check for large position size
        if (tradeEvent.getQuantity().compareTo(profile.getAverageTradeSize().multiply(new BigDecimal("5"))) > 0) {
            triggerRiskAlert(userId, "LARGE_POSITION", "Trade size significantly exceeds average");
        }

        // Check for portfolio concentration
        if (profile.getPortfolioConcentration() > 0.8) {
            triggerRiskAlert(userId, "PORTFOLIO_CONCENTRATION", "Portfolio too concentrated in single position");
        }
    }

    /**
     * Update position risk
     */
    private void updatePositionRisk(UserRiskProfile profile, TradeEvent tradeEvent) {
        // Calculate position-specific risk metrics
        BigDecimal positionValue = tradeEvent.getPrice().multiply(tradeEvent.getQuantity());
        BigDecimal portfolioValue = profile.getPortfolioValue();
        
        if (portfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            double positionWeight = positionValue.divide(portfolioValue, 4, RoundingMode.HALF_UP).doubleValue();
            profile.setLargestPositionWeight(Math.max(profile.getLargestPositionWeight(), positionWeight));
        }
    }

    /**
     * Update portfolio risk
     */
    private void updatePortfolioRisk(UserRiskProfile profile) {
        // Calculate portfolio-level risk metrics
        BigDecimal portfolioValue = profile.getPortfolioValue();
        BigDecimal portfolioRisk = profile.getPortfolioRisk();
        
        // Update portfolio risk score
        double riskScore = portfolioRisk.divide(portfolioValue, 4, RoundingMode.HALF_UP).doubleValue();
        profile.setPortfolioRiskScore(Math.min(riskScore, 1.0));
    }

    /**
     * Update correlation risk
     */
    private void updateCorrelationRisk(UserRiskProfile profile, TradeEvent tradeEvent) {
        // Calculate correlation with market movements
        // This is a simplified implementation
        double correlation = calculateCorrelation(profile, tradeEvent);
        profile.setMarketCorrelation(Math.max(profile.getMarketCorrelation(), correlation));
    }

    /**
     * Update velocity metrics
     */
    private void updateVelocityMetrics(UserRiskProfile profile, OrderEvent orderEvent) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);
        
        // Count orders in last minute
        long ordersInLastMinute = profile.getRecentOrders().stream()
            .filter(order -> order.isAfter(oneMinuteAgo))
            .count();
        
        profile.setOrdersPerMinute((int) ordersInLastMinute);
        profile.setTradesPerMinute((int) ordersInLastMinute); // Simplified
    }

    /**
     * Check user risk thresholds
     */
    private void checkUserRiskThresholds(UserRiskProfile profile) {
        double overallRisk = profile.getOverallRiskScore();
        
        if (overallRisk > HIGH_RISK_THRESHOLD) {
            profile.setRiskLevel(RiskLevel.HIGH);
            triggerRiskAlert(profile.getUserId(), "HIGH_RISK_USER", "User risk score exceeds high threshold");
        } else if (overallRisk > MEDIUM_RISK_THRESHOLD) {
            profile.setRiskLevel(RiskLevel.MEDIUM);
        } else if (overallRisk > LOW_RISK_THRESHOLD) {
            profile.setRiskLevel(RiskLevel.LOW);
        } else {
            profile.setRiskLevel(RiskLevel.MINIMAL);
        }
    }

    /**
     * Check market risk thresholds
     */
    private void checkMarketRiskThresholds(MarketRiskMetrics metrics) {
        double volatility = metrics.getVolatility();
        
        if (volatility > 0.8) {
            metrics.setRiskLevel(RiskLevel.HIGH);
            triggerMarketAlert(metrics.getSymbol(), "HIGH_VOLATILITY", "Market volatility exceeds threshold");
        } else if (volatility > 0.6) {
            metrics.setRiskLevel(RiskLevel.MEDIUM);
        } else {
            metrics.setRiskLevel(RiskLevel.LOW);
        }
    }

    /**
     * Trigger fraud alert
     */
    private void triggerFraudAlert(String userId, FraudDetectionService.FraudDetectionResult fraudResult) {
        RiskAlert alert = new RiskAlert();
        alert.setUserId(userId);
        alert.setAlertType("FRAUD_DETECTED");
        alert.setSeverity(AlertSeverity.CRITICAL);
        alert.setMessage("High probability fraud detected: " + fraudResult.getRecommendations());
        alert.setTimestamp(LocalDateTime.now());
        
        // Store alert
        storeRiskAlert(alert);
        
        // Send real-time notification
        messagingTemplate.convertAndSend("/topic/risk-alerts", alert);
    }

    /**
     * Trigger risk alert
     */
    private void triggerRiskAlert(String userId, String alertType, String message) {
        RiskAlert alert = new RiskAlert();
        alert.setUserId(userId);
        alert.setAlertType(alertType);
        alert.setSeverity(AlertSeverity.WARNING);
        alert.setMessage(message);
        alert.setTimestamp(LocalDateTime.now());
        
        // Store alert
        storeRiskAlert(alert);
        
        // Send real-time notification
        messagingTemplate.convertAndSend("/topic/risk-alerts", alert);
    }

    /**
     * Trigger market alert
     */
    private void triggerMarketAlert(String symbol, String alertType, String message) {
        RiskAlert alert = new RiskAlert();
        alert.setSymbol(symbol);
        alert.setAlertType(alertType);
        alert.setSeverity(AlertSeverity.WARNING);
        alert.setMessage(message);
        alert.setTimestamp(LocalDateTime.now());
        
        // Store alert
        storeRiskAlert(alert);
        
        // Send real-time notification
        messagingTemplate.convertAndSend("/topic/market-alerts", alert);
    }

    /**
     * Broadcast risk updates to WebSocket clients
     */
    private void broadcastRiskUpdates() {
        // Broadcast system risk status
        messagingTemplate.convertAndSend("/topic/system-risk", systemRiskStatus.get());
        
        // Broadcast high-risk users
        List<UserRiskProfile> highRiskUsers = getHighRiskUsers();
        messagingTemplate.convertAndSend("/topic/high-risk-users", highRiskUsers);
        
        // Broadcast market risk metrics
        messagingTemplate.convertAndSend("/topic/market-risk", marketRiskMetrics);
    }

    // Helper methods
    private UserRiskProfile createDefaultUserRiskProfile(String userId) {
        UserRiskProfile profile = new UserRiskProfile();
        profile.setUserId(userId);
        profile.setRiskLevel(RiskLevel.MINIMAL);
        profile.setOverallRiskScore(0.0);
        profile.setPortfolioValue(BigDecimal.ZERO);
        profile.setPortfolioRisk(BigDecimal.ZERO);
        profile.setTotalTrades(0);
        profile.setTotalOrders(0);
        profile.setTotalVolume(BigDecimal.ZERO);
        profile.setAverageTradeSize(BigDecimal.ZERO);
        profile.setFraudRiskScore(0.0);
        profile.setPortfolioRiskScore(0.0);
        profile.setMarketCorrelation(0.0);
        profile.setLargestPositionWeight(0.0);
        profile.setPortfolioConcentration(0.0);
        profile.setOrdersPerMinute(0);
        profile.setTradesPerMinute(0);
        profile.setLastTradeTime(LocalDateTime.now());
        profile.setLastOrderTime(LocalDateTime.now());
        profile.setRecentOrders(new ArrayList<>());
        
        return profile;
    }

    private MarketRiskMetrics createDefaultMarketRiskMetrics(String symbol) {
        MarketRiskMetrics metrics = new MarketRiskMetrics();
        metrics.setSymbol(symbol);
        metrics.setRiskLevel(RiskLevel.LOW);
        metrics.setVolatility(0.0);
        metrics.setVolume(0);
        metrics.setPriceChange(0.0);
        metrics.setLastUpdated(LocalDateTime.now());
        
        return metrics;
    }

    private void updateUserRiskMetrics(UserRiskProfile profile) {
        // Calculate overall risk score
        double overallRisk = calculateOverallUserRisk(profile);
        profile.setOverallRiskScore(overallRisk);
    }

    private void updateMarketRiskCalculations(MarketRiskMetrics metrics) {
        // Calculate market risk metrics
        double volatility = calculateVolatility(metrics.getSymbol());
        metrics.setVolatility(volatility);
        metrics.setLastUpdated(LocalDateTime.now());
    }

    private double calculateOverallUserRisk(UserRiskProfile profile) {
        double fraudWeight = 0.3;
        double portfolioWeight = 0.3;
        double velocityWeight = 0.2;
        double concentrationWeight = 0.2;

        return (profile.getFraudRiskScore() * fraudWeight) +
               (profile.getPortfolioRiskScore() * portfolioWeight) +
               (Math.min(profile.getOrdersPerMinute() / 10.0, 1.0) * velocityWeight) +
               (profile.getPortfolioConcentration() * concentrationWeight);
    }

    private double calculateVolatility(String symbol) {
        // Simplified volatility calculation
        return Math.random() * 0.5; // Placeholder
    }

    private double calculateCorrelation(UserRiskProfile profile, TradeEvent tradeEvent) {
        // Simplified correlation calculation
        return Math.random() * 0.3; // Placeholder
    }

    private int countHighRiskUsers() {
        return (int) userRiskProfiles.values().stream()
            .filter(profile -> profile.getRiskLevel() == RiskLevel.HIGH)
            .count();
    }

    private double calculateSystemRiskScore() {
        if (userRiskProfiles.isEmpty()) return 0.0;
        
        double totalRisk = userRiskProfiles.values().stream()
            .mapToDouble(UserRiskProfile::getOverallRiskScore)
            .sum();
        
        return totalRisk / userRiskProfiles.size();
    }

    private double calculateMarketVolatility() {
        if (marketRiskMetrics.isEmpty()) return 0.0;
        
        double totalVolatility = marketRiskMetrics.values().stream()
            .mapToDouble(MarketRiskMetrics::getVolatility)
            .sum();
        
        return totalVolatility / marketRiskMetrics.size();
    }

    private RiskLevel determineSystemRiskLevel(double systemRiskScore) {
        if (systemRiskScore > HIGH_RISK_THRESHOLD) {
            return RiskLevel.HIGH;
        } else if (systemRiskScore > MEDIUM_RISK_THRESHOLD) {
            return RiskLevel.MEDIUM;
        } else if (systemRiskScore > LOW_RISK_THRESHOLD) {
            return RiskLevel.LOW;
        } else {
            return RiskLevel.MINIMAL;
        }
    }

    private Set<String> getActiveSymbols() {
        // Get active symbols from Redis
        return redisTemplate.keys("symbol:*").stream()
            .map(key -> key.toString().replace("symbol:", ""))
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    private List<UserRiskProfile> getHighRiskUsers() {
        return userRiskProfiles.values().stream()
            .filter(profile -> profile.getRiskLevel() == RiskLevel.HIGH)
            .limit(10)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private List<RiskAlert> getRecentAlerts() {
        // Get recent alerts from Redis
        return new ArrayList<>(); // Placeholder
    }

    private Map<String, Double> calculateRiskTrends() {
        // Calculate risk trends over time
        Map<String, Double> trends = new HashMap<>();
        trends.put("system_risk_trend", 0.0);
        trends.put("user_risk_trend", 0.0);
        trends.put("market_risk_trend", 0.0);
        return trends;
    }

    private void storeRiskAlert(RiskAlert alert) {
        String key = "risk_alert:" + alert.getTimestamp().toString();
        redisTemplate.opsForValue().set(key, alert, 24, java.util.concurrent.TimeUnit.HOURS);
    }

    // Data classes
    public static class UserRiskProfile {
        private String userId;
        private RiskLevel riskLevel;
        private double overallRiskScore;
        private BigDecimal portfolioValue;
        private BigDecimal portfolioRisk;
        private int totalTrades;
        private int totalOrders;
        private BigDecimal totalVolume;
        private BigDecimal averageTradeSize;
        private double fraudRiskScore;
        private double portfolioRiskScore;
        private double marketCorrelation;
        private double largestPositionWeight;
        private double portfolioConcentration;
        private int ordersPerMinute;
        private int tradesPerMinute;
        private LocalDateTime lastTradeTime;
        private LocalDateTime lastOrderTime;
        private List<LocalDateTime> recentOrders;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
        
        public double getOverallRiskScore() { return overallRiskScore; }
        public void setOverallRiskScore(double overallRiskScore) { this.overallRiskScore = overallRiskScore; }
        
        public BigDecimal getPortfolioValue() { return portfolioValue; }
        public void setPortfolioValue(BigDecimal portfolioValue) { this.portfolioValue = portfolioValue; }
        
        public BigDecimal getPortfolioRisk() { return portfolioRisk; }
        public void setPortfolioRisk(BigDecimal portfolioRisk) { this.portfolioRisk = portfolioRisk; }
        
        public int getTotalTrades() { return totalTrades; }
        public void setTotalTrades(int totalTrades) { this.totalTrades = totalTrades; }
        
        public int getTotalOrders() { return totalOrders; }
        public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
        
        public BigDecimal getTotalVolume() { return totalVolume; }
        public void setTotalVolume(BigDecimal totalVolume) { this.totalVolume = totalVolume; }
        
        public BigDecimal getAverageTradeSize() { return averageTradeSize; }
        public void setAverageTradeSize(BigDecimal averageTradeSize) { this.averageTradeSize = averageTradeSize; }
        
        public double getFraudRiskScore() { return fraudRiskScore; }
        public void setFraudRiskScore(double fraudRiskScore) { this.fraudRiskScore = fraudRiskScore; }
        
        public double getPortfolioRiskScore() { return portfolioRiskScore; }
        public void setPortfolioRiskScore(double portfolioRiskScore) { this.portfolioRiskScore = portfolioRiskScore; }
        
        public double getMarketCorrelation() { return marketCorrelation; }
        public void setMarketCorrelation(double marketCorrelation) { this.marketCorrelation = marketCorrelation; }
        
        public double getLargestPositionWeight() { return largestPositionWeight; }
        public void setLargestPositionWeight(double largestPositionWeight) { this.largestPositionWeight = largestPositionWeight; }
        
        public double getPortfolioConcentration() { return portfolioConcentration; }
        public void setPortfolioConcentration(double portfolioConcentration) { this.portfolioConcentration = portfolioConcentration; }
        
        public int getOrdersPerMinute() { return ordersPerMinute; }
        public void setOrdersPerMinute(int ordersPerMinute) { this.ordersPerMinute = ordersPerMinute; }
        
        public int getTradesPerMinute() { return tradesPerMinute; }
        public void setTradesPerMinute(int tradesPerMinute) { this.tradesPerMinute = tradesPerMinute; }
        
        public LocalDateTime getLastTradeTime() { return lastTradeTime; }
        public void setLastTradeTime(LocalDateTime lastTradeTime) { this.lastTradeTime = lastTradeTime; }
        
        public LocalDateTime getLastOrderTime() { return lastOrderTime; }
        public void setLastOrderTime(LocalDateTime lastOrderTime) { this.lastOrderTime = lastOrderTime; }
        
        public List<LocalDateTime> getRecentOrders() { return recentOrders; }
        public void setRecentOrders(List<LocalDateTime> recentOrders) { this.recentOrders = recentOrders; }
    }

    public static class MarketRiskMetrics {
        private String symbol;
        private RiskLevel riskLevel;
        private double volatility;
        private long volume;
        private double priceChange;
        private LocalDateTime lastUpdated;

        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
        
        public double getVolatility() { return volatility; }
        public void setVolatility(double volatility) { this.volatility = volatility; }
        
        public long getVolume() { return volume; }
        public void setVolume(long volume) { this.volume = volume; }
        
        public double getPriceChange() { return priceChange; }
        public void setPriceChange(double priceChange) { this.priceChange = priceChange; }
        
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class SystemRiskStatus {
        private int totalUsers;
        private int highRiskUsers;
        private double systemRiskScore;
        private double marketVolatility;
        private RiskLevel riskLevel;
        private LocalDateTime lastUpdated;

        // Getters and setters
        public int getTotalUsers() { return totalUsers; }
        public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
        
        public int getHighRiskUsers() { return highRiskUsers; }
        public void setHighRiskUsers(int highRiskUsers) { this.highRiskUsers = highRiskUsers; }
        
        public double getSystemRiskScore() { return systemRiskScore; }
        public void setSystemRiskScore(double systemRiskScore) { this.systemRiskScore = systemRiskScore; }
        
        public double getMarketVolatility() { return marketVolatility; }
        public void setMarketVolatility(double marketVolatility) { this.marketVolatility = marketVolatility; }
        
        public RiskLevel getRiskLevel() { return riskLevel; }
        public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }
        
        public LocalDateTime getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class RiskDashboardData {
        private LocalDateTime timestamp;
        private SystemRiskStatus systemRiskStatus;
        private Map<String, MarketRiskMetrics> marketRiskMetrics;
        private List<UserRiskProfile> highRiskUsers;
        private List<RiskAlert> recentAlerts;
        private Map<String, Double> riskTrends;

        // Getters and setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        
        public SystemRiskStatus getSystemRiskStatus() { return systemRiskStatus; }
        public void setSystemRiskStatus(SystemRiskStatus systemRiskStatus) { this.systemRiskStatus = systemRiskStatus; }
        
        public Map<String, MarketRiskMetrics> getMarketRiskMetrics() { return marketRiskMetrics; }
        public void setMarketRiskMetrics(Map<String, MarketRiskMetrics> marketRiskMetrics) { this.marketRiskMetrics = marketRiskMetrics; }
        
        public List<UserRiskProfile> getHighRiskUsers() { return highRiskUsers; }
        public void setHighRiskUsers(List<UserRiskProfile> highRiskUsers) { this.highRiskUsers = highRiskUsers; }
        
        public List<RiskAlert> getRecentAlerts() { return recentAlerts; }
        public void setRecentAlerts(List<RiskAlert> recentAlerts) { this.recentAlerts = recentAlerts; }
        
        public Map<String, Double> getRiskTrends() { return riskTrends; }
        public void setRiskTrends(Map<String, Double> riskTrends) { this.riskTrends = riskTrends; }
    }

    public static class RiskAlert {
        private String userId;
        private String symbol;
        private String alertType;
        private AlertSeverity severity;
        private String message;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        
        public AlertSeverity getSeverity() { return severity; }
        public void setSeverity(AlertSeverity severity) { this.severity = severity; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public enum RiskLevel {
        MINIMAL, LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum AlertSeverity {
        INFO, WARNING, CRITICAL
    }

    // Placeholder classes for events
    public static class TradeEvent {
        private String userId;
        private String symbol;
        private BigDecimal quantity;
        private BigDecimal price;
        private LocalDateTime timestamp;

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class OrderEvent {
        private String userId;
        private Object order; // Placeholder for Order object
        private Object user; // Placeholder for User object

        // Getters and setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public Object getOrder() { return order; }
        public void setOrder(Object order) { this.order = order; }
        
        public Object getUser() { return user; }
        public void setUser(Object user) { this.user = user; }
    }
}
