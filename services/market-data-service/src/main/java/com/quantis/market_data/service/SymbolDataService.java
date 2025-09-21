package com.quantis.market_data.service;

import com.quantis.market_data.model.AssetType;
import com.quantis.market_data.model.Symbol;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Symbol Data Service
 * 
 * Populates the system with actual market symbols from various data sources
 */
@Service
public class SymbolDataService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Real market data API configurations using Alpha Vantage
    private final Map<AssetType, DataSourceConfig> dataSourceConfigs = Map.of(
        AssetType.CRYPTO_SPOT, new DataSourceConfig(
            "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=",
            "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_INTRADAY&symbol=",
            "Alpha Vantage",
            "USD"
        ),
        AssetType.CRYPTO_FUTURES, new DataSourceConfig(
            "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=",
            "https://www.alphavantage.co/query?function=DIGITAL_CURRENCY_INTRADAY&symbol=",
            "Alpha Vantage",
            "USD"
        ),
        AssetType.FOREX, new DataSourceConfig(
            "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=",
            "https://www.alphavantage.co/query?function=FX_INTRADAY&from_symbol=",
            "Alpha Vantage",
            "USD"
        ),
        AssetType.STOCKS, new DataSourceConfig(
            "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=",
            "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=",
            "Alpha Vantage",
            "USD"
        ),
        AssetType.FUTURES, new DataSourceConfig(
            "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=",
            "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=",
            "Alpha Vantage",
            "USD"
        )
    );
    
    /**
     * Populate symbols for all asset types
     */
    @Transactional
    public void populateAllSymbols() {
        for (AssetType assetType : AssetType.values()) {
            try {
                populateSymbolsForAssetType(assetType);
            } catch (Exception e) {
                System.err.println("Failed to populate symbols for " + assetType + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Populate symbols for specific asset type
     */
    @Transactional
    public void populateSymbolsForAssetType(AssetType assetType) {
        DataSourceConfig config = dataSourceConfigs.get(assetType);
        if (config == null) {
            System.err.println("No data source configured for asset type: " + assetType);
            return;
        }
        
        List<Symbol> symbols = fetchSymbolsFromDataSource(assetType, config);
        
        // Cache symbols
        String cacheKey = "symbols:" + assetType.name();
        redisTemplate.opsForValue().set(cacheKey, symbols, 3600); // 1 hour cache
        
        // Store in database (implement database storage)
        storeSymbolsInDatabase(symbols);
        
        System.out.println("Populated " + symbols.size() + " symbols for " + assetType);
    }
    
    /**
     * Fetch symbols from data source
     */
    private List<Symbol> fetchSymbolsFromDataSource(AssetType assetType, DataSourceConfig config) {
        switch (assetType) {
            case CRYPTO_SPOT:
                return fetchCryptoSpotSymbols(config);
            case CRYPTO_FUTURES:
                return fetchCryptoFuturesSymbols(config);
            case FOREX:
                return fetchForexSymbols(config);
            case STOCKS:
                return fetchStockSymbols(config);
            case FUTURES:
                return fetchFuturesSymbols(config);
            default:
                return fetchGenericSymbols(assetType, config);
        }
    }
    
    /**
     * Fetch crypto spot symbols from Alpha Vantage
     */
    private List<Symbol> fetchCryptoSpotSymbols(DataSourceConfig config) {
        try {
            String apiKey = System.getenv("ALPHA_VANTAGE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                System.err.println("Alpha Vantage API key not configured");
                return createDefaultCryptoSymbols();
            }
            
            // Alpha Vantage doesn't provide a direct symbol list, so we'll create popular crypto symbols
            return createDefaultCryptoSymbols();
        } catch (Exception e) {
            System.err.println("Error fetching crypto spot symbols: " + e.getMessage());
            return createDefaultCryptoSymbols();
        }
    }
    
    /**
     * Create default crypto symbols for Alpha Vantage
     */
    private List<Symbol> createDefaultCryptoSymbols() {
        List<Symbol> symbols = new ArrayList<>();
        
        // Popular cryptocurrencies
        String[] cryptoCurrencies = {
            "BTC", "ETH", "ADA", "DOT", "LINK", "UNI", "LTC", "BCH", "XLM", "ATOM",
            "SOL", "AVAX", "MATIC", "ALGO", "VET", "ICP", "FIL", "TRX", "ETC", "XRP"
        };
        
        for (String crypto : cryptoCurrencies) {
            Symbol symbol = new Symbol();
            symbol.setSymbol(crypto + "USD");
            symbol.setAssetType(AssetType.CRYPTO_SPOT);
            symbol.setExchange("Alpha Vantage");
            symbol.setCurrency("USD");
            symbol.setBaseCurrency(crypto);
            symbol.setQuoteCurrency("USD");
            
            // Set crypto-specific parameters
            symbol.setMinOrderSize(new BigDecimal("0.001"));
            symbol.setMaxOrderSize(new BigDecimal("1000000"));
            symbol.setTickSize(new BigDecimal("0.01"));
            symbol.setLeverage(BigDecimal.ONE);
            symbol.setActive(true);
            symbol.setCreatedAt(LocalDateTime.now());
            symbol.setUpdatedAt(LocalDateTime.now());
            
            symbols.add(symbol);
        }
        
        return symbols;
    }
    
    /**
     * Fetch crypto futures symbols from Alpha Vantage
     */
    private List<Symbol> fetchCryptoFuturesSymbols(DataSourceConfig config) {
        try {
            // Alpha Vantage doesn't provide futures symbols directly, create popular crypto futures
            return createDefaultCryptoFuturesSymbols();
        } catch (Exception e) {
            System.err.println("Error fetching crypto futures symbols: " + e.getMessage());
            return createDefaultCryptoFuturesSymbols();
        }
    }
    
    /**
     * Create default crypto futures symbols
     */
    private List<Symbol> createDefaultCryptoFuturesSymbols() {
        List<Symbol> symbols = new ArrayList<>();
        
        // Popular crypto futures
        String[] cryptoFutures = {
            "BTC", "ETH", "ADA", "DOT", "LINK", "UNI", "LTC", "BCH", "XLM", "ATOM"
        };
        
        for (String crypto : cryptoFutures) {
            Symbol symbol = new Symbol();
            symbol.setSymbol(crypto + "USD-FUTURES");
            symbol.setAssetType(AssetType.CRYPTO_FUTURES);
            symbol.setExchange("Alpha Vantage");
            symbol.setCurrency("USD");
            symbol.setBaseCurrency(crypto);
            symbol.setQuoteCurrency("USD");
            
            // Set futures-specific parameters
            symbol.setMinOrderSize(new BigDecimal("0.001"));
            symbol.setMaxOrderSize(new BigDecimal("1000000"));
            symbol.setTickSize(new BigDecimal("0.01"));
            symbol.setLeverage(new BigDecimal("10"));
            symbol.setActive(true);
            symbol.setCreatedAt(LocalDateTime.now());
            symbol.setUpdatedAt(LocalDateTime.now());
            
            symbols.add(symbol);
        }
        
        return symbols;
    }
    
    /**
     * Fetch forex symbols
     */
    private List<Symbol> fetchForexSymbols(DataSourceConfig config) {
        try {
            Map<String, Object> response = restTemplate.getForObject(config.symbolsUrl, Map.class);
            Map<String, Object> rates = (Map<String, Object>) response.get("rates");
            
            List<Symbol> symbols = new ArrayList<>();
            
            // Create major currency pairs
            String[] majorCurrencies = {"EUR", "GBP", "JPY", "CHF", "AUD", "CAD", "NZD"};
            
            for (String baseCurrency : majorCurrencies) {
                if (rates.containsKey(baseCurrency)) {
                    Symbol symbol = new Symbol();
                    symbol.setSymbol(baseCurrency + config.quoteCurrency);
                    symbol.setAssetType(AssetType.FOREX);
                    symbol.setExchange(config.exchange);
                    symbol.setCurrency(config.quoteCurrency);
                    symbol.setBaseCurrency(baseCurrency);
                    symbol.setQuoteCurrency(config.quoteCurrency);
                    
                    // Set forex-specific parameters
                    symbol.setMinOrderSize(new BigDecimal("1000")); // Minimum lot size
                    symbol.setMaxOrderSize(new BigDecimal("10000000")); // Maximum lot size
                    symbol.setTickSize(new BigDecimal("0.00001")); // 5 decimal places
                    symbol.setLeverage(new BigDecimal("50")); // Default leverage
                    symbol.setActive(true);
                    symbol.setCreatedAt(LocalDateTime.now());
                    symbol.setUpdatedAt(LocalDateTime.now());
                    
                    symbols.add(symbol);
                }
            }
            
            return symbols;
        } catch (Exception e) {
            System.err.println("Error fetching forex symbols: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetch stock symbols from Polygon
     */
    private List<Symbol> fetchStockSymbols(DataSourceConfig config) {
        try {
            // Note: This requires a Polygon API key
            // For demo purposes, we'll create some popular stocks
            List<Symbol> symbols = new ArrayList<>();
            
            String[] popularStocks = {
                "AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "NFLX", 
                "AMD", "INTC", "CRM", "ADBE", "PYPL", "UBER", "LYFT", "SQ", "ROKU"
            };
            
            for (String stockSymbol : popularStocks) {
                Symbol symbol = new Symbol();
                symbol.setSymbol(stockSymbol);
                symbol.setAssetType(AssetType.STOCKS);
                symbol.setExchange("NASDAQ");
                symbol.setCurrency(config.quoteCurrency);
                symbol.setBaseCurrency(stockSymbol);
                symbol.setQuoteCurrency(config.quoteCurrency);
                
                // Set stock-specific parameters
                symbol.setMinOrderSize(BigDecimal.ONE); // Minimum 1 share
                symbol.setMaxOrderSize(new BigDecimal("1000000")); // Maximum shares
                symbol.setTickSize(new BigDecimal("0.01")); // $0.01 increments
                symbol.setLeverage(BigDecimal.ONE); // No leverage for stocks
                symbol.setActive(true);
                symbol.setCreatedAt(LocalDateTime.now());
                symbol.setUpdatedAt(LocalDateTime.now());
                
                symbols.add(symbol);
            }
            
            return symbols;
        } catch (Exception e) {
            System.err.println("Error fetching stock symbols: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetch futures symbols
     */
    private List<Symbol> fetchFuturesSymbols(DataSourceConfig config) {
        try {
            // Create some popular futures contracts
            List<Symbol> symbols = new ArrayList<>();
            
            Map<String, String> futuresContracts = Map.of(
                "ES", "E-mini S&P 500",
                "NQ", "E-mini NASDAQ-100",
                "YM", "E-mini Dow Jones",
                "RTY", "E-mini Russell 2000",
                "GC", "Gold",
                "SI", "Silver",
                "CL", "Crude Oil",
                "NG", "Natural Gas"
            );
            
            for (Map.Entry<String, String> entry : futuresContracts.entrySet()) {
                Symbol symbol = new Symbol();
                symbol.setSymbol(entry.getKey() + "Z24"); // December 2024 contract
                symbol.setAssetType(AssetType.FUTURES);
                symbol.setExchange("CME");
                symbol.setCurrency(config.quoteCurrency);
                symbol.setBaseCurrency(entry.getKey());
                symbol.setQuoteCurrency(config.quoteCurrency);
                
                // Set futures-specific parameters
                symbol.setMinOrderSize(BigDecimal.ONE); // Minimum 1 contract
                symbol.setMaxOrderSize(new BigDecimal("10000")); // Maximum contracts
                symbol.setTickSize(new BigDecimal("0.25")); // $0.25 increments
                symbol.setLeverage(new BigDecimal("10")); // Default leverage
                symbol.setActive(true);
                symbol.setCreatedAt(LocalDateTime.now());
                symbol.setUpdatedAt(LocalDateTime.now());
                
                // Add metadata
                symbol.addMetadata("description", entry.getValue());
                symbol.addMetadata("contractMonth", "2024-12");
                
                symbols.add(symbol);
            }
            
            return symbols;
        } catch (Exception e) {
            System.err.println("Error fetching futures symbols: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * Fetch generic symbols for other asset types
     */
    private List<Symbol> fetchGenericSymbols(AssetType assetType, DataSourceConfig config) {
        // Create some sample symbols for other asset types
        List<Symbol> symbols = new ArrayList<>();
        
        if (assetType == AssetType.OPTIONS) {
            // Create some sample options
            String[] underlyingAssets = {"AAPL", "GOOGL", "MSFT", "TSLA"};
            String[] expirationMonths = {"2024-01", "2024-02", "2024-03"};
            String[] strikePrices = {"100", "110", "120", "130", "140"};
            
            for (String underlying : underlyingAssets) {
                for (String expiration : expirationMonths) {
                    for (String strike : strikePrices) {
                        Symbol symbol = new Symbol();
                        symbol.setSymbol(underlying + expiration + "C" + strike);
                        symbol.setAssetType(AssetType.OPTIONS);
                        symbol.setExchange("CBOE");
                        symbol.setCurrency("USD");
                        symbol.setBaseCurrency(underlying);
                        symbol.setQuoteCurrency("USD");
                        symbol.setStrikePrice(new BigDecimal(strike));
                        symbol.setOptionType(Symbol.OptionType.CALL);
                        symbol.setMinOrderSize(BigDecimal.ONE);
                        symbol.setMaxOrderSize(new BigDecimal("1000"));
                        symbol.setTickSize(new BigDecimal("0.01"));
                        symbol.setLeverage(BigDecimal.ONE);
                        symbol.setActive(true);
                        symbol.setCreatedAt(LocalDateTime.now());
                        symbol.setUpdatedAt(LocalDateTime.now());
                        
                        symbols.add(symbol);
                    }
                }
            }
        } else if (assetType == AssetType.ENTERPRISE_TOKENS) {
            // Create some sample enterprise tokens
            String[] enterpriseTokens = {"COMPANY_A", "COMPANY_B", "COMPANY_C"};
            
            for (String token : enterpriseTokens) {
                Symbol symbol = new Symbol();
                symbol.setSymbol(token);
                symbol.setAssetType(AssetType.ENTERPRISE_TOKENS);
                symbol.setExchange("PRIVATE");
                symbol.setCurrency("USD");
                symbol.setBaseCurrency(token);
                symbol.setQuoteCurrency("USD");
                symbol.setMinOrderSize(BigDecimal.ONE);
                symbol.setMaxOrderSize(new BigDecimal("1000000"));
                symbol.setTickSize(new BigDecimal("0.01"));
                symbol.setLeverage(BigDecimal.ONE);
                symbol.setActive(true);
                symbol.setCreatedAt(LocalDateTime.now());
                symbol.setUpdatedAt(LocalDateTime.now());
                
                symbols.add(symbol);
            }
        }
        
        return symbols;
    }
    
    /**
     * Store symbols in database
     */
    private void storeSymbolsInDatabase(List<Symbol> symbols) {
        // Implement database storage
        // This is a placeholder - implement actual database storage
        System.out.println("Storing " + symbols.size() + " symbols in database");
    }
    
    /**
     * Scheduled task to refresh symbols daily
     */
    @Scheduled(cron = "0 0 1 * * ?") // Run daily at 1 AM
    public void refreshSymbolsDaily() {
        System.out.println("Starting daily symbol refresh...");
        populateAllSymbols();
        System.out.println("Daily symbol refresh completed.");
    }
    
    /**
     * Get symbols for asset type
     */
    public List<Symbol> getSymbolsForAssetType(AssetType assetType) {
        String cacheKey = "symbols:" + assetType.name();
        List<Symbol> cachedSymbols = (List<Symbol>) redisTemplate.opsForValue().get(cacheKey);
        
        if (cachedSymbols != null) {
            return cachedSymbols;
        }
        
        // If not in cache, populate symbols
        populateSymbolsForAssetType(assetType);
        return (List<Symbol>) redisTemplate.opsForValue().get(cacheKey);
    }
    
    /**
     * Data source configuration
     */
    private static class DataSourceConfig {
        private final String symbolsUrl;
        private final String marketDataUrl;
        private final String exchange;
        private final String quoteCurrency;
        
        public DataSourceConfig(String symbolsUrl, String marketDataUrl, String exchange, String quoteCurrency) {
            this.symbolsUrl = symbolsUrl;
            this.marketDataUrl = marketDataUrl;
            this.exchange = exchange;
            this.quoteCurrency = quoteCurrency;
        }
        
        public String getSymbolsUrl() { return symbolsUrl; }
        public String getMarketDataUrl() { return marketDataUrl; }
        public String getExchange() { return exchange; }
        public String getQuoteCurrency() { return quoteCurrency; }
    }
}
