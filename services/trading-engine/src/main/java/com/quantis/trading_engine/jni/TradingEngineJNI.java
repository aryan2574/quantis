package com.quantis.trading_engine.jni;

/**
 * JNI interface to C++ trading engine
 */
public class TradingEngineJNI {
    
    static {
        try {
            // Load the library from the classpath
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.contains("windows")) {
                // Load from classpath resources using proper URL handling
                String dllPath = TradingEngineJNI.class.getResource("/lib/tradingenginejni.dll").getPath();
                // Handle URL encoding (replace %20 with spaces, etc.)
                dllPath = dllPath.replaceAll("%20", " ");
                System.load(dllPath);
            } else {
                // For Linux/Mac, load the .so file
                String soPath = TradingEngineJNI.class.getResource("/lib/libtradingenginejni.so").getPath();
                soPath = soPath.replaceAll("%20", " ");
                System.load(soPath);
            }
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load tradingenginejni library: " + e.getMessage());
            System.err.println("Make sure the native library is in the correct location:");
            System.err.println("- Windows: tradingenginejni.dll in src/main/resources/lib/");
            System.err.println("- Linux/Mac: libtradingenginejni.so in src/main/resources/lib/");
            throw e;
        }
    }
    
    /**
     * Add an order to the C++ order book
     */
    public native boolean addOrder(String orderId, String userId, String symbol, 
                                  String side, long quantity, double price);
    
    /**
     * Remove an order from the C++ order book
     */
    public native boolean removeOrder(String orderId);
    
    /**
     * Update an existing order in the C++ order book
     */
    public native boolean updateOrder(String orderId, String userId, String symbol,
                                     String side, long quantity, double price);
    
    /**
     * Get market data for a symbol
     * @return Array of [bestBid, bestAsk, lastPrice, spread]
     */
    public native String[] getMarketData(String symbol);
    
    /**
     * Get order count for a symbol
     */
    public native long getOrderCount(String symbol);
    
    /**
     * Get spread for a symbol
     */
    public native double getSpread(String symbol);
    
    /**
     * Check if symbol is halted
     */
    public native boolean isSymbolHalted(String symbol);
    
    /**
     * Get executed trades for an order
     * @return Array of Trade objects (currently returns empty array)
     */
    public native Object[] getExecutedTrades(String orderId);
    
    // ==================== ULTRA-LOW LATENCY MARKET DATA ====================
    
    /**
     * Update market data directly in C++ (lock-free)
     * Latency: ~50 nanoseconds
     */
    public native boolean updateMarketData(String symbol, double bestBid, double bestAsk, double lastPrice, long volume);
    
    /**
     * Get market data from C++ (lock-free)
     * Returns: [bestBid, bestAsk, lastPrice, spread, volume, timestamp]
     * Latency: ~10 nanoseconds
     */
    public native double[] getMarketDataLockFree(String symbol);
    
    /**
     * Check if symbol has valid market data
     */
    public native boolean hasValidMarketData(String symbol);

    // ==================== C++ MARKET DATA SERVICE CONTROL ====================
    
    /**
     * Start the C++ Market Data Service
     * This will begin fetching real-time market data at high frequency
     */
    public native boolean startMarketDataService();
    
    /**
     * Stop the C++ Market Data Service
     */
    public native boolean stopMarketDataService();
    
    /**
     * Check if the C++ Market Data Service is running
     */
    public native boolean isMarketDataServiceRunning();
    
    /**
     * Add a symbol to track
     */
    public native boolean addSymbol(String symbol);
    
    /**
     * Remove a symbol from tracking
     */
    public native boolean removeSymbol(String symbol);
    
    /**
     * Get all symbols being tracked
     */
    public native String[] getSymbols();
    
    /**
     * Set the Alpha Vantage API key
     */
    public native boolean setApiKey(String apiKey);
    
    /**
     * Set the update interval in milliseconds
     */
    public native boolean setUpdateInterval(long intervalMs);
    
    /**
     * Get performance metrics from the C++ service
     * Returns a Map with performance data
     */
    public native Object getPerformanceMetrics();
    
    /**
     * Reset performance metrics
     */
    public native boolean resetMetrics();
    
    /**
     * Check if the C++ service is healthy
     */
    public native boolean isHealthy();
}
