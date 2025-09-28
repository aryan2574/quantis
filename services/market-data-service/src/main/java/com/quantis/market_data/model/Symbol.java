package com.quantis.market_data.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Enhanced Symbol Management System
 * 
 * Supports multiple asset classes with unified symbol handling
 */
public class Symbol {
    private String symbol;
    private String baseSymbol;
    private AssetType assetType;
    private String exchange;
    private String currency;
    private String baseCurrency;
    private String quoteCurrency;
    private BigDecimal contractSize;
    private BigDecimal tickSize;
    private BigDecimal minOrderSize;
    private BigDecimal maxOrderSize;
    private BigDecimal leverage;
    private LocalDateTime expirationDate;
    private BigDecimal strikePrice;
    private OptionType optionType;
    private Map<String, Object> metadata;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Symbol() {
        this.metadata = new HashMap<>();
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Symbol(String symbol, AssetType assetType, String exchange) {
        this();
        this.symbol = symbol;
        this.assetType = assetType;
        this.exchange = exchange;
        this.baseSymbol = extractBaseSymbol(symbol);
    }

    // Getters and Setters
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { 
        this.symbol = symbol;
        this.baseSymbol = extractBaseSymbol(symbol);
    }

    public String getBaseSymbol() { return baseSymbol; }
    public void setBaseSymbol(String baseSymbol) { this.baseSymbol = baseSymbol; }

    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public String getQuoteCurrency() { return quoteCurrency; }
    public void setQuoteCurrency(String quoteCurrency) { this.quoteCurrency = quoteCurrency; }

    public BigDecimal getContractSize() { return contractSize; }
    public void setContractSize(BigDecimal contractSize) { this.contractSize = contractSize; }

    public BigDecimal getTickSize() { return tickSize; }
    public void setTickSize(BigDecimal tickSize) { this.tickSize = tickSize; }

    public BigDecimal getMinOrderSize() { return minOrderSize; }
    public void setMinOrderSize(BigDecimal minOrderSize) { this.minOrderSize = minOrderSize; }

    public BigDecimal getMaxOrderSize() { return maxOrderSize; }
    public void setMaxOrderSize(BigDecimal maxOrderSize) { this.maxOrderSize = maxOrderSize; }

    public BigDecimal getLeverage() { return leverage; }
    public void setLeverage(BigDecimal leverage) { this.leverage = leverage; }

    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }

    public BigDecimal getStrikePrice() { return strikePrice; }
    public void setStrikePrice(BigDecimal strikePrice) { this.strikePrice = strikePrice; }

    public OptionType getOptionType() { return optionType; }
    public void setOptionType(OptionType optionType) { this.optionType = optionType; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * Extract base symbol from full symbol
     */
    private String extractBaseSymbol(String symbol) {
        if (symbol == null) return null;
        
        // Remove common suffixes for different asset types
        if (assetType != null) {
            switch (assetType) {
                case CRYPTO_FUTURES:
                case CRYPTO_PERPETUAL:
                    return symbol.replaceAll("(-\\d{4}|-PERP)$", "");
                case OPTIONS:
                case CRYPTO_OPTIONS:
                    return symbol.replaceAll("(\\d{6}[CP]\\d+)$", "");
                case FUTURES:
                    return symbol.replaceAll("(\\d{2}[HMUZ])$", "");
                default:
                    return symbol;
            }
        }
        
        return symbol;
    }

    /**
     * Check if symbol is cryptocurrency
     */
    public boolean isCrypto() {
        return assetType != null && assetType.isCrypto();
    }

    /**
     * Check if symbol is forex
     */
    public boolean isForex() {
        return assetType != null && assetType.isForex();
    }

    /**
     * Check if symbol is derivative
     */
    public boolean isDerivative() {
        return assetType != null && assetType.isDerivative();
    }

    /**
     * Check if symbol is enterprise token
     */
    public boolean isEnterpriseToken() {
        return assetType != null && assetType.isEnterpriseToken();
    }

    /**
     * Check if symbol requires margin
     */
    public boolean requiresMargin() {
        return assetType != null && assetType.requiresMargin();
    }

    /**
     * Check if symbol is 24/7 trading
     */
    public boolean is24x7Trading() {
        return assetType != null && assetType.is24x7Trading();
    }

    /**
     * Get trading session for symbol
     */
    public AssetType.TradingSession getTradingSession() {
        return assetType != null ? assetType.getTradingSession() : AssetType.TradingSession.MARKET_HOURS;
    }

    /**
     * Get settlement period for symbol
     */
    public AssetType.SettlementPeriod getSettlementPeriod() {
        return assetType != null ? assetType.getSettlementPeriod() : AssetType.SettlementPeriod.T_PLUS_2;
    }

    /**
     * Get minimum tick size for symbol
     */
    public BigDecimal getMinimumTickSize() {
        return tickSize != null ? tickSize : 
               (assetType != null ? BigDecimal.valueOf(assetType.getMinimumTickSize()) : BigDecimal.valueOf(0.01));
    }

    /**
     * Get default leverage for symbol
     */
    public BigDecimal getDefaultLeverage() {
        return leverage != null ? leverage : 
               (assetType != null ? BigDecimal.valueOf(assetType.getDefaultLeverage()) : BigDecimal.ONE);
    }

    /**
     * Check if symbol is expired
     */
    public boolean isExpired() {
        return expirationDate != null && expirationDate.isBefore(LocalDateTime.now());
    }

    /**
     * Get days to expiration
     */
    public long getDaysToExpiration() {
        if (expirationDate == null) return Long.MAX_VALUE;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expirationDate);
    }

