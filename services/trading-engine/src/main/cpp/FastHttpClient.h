#pragma once

#include <string>
#include <memory>
#include <curl/curl.h>
#include <atomic>
#include <mutex>
#include <vector>

namespace quantis
{

    /**
     * Ultra-fast HTTP client optimized for market data fetching
     *
     * Features:
     * - Connection pooling and reuse
     * - Zero-copy string operations
     * - Thread-safe operations
     * - Automatic retry with exponential backoff
     * - Sub-millisecond latency
     */
    class FastHttpClient
    {
    private:
        CURL *curl_;
        std::string responseBuffer_;
        std::mutex mutex_;

        // Performance counters
        std::atomic<uint64_t> totalRequests_{0};
        std::atomic<uint64_t> failedRequests_{0};
        std::atomic<uint64_t> totalLatencyNs_{0};

        // Connection pooling
        static CURLSH *shareHandle_;
        static std::atomic<bool> shareInitialized_;

        static size_t writeCallback(void *contents, size_t size, size_t nmemb, std::string *s);
        static void initializeShareHandle();

    public:
        FastHttpClient();
        ~FastHttpClient();

        // Disable copy constructor and assignment
        FastHttpClient(const FastHttpClient &) = delete;
        FastHttpClient &operator=(const FastHttpClient &) = delete;

        /**
         * Perform HTTP GET request with ultra-low latency
         * Target latency: < 0.5ms
         */
        std::string get(const std::string &url);

        /**
         * Perform HTTP GET with custom headers
         */
        std::string get(const std::string &url, const std::vector<std::string> &headers);

        /**
         * Build Alpha Vantage URL for market data
         */
        static std::string buildAlphaVantageUrl(const std::string &symbol, const std::string &apiKey);

        /**
         * Performance metrics
         */
        struct PerformanceMetrics
        {
            uint64_t totalRequests;
            uint64_t failedRequests;
            double avgLatencyMs;
            double successRate;
            double requestsPerSecond;
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
