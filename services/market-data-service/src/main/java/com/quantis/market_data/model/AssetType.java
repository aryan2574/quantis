package com.quantis.market_data.model;

/**
 * Comprehensive Asset Type System
 * 
 * Supports:
 * - Traditional Securities (Stocks, Bonds, ETFs)
 * - Cryptocurrencies (Spot, Futures, Options)
 * - Forex (Major/Minor/Exotic pairs)
 * - Derivatives (Futures, Options, Swaps)
 * - Enterprise Tokens (Private securities, Tokenized assets)
 */
public enum AssetType {
    // Traditional Securities
    STOCK("STOCK", "Equity", "Traditional company shares"),
    STOCKS("STOCKS", "Stocks", "Traditional company shares (alias)"),
    BOND("BOND", "Fixed Income", "Government and corporate bonds"),
    ETF("ETF", "Exchange Traded Fund", "Index and sector ETFs"),
    MUTUAL_FUND("MUTUAL_FUND", "Mutual Fund", "Actively managed funds"),
    REIT("REIT", "Real Estate Investment Trust", "Real estate securities"),
    
    // Cryptocurrencies
    CRYPTO_SPOT("CRYPTO_SPOT", "Cryptocurrency Spot", "Direct cryptocurrency trading"),
    CRYPTO_FUTURES("CRYPTO_FUTURES", "Crypto Futures", "Cryptocurrency futures contracts"),
    CRYPTO_OPTIONS("CRYPTO_OPTIONS", "Crypto Options", "Cryptocurrency options contracts"),
    CRYPTO_PERPETUAL("CRYPTO_PERPETUAL", "Crypto Perpetual", "Perpetual cryptocurrency contracts"),
    CRYPTO_MARGIN("CRYPTO_MARGIN", "Crypto Margin", "Margin cryptocurrency trading"),
    
    // Forex
    FOREX("FOREX", "Forex", "Foreign exchange (alias)"),
    FOREX_SPOT("FOREX_SPOT", "Forex Spot", "Spot foreign exchange"),
    FOREX_FORWARD("FOREX_FORWARD", "Forex Forward", "Forward foreign exchange contracts"),
    FOREX_SWAP("FOREX_SWAP", "Forex Swap", "Foreign exchange swaps"),
    FOREX_OPTIONS("FOREX_OPTIONS", "Forex Options", "Foreign exchange options"),
    
    // Derivatives
    FUTURES("FUTURES", "Futures", "Commodity and financial futures"),
    OPTIONS("OPTIONS", "Options", "Stock and index options"),
    WARRANTS("WARRANTS", "Warrants", "Equity warrants"),
    SWAPS("SWAPS", "Swaps", "Interest rate and credit swaps"),
    CFDS("CFDS", "Contract for Difference", "CFD trading"),
    
    // Enterprise Tokens
    ENTERPRISE_TOKENS("ENTERPRISE_TOKENS", "Enterprise Tokens", "Enterprise blockchain tokens (alias)"),
    PRIVATE_EQUITY("PRIVATE_EQUITY", "Private Equity", "Private company shares"),
    TOKENIZED_ASSET("TOKENIZED_ASSET", "Tokenized Asset", "Blockchain-tokenized real assets"),
    SECURITY_TOKEN("SECURITY_TOKEN", "Security Token", "Regulated security tokens"),
    UTILITY_TOKEN("UTILITY_TOKEN", "Utility Token", "Platform utility tokens"),
    STABLECOIN("STABLECOIN", "Stablecoin", "Price-stable cryptocurrencies"),
    
    // Commodities
    COMMODITY_SPOT("COMMODITY_SPOT", "Commodity Spot", "Physical commodity trading"),
    COMMODITY_FUTURES("COMMODITY_FUTURES", "Commodity Futures", "Commodity futures contracts"),
    
    // Indices
    INDEX("INDEX", "Index", "Market indices"),
    INDEX_FUTURES("INDEX_FUTURES", "Index Futures", "Index futures contracts"),
    INDEX_OPTIONS("INDEX_OPTIONS", "Index Options", "Index options contracts");

    private final String code;
    private final String displayName;
    private final String description;

    AssetType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    /**
     * Check if asset type is cryptocurrency
     */
    public boolean isCrypto() {
        return this.name().startsWith("CRYPTO_");
    }

    /**
     * Check if asset type is forex
     */
    public boolean isForex() {
        return this == FOREX || this.name().startsWith("FOREX_");
    }

    /**
     * Check if asset type is derivative
     */
    public boolean isDerivative() {
        return this == FUTURES || this == OPTIONS || this == WARRANTS || 
               this == SWAPS || this == CFDS || this.name().contains("FUTURES") ||
               this.name().contains("OPTIONS");
    }

