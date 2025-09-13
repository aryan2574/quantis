#include "CppMarketDataService.h"
#include <iostream>
#include <algorithm>
#include <thread>

namespace quantis
{

    CppMarketDataService::CppMarketDataService(MarketDataStore &store)
        : marketDataStore_(store), apiKey_("") // API key will be set via setApiKey()
          ,
          updateInterval_(std::chrono::milliseconds(12)) // ~83 updates/second
          ,
          startTime_(std::chrono::steady_clock::now())
    {

        // Initialize components
        httpClient_ = std::make_unique<FastHttpClient>();
        jsonParser_ = std::make_unique<FastJsonParser>();

        // Default symbols
        symbols_ = {"AAPL", "GOOGL", "MSFT", "TSLA", "AMZN", "META", "NVDA", "NFLX"};

        std::cout << "CppMarketDataService initialized with " << symbols_.size() << " symbols" << std::endl;
    }

    CppMarketDataService::~CppMarketDataService()
    {
        stop();
    }

    bool CppMarketDataService::start()
    {
        if (running_.exchange(true))
        {
            std::cout << "CppMarketDataService already running" << std::endl;
            return true;
        }

        try
        {
            workerThread_ = std::thread(&CppMarketDataService::workerThread, this);
            std::cout << "CppMarketDataService started successfully" << std::endl;
            return true;
        }
        catch (const std::exception &e)
        {
            running_.store(false);
            std::cerr << "Failed to start CppMarketDataService: " << e.what() << std::endl;
            return false;
        }
    }

    void CppMarketDataService::stop()
    {
        if (!running_.exchange(false))
        {
            return;
        }

        if (workerThread_.joinable())
        {
            workerThread_.join();
        }

        std::cout << "CppMarketDataService stopped" << std::endl;
    }

    void CppMarketDataService::setSymbols(const std::vector<std::string> &symbols)
    {
        std::lock_guard<std::mutex> lock(configMutex_);
        symbols_ = symbols;
        std::cout << "Updated symbols: " << symbols_.size() << " symbols" << std::endl;
    }

    void CppMarketDataService::addSymbol(const std::string &symbol)
    {
        std::lock_guard<std::mutex> lock(configMutex_);
        auto it = std::find(symbols_.begin(), symbols_.end(), symbol);
        if (it == symbols_.end())
        {
            symbols_.push_back(symbol);
            std::cout << "Added symbol: " << symbol << std::endl;
        }
    }

    void CppMarketDataService::removeSymbol(const std::string &symbol)
    {
        std::lock_guard<std::mutex> lock(configMutex_);
        auto it = std::find(symbols_.begin(), symbols_.end(), symbol);
        if (it != symbols_.end())
        {
            symbols_.erase(it);
            std::cout << "Removed symbol: " << symbol << std::endl;
        }
    }

    void CppMarketDataService::setApiKey(const std::string &apiKey)
    {
        std::lock_guard<std::mutex> lock(configMutex_);
        apiKey_ = apiKey;
        std::cout << "Updated API key" << std::endl;
    }

    void CppMarketDataService::setUpdateInterval(std::chrono::milliseconds interval)
    {
        std::lock_guard<std::mutex> lock(configMutex_);
        updateInterval_ = interval;
        std::cout << "Updated interval: " << interval.count() << "ms" << std::endl;
    }

    std::vector<std::string> CppMarketDataService::getSymbols() const
    {
        std::lock_guard<std::mutex> lock(const_cast<std::mutex &>(configMutex_));
        return symbols_;
    }

    CppMarketDataService::PerformanceMetrics CppMarketDataService::getPerformanceMetrics() const
    {
        auto now = std::chrono::steady_clock::now();
        auto uptime = std::chrono::duration_cast<std::chrono::seconds>(now - startTime_);

        uint64_t total = totalUpdates_.load();
        uint64_t failed = failedUpdates_.load();
        uint64_t latencyNs = totalLatencyNs_.load();

        PerformanceMetrics metrics;
        metrics.totalUpdates = total;
        metrics.failedUpdates = failed;
        metrics.avgLatencyMs = total > 0 ? (latencyNs / total) / 1e6 : 0.0;
        metrics.updatesPerSecond = total > 0 ? total / uptime.count() : 0.0;
        metrics.successRate = total > 0 ? (double)(total - failed) / total * 100.0 : 0.0;
        metrics.uptimeSeconds = uptime.count();

        if (httpClient_)
        {
            metrics.httpMetrics = httpClient_->getPerformanceMetrics();
        }

        if (jsonParser_)
        {
            metrics.parserMetrics = jsonParser_->getPerformanceMetrics();
        }

        return metrics;
    }

