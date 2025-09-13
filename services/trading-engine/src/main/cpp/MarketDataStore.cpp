#include "MarketDataStore.h"
#include <iostream>
#include <vector>

namespace quantis
{

    // Global market data store instance
    std::unique_ptr<MarketDataStore> g_marketDataStore = nullptr;

    void initializeMarketDataStore()
    {
        if (!g_marketDataStore)
        {
            g_marketDataStore = std::make_unique<MarketDataStore>();
            std::cout << "MarketDataStore initialized with lock-free architecture" << std::endl;
        }
    }

    MarketDataStore &getMarketDataStore()
    {
        if (!g_marketDataStore)
        {
            initializeMarketDataStore();
        }
        return *g_marketDataStore;
    }

} // namespace quantis
