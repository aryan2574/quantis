package com.quantis.market_data.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

/**
 * Centralized API Configuration
 * 
 * Manages all API configurations and keys from centralized config files
 */
@Configuration
@ConfigurationProperties(prefix = "api")
@PropertySources({
    @PropertySource(value = "classpath:api-config.yml", factory = YamlPropertySourceFactory.class),
    @PropertySource(value = "file:config/api-keys.yml", factory = YamlPropertySourceFactory.class, ignoreResourceNotFound = true)
})
public class ApiConfiguration {
    
    private Keys keys = new Keys();
    private Config config = new Config();
    
    // Getters and Setters
    public Keys getKeys() { return keys; }
    public void setKeys(Keys keys) { this.keys = keys; }
    
    public Config getConfig() { return config; }
    public void setConfig(Config config) { this.config = config; }
    
    /**
     * API Keys Configuration
     */
    public static class Keys {
        private AlphaVantage alphaVantage = new AlphaVantage();
        private ExchangeRate exchangeRate = new ExchangeRate();
        private Polygon polygon = new Polygon();
        
        public AlphaVantage getAlphaVantage() { return alphaVantage; }
        public void setAlphaVantage(AlphaVantage alphaVantage) { this.alphaVantage = alphaVantage; }
        
        public ExchangeRate getExchangeRate() { return exchangeRate; }
        public void setExchangeRate(ExchangeRate exchangeRate) { this.exchangeRate = exchangeRate; }
        
        public Polygon getPolygon() { return polygon; }
        public void setPolygon(Polygon polygon) { this.polygon = polygon; }
        
        public static class AlphaVantage {
            private String apiKey;
            private String baseUrl;
            private RateLimit rateLimit = new RateLimit();
            private Endpoints endpoints = new Endpoints();
            
            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }
            
            public String getBaseUrl() { return baseUrl; }
            public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
            
            public RateLimit getRateLimit() { return rateLimit; }
            public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
            
            public Endpoints getEndpoints() { return endpoints; }
            public void setEndpoints(Endpoints endpoints) { this.endpoints = endpoints; }
            
            public static class RateLimit {
                private int callsPerMinute = 5;
                private int callsPerDay = 500;
                
                public int getCallsPerMinute() { return callsPerMinute; }
                public void setCallsPerMinute(int callsPerMinute) { this.callsPerMinute = callsPerMinute; }
                
                public int getCallsPerDay() { return callsPerDay; }
                public void setCallsPerDay(int callsPerDay) { this.callsPerDay = callsPerDay; }
            }
            
            public static class Endpoints {
                private String digitalCurrency = "/query?function=DIGITAL_CURRENCY_INTRADAY";
                private String forex = "/query?function=FX_INTRADAY";
                private String timeSeries = "/query?function=TIME_SERIES_INTRADAY";
                private String listingStatus = "/query?function=LISTING_STATUS";
                
                public String getDigitalCurrency() { return digitalCurrency; }
                public void setDigitalCurrency(String digitalCurrency) { this.digitalCurrency = digitalCurrency; }
                
                public String getForex() { return forex; }
                public void setForex(String forex) { this.forex = forex; }
                
                public String getTimeSeries() { return timeSeries; }
                public void setTimeSeries(String timeSeries) { this.timeSeries = timeSeries; }
                