    /**
     * Check if asset type is enterprise token
     */
    public boolean isEnterpriseToken() {
        return this == ENTERPRISE_TOKENS || this == PRIVATE_EQUITY || this == TOKENIZED_ASSET || 
               this == SECURITY_TOKEN || this == UTILITY_TOKEN || this == STABLECOIN;
    }

    /**
     * Check if asset type requires margin
     */
    public boolean requiresMargin() {
        return isDerivative() || isForex() || this == CRYPTO_MARGIN || 
               this == CRYPTO_FUTURES || this == CRYPTO_PERPETUAL;
    }

    /**
     * Check if asset type is 24/7 trading
     */
    public boolean is24x7Trading() {
        return isCrypto() || this == STABLECOIN;
    }

    /**
     * Get trading session for asset type
     */
    public TradingSession getTradingSession() {
        if (is24x7Trading()) {
            return TradingSession.TWENTY_FOUR_SEVEN;
        } else if (isForex()) {
            return TradingSession.FOREX_HOURS;
        } else {
            return TradingSession.MARKET_HOURS;
        }
    }

    /**
     * Get settlement period for asset type
     */
    public SettlementPeriod getSettlementPeriod() {
        switch (this) {
            case STOCK:
            case ETF:
            case REIT:
                return SettlementPeriod.T_PLUS_2;
            case BOND:
                return SettlementPeriod.T_PLUS_1;
            case CRYPTO_SPOT:
            case CRYPTO_MARGIN:
                return SettlementPeriod.T_PLUS_0;
            case FOREX_SPOT:
                return SettlementPeriod.T_PLUS_2;
            case FUTURES:
            case CRYPTO_FUTURES:
                return SettlementPeriod.DAILY;
            case OPTIONS:
            case CRYPTO_OPTIONS:
                return SettlementPeriod.T_PLUS_1;
            default:
                return SettlementPeriod.T_PLUS_2;
        }
    }

    /**
     * Get minimum tick size for asset type
     */
    public double getMinimumTickSize() {
        switch (this) {
            case STOCK:
                return 0.01;
            case CRYPTO_SPOT:
            case CRYPTO_MARGIN:
                return 0.00000001; // 8 decimal places
            case FOREX_SPOT:
                return 0.00001; // 5 decimal places
            case FUTURES:
                return 0.25;
            case OPTIONS:
                return 0.01;
            default:
                return 0.01;
        }
    }

    /**
     * Get default leverage for asset type
     */
    public double getDefaultLeverage() {
        switch (this) {
            case CRYPTO_MARGIN:
                return 2.0;
            case CRYPTO_FUTURES:
            case CRYPTO_PERPETUAL:
                return 10.0;
            case FOREX:
            case FOREX_SPOT:
                return 50.0;
            case FUTURES:
                return 10.0;
            case CFDS:
                return 20.0;
            default:
                return 1.0;
        }
    }

    /**
     * Get timezone for asset type
     */
    public String getTimezone() {
        switch (this) {
            case STOCK:
            case STOCKS:
            case ETF:
            case REIT:
                return "America/New_York";
            case FOREX:
            case FOREX_SPOT:
                return "UTC";
            case CRYPTO_SPOT:
            case CRYPTO_FUTURES:
            case CRYPTO_PERPETUAL:
                return "UTC";
            default:
                return "UTC";
        }
    }

    /**
     * Get data refresh interval in milliseconds
     */
    public long getDataRefreshInterval() {
        switch (this) {
            case CRYPTO_SPOT:
            case CRYPTO_FUTURES:
            case CRYPTO_PERPETUAL:
                return 1000; // 1 second
            case FOREX:
            case FOREX_SPOT:
                return 500; // 0.5 seconds
            case STOCK:
            case STOCKS:
                return 1000; // 1 second
            case FUTURES:
                return 500; // 0.5 seconds
            case OPTIONS:
                return 1000; // 1 second
            case ENTERPRISE_TOKENS:
                return 5000; // 5 seconds
            default:
                return 1000; // 1 second
        }
    }

    public enum TradingSession {
        MARKET_HOURS("Market Hours", "9:30 AM - 4:00 PM EST"),
        FOREX_HOURS("Forex Hours", "24/5 Sunday 5 PM - Friday 5 PM EST"),
        TWENTY_FOUR_SEVEN("24/7", "Continuous trading");

        private final String name;
        private final String hours;

        TradingSession(String name, String hours) {
            this.name = name;
            this.hours = hours;
        }

        public String getName() { return name; }
        public String getHours() { return hours; }
    }

    public enum SettlementPeriod {
        T_PLUS_0("T+0", "Same day settlement"),
        T_PLUS_1("T+1", "Next day settlement"),
        T_PLUS_2("T+2", "Two day settlement"),
        DAILY("Daily", "Daily settlement");

        private final String code;
        private final String description;

        SettlementPeriod(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
}
