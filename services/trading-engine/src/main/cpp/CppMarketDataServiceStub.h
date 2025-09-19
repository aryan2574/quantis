#pragma once

#include <string>
#include <vector>
#include <chrono>

namespace quantis
{
    class MarketDataStore;

    // Stub class to replace CppMarketDataService for compilation without external dependencies
    class CppMarketDataService
    {
    public:
        explicit CppMarketDataService(MarketDataStore &store) {}

        bool start() { return false; }
        void stop() {}
        bool isRunning() const { return false; }

        void setSymbols(const std::vector<std::string> &symbols) {}
        void addSymbol(const std::string &symbol) {}
        void removeSymbol(const std::string &symbol) {}
        void setApiKey(const std::string &apiKey) {}
        void setUpdateInterval(std::chrono::milliseconds interval) {}

        struct PerformanceMetrics
        {
            uint64_t totalUpdates = 0;
            uint64_t failedUpdates = 0;
            double avgLatencyMs = 0.0;
            double updatesPerSecond = 0.0;
            double successRate = 0.0;
            double uptimeSeconds = 0.0;
        };

        PerformanceMetrics getPerformanceMetrics() const { return PerformanceMetrics{}; }
        void resetMetrics() {}
        bool isHealthy() const { return false; }

        std::vector<std::string> getSymbols() const { return {}; }
    };
}