    void CppMarketDataService::resetMetrics()
    {
        totalUpdates_.store(0);
        failedUpdates_.store(0);
        totalLatencyNs_.store(0);

        if (httpClient_)
        {
            httpClient_->resetMetrics();
        }

        if (jsonParser_)
        {
            jsonParser_->resetMetrics();
        }

        startTime_ = std::chrono::steady_clock::now();
        std::cout << "Performance metrics reset" << std::endl;
    }

    bool CppMarketDataService::isHealthy() const
    {
        return running_.load() &&
               httpClient_ && httpClient_->isHealthy() &&
               jsonParser_ && jsonParser_->isHealthy();
    }

    bool CppMarketDataService::updateSymbol(const std::string &symbol)
    {
        return updateMarketData(symbol);
    }

    void CppMarketDataService::workerThread()
    {
        std::cout << "Market data worker thread started" << std::endl;

        while (running_.load())
        {
            try
            {
                // Get current symbols (thread-safe copy)
                std::vector<std::string> currentSymbols;
                {
                    std::lock_guard<std::mutex> lock(configMutex_);
                    currentSymbols = symbols_;
                }

                // Update each symbol
                for (const auto &symbol : currentSymbols)
                {
                    if (!running_.load())
                        break;

                    if (canUpdate())
                    {
                        updateMarketData(symbol);
                        updateRateLimit();
                    }
                }

                // Sleep for update interval
                std::this_thread::sleep_for(updateInterval_);
            }
            catch (const std::exception &e)
            {
                std::cerr << "Error in worker thread: " << e.what() << std::endl;
                std::this_thread::sleep_for(std::chrono::milliseconds(100));
            }
        }

        std::cout << "Market data worker thread stopped" << std::endl;
    }

    bool CppMarketDataService::updateMarketData(const std::string &symbol)
    {
        auto start = std::chrono::high_resolution_clock::now();

        try
        {
            // Build URL
            std::string url = FastHttpClient::buildAlphaVantageUrl(symbol, apiKey_);

            // Fetch data
            std::string response = httpClient_->get(url);
            if (response.empty())
            {
                failedUpdates_.fetch_add(1);
                return false;
            }

            // Parse JSON
            auto marketData = jsonParser_->parseAlphaVantageSafe(symbol, response);
            if (!marketData.isValid)
            {
                failedUpdates_.fetch_add(1);
                return false;
            }

            // Update store
            bool success = marketDataStore_.updateMarketData(
                symbol,
                marketData.bestBid,
                marketData.bestAsk,
                marketData.lastPrice,
                marketData.volume);

            if (success)
            {
                totalUpdates_.fetch_add(1);
            }
            else
            {
                failedUpdates_.fetch_add(1);
            }

            auto end = std::chrono::high_resolution_clock::now();
            auto duration = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start);
            totalLatencyNs_.fetch_add(duration.count());

            return success;
        }
        catch (const std::exception &e)
        {
            failedUpdates_.fetch_add(1);
            std::cerr << "Error updating market data for " << symbol << ": " << e.what() << std::endl;
            return false;
        }
    }

    bool CppMarketDataService::canUpdate() const
    {
        auto now = std::chrono::steady_clock::now();
        auto nowMs = std::chrono::duration_cast<std::chrono::milliseconds>(now.time_since_epoch()).count();

        uint64_t lastUpdate = lastUpdateTime_.load();
        uint64_t intervalMs = static_cast<uint64_t>(minUpdateInterval_.count());
        return (static_cast<uint64_t>(nowMs) - lastUpdate) >= intervalMs;
    }

    void CppMarketDataService::updateRateLimit()
    {
        auto now = std::chrono::steady_clock::now();
        auto nowMs = std::chrono::duration_cast<std::chrono::milliseconds>(now.time_since_epoch()).count();
        lastUpdateTime_.store(nowMs);
    }

} // namespace quantis
