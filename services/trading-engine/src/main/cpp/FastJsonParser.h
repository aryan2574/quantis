#pragma once

#include <string>
#include <chrono>
#include <atomic>

namespace quantis
{

    /**
     * Ultra-fast JSON parser optimized for market data
     *
     * Features:
     * - Zero-copy string operations
     * - Pre-allocated buffers
     * - Custom Alpha Vantage parser
     * - Sub-microsecond parsing latency
     */
    class FastJsonParser
    {
    private:
        // Performance counters
        std::atomic<uint64_t> totalParses_{0};
        std::atomic<uint64_t> failedParses_{0};
        std::atomic<uint64_t> totalParseTimeNs_{0};

        // Pre-allocated buffers for performance
        static constexpr size_t MAX_RESPONSE_SIZE = 8192;
        char responseBuffer_[MAX_RESPONSE_SIZE];

    public:
        /**
         * Market data structure optimized for Alpha Vantage
         */
        struct MarketData
        {
            std::string symbol;
            double bestBid{0.0};
            double bestAsk{0.0};
            double lastPrice{0.0};
            double open{0.0};
            double high{0.0};
            double low{0.0};
            long volume{0};
            uint64_t timestamp{0};
            bool isValid{false};

            MarketData() = default;

            MarketData(const std::string &sym, double bid, double ask, double price,
                       double o, double h, double l, long vol)
                : symbol(sym), bestBid(bid), bestAsk(ask), lastPrice(price),
                  open(o), high(h), low(l), volume(vol),
                  timestamp(std::chrono::duration_cast<std::chrono::nanoseconds>(
                                std::chrono::high_resolution_clock::now().time_since_epoch())
                                .count()),
                  isValid(true) {}
        };

        /**
         * Parse Alpha Vantage JSON response with ultra-low latency
         * Target latency: < 0.1ms
         */
        MarketData parseAlphaVantage(const std::string &symbol, const std::string &jsonResponse);

        /**
         * Parse Alpha Vantage response with error handling
         */
        MarketData parseAlphaVantageSafe(const std::string &symbol, const std::string &jsonResponse);

        /**
         * Extract double value from JSON string (fast path)
         */
        static double extractDouble(const char *json, const char *key);

        /**
         * Extract long value from JSON string (fast path)
         */
        static long extractLong(const char *json, const char *key);

        /**
         * Extract string value from JSON string (fast path)
         */
        static std::string extractString(const char *json, const char *key);

        /**
         * Performance metrics
         */
        struct PerformanceMetrics
        {
            uint64_t totalParses;
            uint64_t failedParses;
            double avgParseTimeMs;
            double successRate;
            double parsesPerSecond;
        };

        PerformanceMetrics getPerformanceMetrics() const;

        /**
         * Reset performance counters
         */
        void resetMetrics();

        /**
         * Health check
         */
        bool isHealthy() const;
    };

} // namespace quantis