    /**
     * Add metadata
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }

    /**
     * Get metadata value
     */
    public Object getMetadata(String key) {
        return metadata != null ? metadata.get(key) : null;
    }

    /**
     * Check if symbol matches pattern
     */
    public boolean matchesPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) return true;
        
        // Simple pattern matching (can be enhanced with regex)
        return symbol != null && symbol.toUpperCase().contains(pattern.toUpperCase());
    }

    /**
     * Get display name for symbol
     */
    public String getDisplayName() {
        if (metadata != null && metadata.containsKey("displayName")) {
            return (String) metadata.get("displayName");
        }
        return symbol;
    }

    /**
     * Get symbol description
     */
    public String getDescription() {
        if (metadata != null && metadata.containsKey("description")) {
            return (String) metadata.get("description");
        }
        return assetType != null ? assetType.getDescription() : "";
    }

    @Override
    public String toString() {
        return String.format("Symbol{symbol='%s', assetType=%s, exchange='%s', currency='%s'}", 
                           symbol, assetType, exchange, currency);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Symbol symbol1 = (Symbol) obj;
        return symbol != null ? symbol.equals(symbol1.symbol) : symbol1.symbol == null;
    }

    @Override
    public int hashCode() {
        return symbol != null ? symbol.hashCode() : 0;
    }

    /**
     * Option Type enumeration
     */
    public enum OptionType {
        CALL("C", "Call Option"),
        PUT("P", "Put Option");

        private final String code;
        private final String description;

        OptionType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }

    /**
     * Builder pattern for Symbol creation
     */
    public static class Builder {
        private Symbol symbol;

        public Builder() {
            this.symbol = new Symbol();
        }

        public Builder symbol(String symbol) {
            this.symbol.setSymbol(symbol);
            return this;
        }

        public Builder assetType(AssetType assetType) {
            this.symbol.setAssetType(assetType);
            return this;
        }

        public Builder exchange(String exchange) {
            this.symbol.setExchange(exchange);
            return this;
        }

        public Builder currency(String currency) {
            this.symbol.setCurrency(currency);
            return this;
        }

        public Builder baseCurrency(String baseCurrency) {
            this.symbol.setBaseCurrency(baseCurrency);
            return this;
        }

        public Builder quoteCurrency(String quoteCurrency) {
            this.symbol.setQuoteCurrency(quoteCurrency);
            return this;
        }

        public Builder contractSize(BigDecimal contractSize) {
            this.symbol.setContractSize(contractSize);
            return this;
        }

        public Builder tickSize(BigDecimal tickSize) {
            this.symbol.setTickSize(tickSize);
            return this;
        }

        public Builder minOrderSize(BigDecimal minOrderSize) {
            this.symbol.setMinOrderSize(minOrderSize);
            return this;
        }

        public Builder maxOrderSize(BigDecimal maxOrderSize) {
            this.symbol.setMaxOrderSize(maxOrderSize);
            return this;
        }

        public Builder leverage(BigDecimal leverage) {
            this.symbol.setLeverage(leverage);
            return this;
        }

        public Builder expirationDate(LocalDateTime expirationDate) {
            this.symbol.setExpirationDate(expirationDate);
            return this;
        }

        public Builder strikePrice(BigDecimal strikePrice) {
            this.symbol.setStrikePrice(strikePrice);
            return this;
        }

        public Builder optionType(OptionType optionType) {
            this.symbol.setOptionType(optionType);
            return this;
        }

        public Builder metadata(String key, Object value) {
            this.symbol.addMetadata(key, value);
            return this;
        }

        public Symbol build() {
            return this.symbol;
        }
    }
}