                public String getListingStatus() { return listingStatus; }
                public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }
            }
        }
        
        public static class ExchangeRate {
            private String apiKey;
            private String baseUrl;
            private RateLimit rateLimit = new RateLimit();
            private Endpoints endpoints = new Endpoints();
            
            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }
            
            public String getBaseUrl() { return baseUrl; }
            public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
            
            public RateLimit getRateLimit() { return rateLimit; }
            public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
            
            public Endpoints getEndpoints() { return endpoints; }
            public void setEndpoints(Endpoints endpoints) { this.endpoints = endpoints; }
            
            public static class RateLimit {
                private int callsPerMinute = 1000;
                private int callsPerDay = 100000;
                
                public int getCallsPerMinute() { return callsPerMinute; }
                public void setCallsPerMinute(int callsPerMinute) { this.callsPerMinute = callsPerMinute; }
                
                public int getCallsPerDay() { return callsPerDay; }
                public void setCallsPerDay(int callsPerDay) { this.callsPerDay = callsPerDay; }
            }
            
            public static class Endpoints {
                private String latest = "/latest";
                private String historical = "/history";
                private String convert = "/pair";
                private String supportedCurrencies = "/codes";
                
                public String getLatest() { return latest; }
                public void setLatest(String latest) { this.latest = latest; }
                
                public String getHistorical() { return historical; }
                public void setHistorical(String historical) { this.historical = historical; }
                
                public String getConvert() { return convert; }
                public void setConvert(String convert) { this.convert = convert; }
                
                public String getSupportedCurrencies() { return supportedCurrencies; }
                public void setSupportedCurrencies(String supportedCurrencies) { this.supportedCurrencies = supportedCurrencies; }
            }
        }
        
        public static class Polygon {
            private String apiKey;
            private String baseUrl;
            private RateLimit rateLimit = new RateLimit();
            private Endpoints endpoints = new Endpoints();
            
            public String getApiKey() { return apiKey; }
            public void setApiKey(String apiKey) { this.apiKey = apiKey; }
            
            public String getBaseUrl() { return baseUrl; }
            public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
            
            public RateLimit getRateLimit() { return rateLimit; }
            public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
            
            public Endpoints getEndpoints() { return endpoints; }
            public void setEndpoints(Endpoints endpoints) { this.endpoints = endpoints; }
            
            public static class RateLimit {
                private int callsPerMinute = 1000;
                private int callsPerDay = 100000;
                
                public int getCallsPerMinute() { return callsPerMinute; }
                public void setCallsPerMinute(int callsPerMinute) { this.callsPerMinute = callsPerMinute; }
                
                public int getCallsPerDay() { return callsPerDay; }
                public void setCallsPerDay(int callsPerDay) { this.callsPerDay = callsPerDay; }
            }
            
            public static class Endpoints {
                private String stocks = "/v2/aggs/ticker";
                private String forex = "/v1/last/forex";
                private String crypto = "/v1/last/crypto";
                private String options = "/v3/reference/options/contracts";
                private String tickers = "/v3/reference/tickers";
                private String marketStatus = "/v1/marketstatus/now";
                
                public String getStocks() { return stocks; }
                public void setStocks(String stocks) { this.stocks = stocks; }
                
                public String getForex() { return forex; }
                public void setForex(String forex) { this.forex = forex; }
                
                public String getCrypto() { return crypto; }
                public void setCrypto(String crypto) { this.crypto = crypto; }
                
                public String getOptions() { return options; }
                public void setOptions(String options) { this.options = options; }
                
                public String getTickers() { return tickers; }
                public void setTickers(String tickers) { this.tickers = tickers; }
                
                public String getMarketStatus() { return marketStatus; }
                public void setMarketStatus(String marketStatus) { this.marketStatus = marketStatus; }
            }
        }
    }
    
    /**
     * API Configuration
     */
    public static class Config {
        private AlphaVantage alphaVantage = new AlphaVantage();
        
        public AlphaVantage getAlphaVantage() { return alphaVantage; }
        public void setAlphaVantage(AlphaVantage alphaVantage) { this.alphaVantage = alphaVantage; }
        
        public static class AlphaVantage {
            private String baseUrl = "https://www.alphavantage.co";
            private int timeout = 10000;
            private int retryAttempts = 3;
            private int retryDelay = 1000;
            private Endpoints endpoints = new Endpoints();
            private RateLimits rateLimits = new RateLimits();
            
            public String getBaseUrl() { return baseUrl; }
            public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
            
            public int getTimeout() { return timeout; }
            public void setTimeout(int timeout) { this.timeout = timeout; }
            
            public int getRetryAttempts() { return retryAttempts; }
            public void setRetryAttempts(int retryAttempts) { this.retryAttempts = retryAttempts; }
            
            public int getRetryDelay() { return retryDelay; }
            public void setRetryDelay(int retryDelay) { this.retryDelay = retryDelay; }
            
            public Endpoints getEndpoints() { return endpoints; }
            public void setEndpoints(Endpoints endpoints) { this.endpoints = endpoints; }
            
            public RateLimits getRateLimits() { return rateLimits; }
            public void setRateLimits(RateLimits rateLimits) { this.rateLimits = rateLimits; }
            
            public static class Endpoints {
                private String digitalCurrency = "/query?function=DIGITAL_CURRENCY_INTRADAY";
                private String forex = "/query?function=FX_INTRADAY";
                private String timeSeries = "/query?function=TIME_SERIES_INTRADAY";
                private String listingStatus = "/query?function=LISTING_STATUS";
                private String currencyExchangeRate = "/query?function=CURRENCY_EXCHANGE_RATE";
                
                public String getDigitalCurrency() { return digitalCurrency; }
                public void setDigitalCurrency(String digitalCurrency) { this.digitalCurrency = digitalCurrency; }
                
                public String getForex() { return forex; }
                public void setForex(String forex) { this.forex = forex; }
                
                public String getTimeSeries() { return timeSeries; }
                public void setTimeSeries(String timeSeries) { this.timeSeries = timeSeries; }
                
                public String getListingStatus() { return listingStatus; }
                public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }
                
                public String getCurrencyExchangeRate() { return currencyExchangeRate; }
                public void setCurrencyExchangeRate(String currencyExchangeRate) { this.currencyExchangeRate = currencyExchangeRate; }
            }
            
            public static class RateLimits {
                private FreeTier freeTier = new FreeTier();
                private PremiumTier premiumTier = new PremiumTier();
                
                public FreeTier getFreeTier() { return freeTier; }
                public void setFreeTier(FreeTier freeTier) { this.freeTier = freeTier; }
                
                public PremiumTier getPremiumTier() { return premiumTier; }
                public void setPremiumTier(PremiumTier premiumTier) { this.premiumTier = premiumTier; }
                
                public static class FreeTier {
                    private int callsPerMinute = 5;
                    private int callsPerDay = 500;
                    
                    public int getCallsPerMinute() { return callsPerMinute; }
                    public void setCallsPerMinute(int callsPerMinute) { this.callsPerMinute = callsPerMinute; }
                    
                    public int getCallsPerDay() { return callsPerDay; }
                    public void setCallsPerDay(int callsPerDay) { this.callsPerDay = callsPerDay; }
                }
                
                public static class PremiumTier {
                    private int callsPerMinute = 1200;
                    private int callsPerDay = 1000000;
                    
                    public int getCallsPerMinute() { return callsPerMinute; }
                    public void setCallsPerMinute(int callsPerMinute) { this.callsPerMinute = callsPerMinute; }
                    
                    public int getCallsPerDay() { return callsPerDay; }
                    public void setCallsPerDay(int callsPerDay) { this.callsPerDay = callsPerDay; }
                }
            }
        }
    }
}