#pragma once

#include <map>
#include <vector>
#include <string>
#include <memory>
#include <atomic>
#include <chrono>
#include <functional>
#include <mutex>
#include <shared_mutex>
#include <concepts>
#include <ranges>
#include <algorithm>
#include <execution>
#include <thread>
#include <future>
#include <queue>
#include <condition_variable>
#include <generator>
#include "MarketDataStore.h"

namespace quantis
{

    struct Order
    {
        std::string orderId;
        std::string userId;
        std::string symbol;
        std::string side; // "BUY" or "SELL"
        long quantity;
        double price;
        std::chrono::system_clock::time_point timestamp;
        bool isActive;

        Order() : quantity(0), price(0.0), isActive(false) {}

        Order(const std::string &id, const std::string &user, const std::string &sym,
              const std::string &s, long qty, double prc)
            : orderId(id), userId(user), symbol(sym), side(s), quantity(qty), price(prc),
              timestamp(std::chrono::system_clock::now()), isActive(true) {}
    };

    struct Trade
    {
        std::string tradeId;
        std::string orderId;
        std::string userId;
        std::string symbol;
        std::string side;
        long quantity;
        double price;
        double totalValue;
        std::chrono::system_clock::time_point executedAt;

        Trade() : quantity(0), price(0.0), totalValue(0.0) {}
    };

    // Type safety helpers (C++17 compatible)
    template <typename T>
    struct is_numeric : std::integral_constant<bool, std::is_integral<T>::value || std::is_floating_point<T>::value>
    {
    };

    class OrderBook
    {
    private:
        std::string symbol_;
        mutable std::shared_mutex orderBookMutex_; // Reader-writer lock
        std::mutex tradeMutex_;                    // For trade operations
        std::condition_variable tradeCondition_;   // For async operations

        // Thread-safe order storage with modern containers
        std::map<double, std::vector<std::shared_ptr<Order>>, std::greater<double>> buyOrders_;
        std::map<double, std::vector<std::shared_ptr<Order>>> sellOrders_;

        // Lock-free atomic counters
        std::atomic<size_t> totalOrders_{0};
        std::atomic<size_t> totalVolume_{0};
        std::atomic<double> lastTradePrice_{0.0};

        // Async trade processing
        std::queue<std::shared_ptr<Order>> pendingOrders_;
        std::atomic<bool> processingEnabled_{true};
        std::thread processingThread_;

        // Ultra-low latency market data integration
        MarketDataStore &marketDataStore_;

        // Order lookup by ID for fast access
        std::map<std::string, std::shared_ptr<Order>> orders_;

        // Market data
        std::atomic<double> lastPrice_{0.0};
        std::atomic<double> bestBid_{0.0};
        std::atomic<double> bestAsk_{0.0};

    public:
        explicit OrderBook(const std::string &symbol);
        ~OrderBook() = default;

        // Order management
        bool addOrder(std::shared_ptr<Order> order);
        bool removeOrder(const std::string &orderId);
        bool updateOrder(std::shared_ptr<Order> order);

        // Order matching
        std::vector<Trade> matchOrder(std::shared_ptr<Order> order);

        // Market data
        double getBestBid() const { return bestBid_.load(); }
        double getBestAsk() const { return bestAsk_.load(); }
        double getLastPrice() const { return lastPrice_.load(); }

        // Ultra-low latency market data integration
        bool updateMarketData(double bestBid, double bestAsk, double lastPrice, long volume = 0);
        bool getMarketData(double &bestBid, double &bestAsk, double &lastPrice, double &spread);
        bool hasValidMarketData() const;
        double getSpread() const;
        long getTotalVolume() const { return totalVolume_.load(); }

        // Order book state
        std::vector<std::shared_ptr<Order>> getBestBidOrders() const;
        std::vector<std::shared_ptr<Order>> getBestAskOrders() const;
        size_t getOrderCount() const;

        // Utility
        void updateMarketData();
        bool isCrossed() const;

        // Modern C++17 Features
        template <typename T>
        bool addOrderModern(std::shared_ptr<T> order);

        // Async order processing
        std::future<std::vector<Trade>> addOrderAsync(std::shared_ptr<Order> order);

        // Parallel order matching using C++17 parallel algorithms
        std::vector<Trade> matchOrderParallel(std::shared_ptr<Order> order);

        // Range-based operations (C++17 compatible)
        std::vector<std::shared_ptr<Order>> getBestBids() const;
        std::vector<std::shared_ptr<Order>> getBestAsks() const;

        // Lock-free statistics with [[nodiscard]]
        [[nodiscard]] size_t getTotalOrders() const noexcept { return totalOrders_.load(); }
        [[nodiscard]] size_t getTotalVolumeAtomic() const noexcept { return totalVolume_.load(); }
        [[nodiscard]] double getLastTradePrice() const noexcept { return lastTradePrice_.load(); }

        // Modern C++17 features
        void processOrdersInBatches(size_t batchSize = 100);
        void enableAsyncProcessing(bool enable = true);

    private:
        std::vector<Trade> matchBuyOrder(std::shared_ptr<Order> order);
        std::vector<Trade> matchSellOrder(std::shared_ptr<Order> order);
        void removeOrderFromLevel(std::shared_ptr<Order> order);
        void addOrderToLevel(std::shared_ptr<Order> order);
    };

} // namespace quantis
