#include "OrderBook.h"
#include <iostream>
#include <algorithm>

namespace quantis
{

    OrderBook::OrderBook(const std::string &symbol) : symbol_(symbol), marketDataStore_(getMarketDataStore())
    {
        std::cout << "OrderBook created for symbol: " << symbol_ << " with lock-free market data" << std::endl;
    }

    // Destructor is defaulted in header

    bool OrderBook::addOrder(std::shared_ptr<Order> order)
    {
        std::unique_lock<std::shared_mutex> lock(orderBookMutex_);

        try
        {
            // Store order
            orders_[order->orderId] = order;

            // Add to appropriate side
            if (order->side == "BUY")
            {
                buyOrders_[order->price].push_back(order);
                bestBid_.store(std::max(bestBid_.load(), order->price));
            }
            else if (order->side == "SELL")
            {
                sellOrders_[order->price].push_back(order);
                bestAsk_.store(std::min(bestAsk_.load(), order->price));
            }

            totalOrders_.fetch_add(1);
            totalVolume_.fetch_add(order->quantity);

            std::cout << "Order added: " << order->orderId << " " << order->side
                      << " " << order->quantity << "@" << order->price << std::endl;

            return true;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error adding order: " << e.what() << std::endl;
            return false;
        }
    }

    bool OrderBook::removeOrder(const std::string &orderId)
    {
        std::unique_lock<std::shared_mutex> lock(orderBookMutex_);

        try
        {
            auto it = orders_.find(orderId);
            if (it == orders_.end())
            {
                return false;
            }

            auto order = it->second;
            orders_.erase(it);

            // Remove from appropriate side
            if (order->side == "BUY")
            {
                auto buyIt = buyOrders_.find(order->price);
                if (buyIt != buyOrders_.end())
                {
                    auto &orders = buyIt->second;
                    orders.erase(std::remove(orders.begin(), orders.end(), order), orders.end());
                    if (orders.empty())
                    {
                        buyOrders_.erase(buyIt);
                    }
                }
            }
            else if (order->side == "SELL")
            {
                auto sellIt = sellOrders_.find(order->price);
                if (sellIt != sellOrders_.end())
                {
                    auto &orders = sellIt->second;
                    orders.erase(std::remove(orders.begin(), orders.end(), order), orders.end());
                    if (orders.empty())
                    {
                        sellOrders_.erase(sellIt);
                    }
                }
            }

            totalOrders_.fetch_sub(1);
            totalVolume_.fetch_sub(order->quantity);

            std::cout << "Order removed: " << orderId << std::endl;
            return true;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error removing order: " << e.what() << std::endl;
            return false;
        }
    }

    bool OrderBook::updateOrder(std::shared_ptr<Order> order)
    {
        std::unique_lock<std::shared_mutex> lock(orderBookMutex_);

        try
        {
            auto it = orders_.find(order->orderId);
            if (it == orders_.end())
            {
                return false;
            }

            auto oldOrder = it->second;

            // Remove old order
            removeOrder(order->orderId);

            // Add updated order
            return addOrder(order);
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error updating order: " << e.what() << std::endl;
            return false;
        }
    }

    std::vector<Trade> OrderBook::matchOrder(std::shared_ptr<Order> order)
    {
        std::vector<Trade> trades;

        try
        {
            if (order->side == "BUY")
            {
                trades = matchBuyOrder(order);
            }
            else if (order->side == "SELL")
            {
                trades = matchSellOrder(order);
            }

            // Update last trade price
            if (!trades.empty())
            {
                lastTradePrice_.store(trades.back().price);
            }
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error matching order: " << e.what() << std::endl;
        }

        return trades;
    }

    std::vector<Trade> OrderBook::matchBuyOrder(std::shared_ptr<Order> order)
    {
        std::vector<Trade> trades;

        // Simple matching logic - match against best ask
        if (!sellOrders_.empty())
        {
            auto bestAskIt = sellOrders_.begin();
            double bestAskPrice = bestAskIt->first;

            if (order->price >= bestAskPrice)
            {
                // Create trade
                Trade trade;
                trade.tradeId = "trade_" + std::to_string(std::time(nullptr));
                trade.orderId = order->orderId;
                trade.userId = order->userId;
                trade.symbol = order->symbol;
                trade.side = order->side;
                trade.quantity = std::min(order->quantity, bestAskIt->second[0]->quantity);
                trade.price = bestAskPrice;
                trade.totalValue = trade.quantity * trade.price;
                trade.executedAt = std::chrono::system_clock::now();

                trades.push_back(trade);

                std::cout << "Trade executed: " << trade.tradeId << " "
                          << trade.quantity << "@" << trade.price << std::endl;
            }
        }

        return trades;
    }

    std::vector<Trade> OrderBook::matchSellOrder(std::shared_ptr<Order> order)
    {
        std::vector<Trade> trades;

        // Simple matching logic - match against best bid
        if (!buyOrders_.empty())
        {
            auto bestBidIt = buyOrders_.begin();
            double bestBidPrice = bestBidIt->first;

            if (order->price <= bestBidPrice)
            {
                // Create trade
                Trade trade;
                trade.tradeId = "trade_" + std::to_string(std::time(nullptr));
                trade.orderId = order->orderId;
                trade.userId = order->userId;
                trade.symbol = order->symbol;
                trade.side = order->side;
                trade.quantity = std::min(order->quantity, bestBidIt->second[0]->quantity);
                trade.price = bestBidPrice;
                trade.totalValue = trade.quantity * trade.price;
                trade.executedAt = std::chrono::system_clock::now();

                trades.push_back(trade);

                std::cout << "Trade executed: " << trade.tradeId << " "
                          << trade.quantity << "@" << trade.price << std::endl;
            }
        }

        return trades;
    }

    double OrderBook::getSpread() const
    {
        double bid = bestBid_.load();
        double ask = bestAsk_.load();

        if (bid > 0 && ask > 0)
        {
            return ask - bid;
        }

        return 0.0;
    }

    size_t OrderBook::getOrderCount() const
    {
        return totalOrders_.load();
    }

    // Ultra-low latency market data integration
    bool OrderBook::updateMarketData(double bestBid, double bestAsk, double lastPrice, long volume)
    {
        return marketDataStore_.updateMarketData(symbol_, bestBid, bestAsk, lastPrice, volume);
    }

    bool OrderBook::getMarketData(double &bestBid, double &bestAsk, double &lastPrice, double &spread)
    {
        long volume;
        uint64_t timestamp;
        return marketDataStore_.getMarketData(symbol_, bestBid, bestAsk, lastPrice, spread, volume, timestamp);
    }

    bool OrderBook::hasValidMarketData() const
    {
        return marketDataStore_.hasValidData(symbol_);
    }

} // namespace quantis
