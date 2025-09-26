package com.quantis.trading_engine.jni;

/**
 * JNI interface to C++ trading engine
 */
public class TradingEngineJNI {
    
    private static boolean nativeLibraryLoaded = false;
    
    static {
        try {
            // Check if we're running in Docker (no native library available)
            String environment = System.getProperty("spring.profiles.active", "");
            if (environment.contains("docker")) {
                System.out.println("Running in Docker environment - using mock JNI implementation");
                nativeLibraryLoaded = false;
                return;
            }
            
            // Load the library from the classpath
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                // Load from classpath resources using proper URL handling
                String dllPath = TradingEngineJNI.class.getResource("/lib/tradingenginejni.dll").getPath();
                // Handle URL encoding (replace %20 with spaces, etc.)
                dllPath = dllPath.replaceAll("%20", " ");
                System.load(dllPath);
                nativeLibraryLoaded = true;
            } else {
                // For Linux/Mac, load the .so file
                String soPath = TradingEngineJNI.class.getResource("/lib/libtradingenginejni.so").getPath();
                soPath = soPath.replaceAll("%20", " ");
                System.load(soPath);
                nativeLibraryLoaded = true;
            }
        } catch (UnsatisfiedLinkError | NullPointerException e) {
            System.err.println("Failed to load tradingenginejni library: " + e.getMessage());
            System.err.println("Using mock implementation for development");
            nativeLibraryLoaded = false;
        }
    }
    
    /**
     * Add an order to the C++ order book
     */
    public boolean addOrder(String orderId, String userId, String symbol, 
                           String side, long quantity, double price) {
        if (nativeLibraryLoaded) {
            return addOrderNative(orderId, userId, symbol, side, quantity, price);
        } else {
            // Mock implementation
            System.out.println("Mock: Adding order " + orderId + " for " + symbol + " " + side + " " + quantity + "@" + price);
            return true;
        }
    }
    
    private native boolean addOrderNative(String orderId, String userId, String symbol, 
                                         String side, long quantity, double price);
    
    /**
     * Remove an order from the C++ order book
     */
    public boolean removeOrder(String orderId) {
        if (nativeLibraryLoaded) {
            return removeOrderNative(orderId);
        } else {
            // Mock implementation
            System.out.println("Mock: Removing order " + orderId);
            return true;
        }
    }
    
    private native boolean removeOrderNative(String orderId);
    
    /**
     * Update an existing order in the C++ order book
     */
    public boolean updateOrder(String orderId, String userId, String symbol,
                              String side, long quantity, double price) {
        if (nativeLibraryLoaded) {
            return updateOrderNative(orderId, userId, symbol, side, quantity, price);
        } else {
            // Mock implementation
            System.out.println("Mock: Updating order " + orderId + " for " + symbol + " " + side + " " + quantity + "@" + price);
            return true;
        }
    }
    
    private native boolean updateOrderNative(String orderId, String userId, String symbol,
                                           String side, long quantity, double price);
    
    /**
     * Get market data for a symbol
     * @return Array of [bestBid, bestAsk, lastPrice, spread]
     */
    public String[] getMarketData(String symbol) {
        if (nativeLibraryLoaded) {
            return getMarketDataNative(symbol);
        } else {
            // Mock implementation
            return new String[]{"100.50", "100.60", "100.55", "0.10"};
        }
    }
    
    private native String[] getMarketDataNative(String symbol);
    
    /**
     * Get order count for a symbol
     */
    public long getOrderCount(String symbol) {
        if (nativeLibraryLoaded) {
            return getOrderCountNative(symbol);
        } else {
            // Mock implementation
            return 42L;
        }
    }
    
    private native long getOrderCountNative(String symbol);
    
    /**
     * Get spread for a symbol
     */
    public double getSpread(String symbol) {
        if (nativeLibraryLoaded) {
            return getSpreadNative(symbol);
        } else {
            // Mock implementation
            return 0.10;
        }
    }
    
    private native double getSpreadNative(String symbol);
    
    /**
     * Check if symbol is halted
     */
    public boolean isSymbolHalted(String symbol) {
        if (nativeLibraryLoaded) {
            return isSymbolHaltedNative(symbol);
        } else {
            // Mock implementation
            return false;
        }
    }
    
    private native boolean isSymbolHaltedNative(String symbol);
    
    /**
     * Get executed trades for an order
     * @return Array of Trade objects (currently returns empty array)
     */
    public Object[] getExecutedTrades(String orderId) {
        if (nativeLibraryLoaded) {
            return getExecutedTradesNative(orderId);
        } else {
            // Mock implementation
            return new Object[0];
        }
    }
    
    private native Object[] getExecutedTradesNative(String orderId);
    
    // ==================== ULTRA-LOW LATENCY MARKET DATA ====================
    
    /**
     * Update market data directly in C++ (lock-free)
     * Latency: ~50 nanoseconds
     */
    public boolean updateMarketData(String symbol, double bestBid, double bestAsk, double lastPrice, long volume) {
        if (nativeLibraryLoaded) {
            return updateMarketDataNative(symbol, bestBid, bestAsk, lastPrice, volume);
        } else {
            // Mock implementation
            System.out.println("Mock: Updating market data for " + symbol + " bid=" + bestBid + " ask=" + bestAsk + " price=" + lastPrice + " vol=" + volume);
            return true;
        }
    }
    
    private native boolean updateMarketDataNative(String symbol, double bestBid, double bestAsk, double lastPrice, long volume);
    
    /**
     * Get market data from C++ (lock-free)
     * Returns: [bestBid, bestAsk, lastPrice, spread, volume, timestamp]
     * Latency: ~10 nanoseconds
     */
    public double[] getMarketDataLockFree(String symbol) {
        if (nativeLibraryLoaded) {
            return getMarketDataLockFreeNative(symbol);
        } else {
            // Mock implementation
            return new double[]{100.50, 100.60, 100.55, 0.10, 1000.0, System.currentTimeMillis()};
        }
    }
    
    private native double[] getMarketDataLockFreeNative(String symbol);
    
    /**
     * Check if symbol has valid market data
     */
    public boolean hasValidMarketData(String symbol) {
        if (nativeLibraryLoaded) {
            return hasValidMarketDataNative(symbol);
        } else {
            // Mock implementation
            return true;
        }
    }
    
    private native boolean hasValidMarketDataNative(String symbol);

    // ==================== C++ MARKET DATA SERVICE CONTROL ====================
    
    /**
     * Start the C++ Market Data Service
     * This will begin fetching real-time market data at high frequency
     */
    public boolean startMarketDataService() {
        if (nativeLibraryLoaded) {
            return startMarketDataServiceNative();
        } else {
            // Mock implementation
            System.out.println("Mock: Starting market data service");
            return true;
        }
    }
    
    private native boolean startMarketDataServiceNative();
    
    /**
     * Stop the C++ Market Data Service
     */
    public boolean stopMarketDataService() {
        if (nativeLibraryLoaded) {
            return stopMarketDataServiceNative();
        } else {
            // Mock implementation
            System.out.println("Mock: Stopping market data service");
            return true;
        }
    }
    
    private native boolean stopMarketDataServiceNative();
    
    /**
     * Check if the C++ Market Data Service is running
     */
    public boolean isMarketDataServiceRunning() {
        if (nativeLibraryLoaded) {
            return isMarketDataServiceRunningNative();
        } else {
            // Mock implementation
            return true;
        }
    }
    
    private native boolean isMarketDataServiceRunningNative();
    
    /**
     * Add a symbol to track
     */
    public boolean addSymbol(String symbol) {
        if (nativeLibraryLoaded) {
            return addSymbolNative(symbol);
        } else {
            // Mock implementation
            System.out.println("Mock: Adding symbol " + symbol);
            return true;
        }
    }
    
    private native boolean addSymbolNative(String symbol);
    
    /**
     * Remove a symbol from tracking
     */
    public boolean removeSymbol(String symbol) {
        if (nativeLibraryLoaded) {
            return removeSymbolNative(symbol);
        } else {
            // Mock implementation
            System.out.println("Mock: Removing symbol " + symbol);
            return true;
        }
    }
    
    private native boolean removeSymbolNative(String symbol);
    
    /**
     * Get all symbols being tracked
     */
    public String[] getSymbols() {
        if (nativeLibraryLoaded) {
            return getSymbolsNative();
        } else {
            // Mock implementation
            return new String[]{"AAPL", "GOOGL", "TSLA", "MSFT"};
        }
    }
    
    private native String[] getSymbolsNative();
    
    /**
     * Set the Alpha Vantage API key
     */
    public boolean setApiKey(String apiKey) {
        if (nativeLibraryLoaded) {
            return setApiKeyNative(apiKey);
        } else {
            // Mock implementation
            System.out.println("Mock: Setting API key");
            return true;
        }
    }
    
    private native boolean setApiKeyNative(String apiKey);
    
    /**
     * Set the update interval in milliseconds
     */
    public boolean setUpdateInterval(long intervalMs) {
        if (nativeLibraryLoaded) {
            return setUpdateIntervalNative(intervalMs);
        } else {
            // Mock implementation
            System.out.println("Mock: Setting update interval to " + intervalMs + "ms");
            return true;
        }
    }
    
    private native boolean setUpdateIntervalNative(long intervalMs);
    
    /**
     * Get performance metrics from the C++ service
     * Returns a Map with performance data
     */
    public Object getPerformanceMetrics() {
        if (nativeLibraryLoaded) {
            return getPerformanceMetricsNative();
        } else {
            // Mock implementation
            return java.util.Map.of("latency", "10ns", "throughput", "1M ops/sec", "memory", "64MB");
        }
    }
    
    private native Object getPerformanceMetricsNative();
    
    /**
     * Reset performance metrics
     */
    public boolean resetMetrics() {
        if (nativeLibraryLoaded) {
            return resetMetricsNative();
        } else {
            // Mock implementation
            System.out.println("Mock: Resetting metrics");
            return true;
        }
    }
    
    private native boolean resetMetricsNative();
    
    /**
     * Check if the C++ service is healthy
     */
    public boolean isHealthy() {
        if (nativeLibraryLoaded) {
            return isHealthyNative();
        } else {
            // Mock implementation
            return true;
        }
    }
    
    private native boolean isHealthyNative();
}
