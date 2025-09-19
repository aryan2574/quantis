#include "TradingEngineJNI.h"
#include "MarketDataStore.h"
#include "CppMarketDataServiceStub.h" // Use stub instead of real implementation
#include <iostream>

namespace quantis
{

    TradingEngineJNI::TradingEngineJNI()
    {
        // Initialize market data service with stub implementation
        marketDataService_ = std::make_unique<CppMarketDataService>(getMarketDataStore());
        std::cout << "TradingEngineJNI initialized with C++ Market Data Service (stub)" << std::endl;
    }

    TradingEngineJNI::~TradingEngineJNI()
    {
        if (marketDataService_)
        {
            marketDataService_->stop();
        }
        orderBooks_.clear();
    }

    jboolean TradingEngineJNI::addOrder(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId,
                                        jstring userId, jstring symbol, jstring side,
                                        jlong quantity, jdouble price)
    {
        try
        {
            std::string orderIdStr = jstringToString(env, orderId);
            std::string userIdStr = jstringToString(env, userId);
            std::string symbolStr = jstringToString(env, symbol);
            std::string sideStr = jstringToString(env, side);

            auto order = std::make_shared<Order>(orderIdStr, userIdStr, symbolStr,
                                                 sideStr, quantity, price);

            OrderBook *orderBook = getOrderBook(symbolStr);
            if (!orderBook)
            {
                return JNI_FALSE;
            }

            bool success = orderBook->addOrder(order);
            return success ? JNI_TRUE : JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error in addOrder: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::removeOrder(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId)
    {
        try
        {
            std::string orderIdStr = jstringToString(env, orderId);

            for (auto &pair : orderBooks_)
            {
                if (pair.second->removeOrder(orderIdStr))
                {
                    return JNI_TRUE;
                }
            }

            return JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error in removeOrder: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::updateOrder(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId,
                                           jstring userId, jstring symbol, jstring side,
                                           jlong quantity, jdouble price)
    {
        try
        {
            std::string orderIdStr = jstringToString(env, orderId);
            std::string userIdStr = jstringToString(env, userId);
            std::string symbolStr = jstringToString(env, symbol);
            std::string sideStr = jstringToString(env, side);

            auto order = std::make_shared<Order>(orderIdStr, userIdStr, symbolStr,
                                                 sideStr, quantity, price);

            OrderBook *orderBook = getOrderBook(symbolStr);
            if (!orderBook)
            {
                return JNI_FALSE;
            }

            bool success = orderBook->updateOrder(order);
            return success ? JNI_TRUE : JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error in updateOrder: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jobjectArray TradingEngineJNI::getMarketData(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol)
    {
        try
        {
            std::string symbolStr = jstringToString(env, symbol);
            OrderBook *orderBook = getOrderBook(symbolStr);

            if (!orderBook)
            {
                return nullptr;
            }

            // Create array of market data strings
            jobjectArray result = env->NewObjectArray(4, env->FindClass("java/lang/String"), nullptr);

            env->SetObjectArrayElement(result, 0, stringToJstring(env, std::to_string(orderBook->getBestBid())));
            env->SetObjectArrayElement(result, 1, stringToJstring(env, std::to_string(orderBook->getBestAsk())));
            env->SetObjectArrayElement(result, 2, stringToJstring(env, std::to_string(orderBook->getLastPrice())));
            env->SetObjectArrayElement(result, 3, stringToJstring(env, std::to_string(orderBook->getSpread())));

            return result;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error in getMarketData: " << e.what() << std::endl;
            return nullptr;
        }
    }

    jlong TradingEngineJNI::getOrderCount(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol)
    {
        try
        {
            std::string symbolStr = jstringToString(env, symbol);
            OrderBook *orderBook = getOrderBook(symbolStr);

            if (!orderBook)
            {
                return 0;
            }

            return static_cast<jlong>(orderBook->getOrderCount());
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error in getOrderCount: " << e.what() << std::endl;
            return 0;
        }
    }

    jdouble TradingEngineJNI::getSpread(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol)
    {
        try
        {
            std::string symbolStr = jstringToString(env, symbol);
            OrderBook *orderBook = getOrderBook(symbolStr);

            if (!orderBook)
            {
                return 0.0;
            }

            return static_cast<jdouble>(orderBook->getSpread());
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error in getSpread: " << e.what() << std::endl;
            return 0.0;
        }
    }

    jboolean TradingEngineJNI::isSymbolHalted([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj, [[maybe_unused]] jstring symbol)
    {
        // For now, always return false (no halts)
        // In production, this would check circuit breakers
        return JNI_FALSE;
    }

    jobjectArray TradingEngineJNI::getExecutedTrades(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId)
    {
        try
        {
            std::string orderIdStr = jstringToString(env, orderId);

            for ([[maybe_unused]] auto &pair : orderBooks_)
            {
                // This is a simplified implementation - in reality, you'd need to track trades per order
                // For now, return empty array as the current implementation doesn't store trades
                jobjectArray result = env->NewObjectArray(0, env->FindClass("java/lang/Object"), nullptr);
                return result;
            }

            return nullptr;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error in getExecutedTrades: " << e.what() << std::endl;
            return nullptr;
        }
    }

    OrderBook *TradingEngineJNI::getOrderBook(const std::string &symbol)
    {

        auto it = orderBooks_.find(symbol);
        if (it == orderBooks_.end())
        {
            orderBooks_[symbol] = std::make_unique<OrderBook>(symbol);
            return orderBooks_[symbol].get();
        }

        return it->second.get();
    }

    std::string TradingEngineJNI::jstringToString(JNIEnv *env, jstring jstr)
    {
        if (!jstr)
            return "";

        const char *chars = env->GetStringUTFChars(jstr, nullptr);
        std::string result(chars);
        env->ReleaseStringUTFChars(jstr, chars);

        return result;
    }

    // Ultra-low latency market data methods
    jboolean TradingEngineJNI::updateMarketData(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol,
                                                jdouble bestBid, jdouble bestAsk, jdouble lastPrice, jlong volume)
    {
        try
        {
            std::string symbolStr = jstringToString(env, symbol);

            MarketDataStore &store = getMarketDataStore();
            bool success = store.updateMarketData(symbolStr, bestBid, bestAsk, lastPrice, volume);

            return success ? JNI_TRUE : JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error updating market data: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jdoubleArray TradingEngineJNI::getMarketDataLockFree(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol)
    {
        try
        {
            std::string symbolStr = jstringToString(env, symbol);

            MarketDataStore &store = getMarketDataStore();
            double bestBid, bestAsk, lastPrice, spread;
            long volume;
            uint64_t timestamp;

            bool success = store.getMarketData(symbolStr, bestBid, bestAsk, lastPrice, spread, volume, timestamp);

            if (!success)
            {
                return nullptr;
            }

            // Create Java double array: [bestBid, bestAsk, lastPrice, spread, volume, timestamp]
            jdoubleArray result = env->NewDoubleArray(6);
            jdouble values[6] = {bestBid, bestAsk, lastPrice, spread, (double)volume, (double)timestamp};
            env->SetDoubleArrayRegion(result, 0, 6, values);

            return result;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error getting market data: " << e.what() << std::endl;
            return nullptr;
        }
    }

    jboolean TradingEngineJNI::hasValidMarketData(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol)
    {
        try
        {
            std::string symbolStr = jstringToString(env, symbol);

            MarketDataStore &store = getMarketDataStore();
            bool hasData = store.hasValidData(symbolStr);

            return hasData ? JNI_TRUE : JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error checking market data: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jstring TradingEngineJNI::stringToJstring(JNIEnv *env, const std::string &str)
    {
        return env->NewStringUTF(str.c_str());
    }

    // C++ Market Data Service control methods
    jboolean TradingEngineJNI::startMarketDataService([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            bool success = marketDataService_->start();
            return success ? JNI_TRUE : JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error starting market data service: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::stopMarketDataService([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            marketDataService_->stop();
            return JNI_TRUE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error stopping market data service: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::isMarketDataServiceRunning([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            bool running = marketDataService_->isRunning();
            return running ? JNI_TRUE : JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error checking market data service status: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::addSymbol(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            std::string symbolStr = jstringToString(env, symbol);
            marketDataService_->addSymbol(symbolStr);
            return JNI_TRUE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error adding symbol: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::removeSymbol(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            std::string symbolStr = jstringToString(env, symbol);
            marketDataService_->removeSymbol(symbolStr);
            return JNI_TRUE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error removing symbol: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jobjectArray TradingEngineJNI::getSymbols(JNIEnv *env, [[maybe_unused]] jobject obj)
    {
        try
        {
            if (!marketDataService_)
            {
                return nullptr;
            }

            auto symbols = marketDataService_->getSymbols();
            jobjectArray result = env->NewObjectArray(symbols.size(), env->FindClass("java/lang/String"), nullptr);

            for (size_t i = 0; i < symbols.size(); i++)
            {
                jstring symbolStr = env->NewStringUTF(symbols[i].c_str());
                env->SetObjectArrayElement(result, i, symbolStr);
                env->DeleteLocalRef(symbolStr);
            }

            return result;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error getting symbols: " << e.what() << std::endl;
            return nullptr;
        }
    }

    jboolean TradingEngineJNI::setApiKey(JNIEnv *env, [[maybe_unused]] jobject obj, jstring apiKey)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            std::string apiKeyStr = jstringToString(env, apiKey);
            marketDataService_->setApiKey(apiKeyStr);
            return JNI_TRUE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error setting API key: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::setUpdateInterval([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj, jlong intervalMs)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            marketDataService_->setUpdateInterval(std::chrono::milliseconds(intervalMs));
            return JNI_TRUE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error setting update interval: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jobject TradingEngineJNI::getPerformanceMetrics(JNIEnv *env, [[maybe_unused]] jobject obj)
    {
        try
        {
            if (!marketDataService_)
            {
                return nullptr;
            }

            auto metrics = marketDataService_->getPerformanceMetrics();
            return createPerformanceMetricsObject(env, metrics);
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error getting performance metrics: " << e.what() << std::endl;
            return nullptr;
        }
    }

    jboolean TradingEngineJNI::resetMetrics([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            marketDataService_->resetMetrics();
            return JNI_TRUE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error resetting metrics: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jboolean TradingEngineJNI::isHealthy([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj)
    {
        try
        {
            if (!marketDataService_)
            {
                return JNI_FALSE;
            }

            bool healthy = marketDataService_->isHealthy();
            return healthy ? JNI_TRUE : JNI_FALSE;
        }
        catch (const std::exception &e)
        {
            std::cerr << "Error checking health: " << e.what() << std::endl;
            return JNI_FALSE;
        }
    }

    jobject TradingEngineJNI::createPerformanceMetricsObject(JNIEnv *env, const CppMarketDataService::PerformanceMetrics &metrics)
    {
        // Create a simple Map object to return performance metrics
        jclass mapClass = env->FindClass("java/util/HashMap");
        jmethodID mapConstructor = env->GetMethodID(mapClass, "<init>", "()V");
        jmethodID putMethod = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

        jobject map = env->NewObject(mapClass, mapConstructor);

        // Add metrics to map
        jstring key;
        jstring value;

        key = env->NewStringUTF("totalUpdates");
        value = env->NewStringUTF(std::to_string(metrics.totalUpdates).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("failedUpdates");
        value = env->NewStringUTF(std::to_string(metrics.failedUpdates).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("avgLatencyMs");
        value = env->NewStringUTF(std::to_string(metrics.avgLatencyMs).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("updatesPerSecond");
        value = env->NewStringUTF(std::to_string(metrics.updatesPerSecond).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("successRate");
        value = env->NewStringUTF(std::to_string(metrics.successRate).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("uptimeSeconds");
        value = env->NewStringUTF(std::to_string(metrics.uptimeSeconds).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        return map;
    }

    jobject TradingEngineJNI::createTradeObject(JNIEnv *env, const Trade &trade)
    {
        // Create a simple Map object to return trade data
        jclass mapClass = env->FindClass("java/util/HashMap");
        jmethodID mapConstructor = env->GetMethodID(mapClass, "<init>", "()V");
        jmethodID putMethod = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");

        jobject map = env->NewObject(mapClass, mapConstructor);

        // Add trade data to map
        jstring key;
        jstring value;

        key = env->NewStringUTF("tradeId");
        value = env->NewStringUTF(trade.tradeId.c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("orderId");
        value = env->NewStringUTF(trade.orderId.c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("userId");
        value = env->NewStringUTF(trade.userId.c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("symbol");
        value = env->NewStringUTF(trade.symbol.c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("side");
        value = env->NewStringUTF(trade.side.c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("quantity");
        value = env->NewStringUTF(std::to_string(trade.quantity).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("price");
        value = env->NewStringUTF(std::to_string(trade.price).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        key = env->NewStringUTF("totalValue");
        value = env->NewStringUTF(std::to_string(trade.totalValue).c_str());
        env->CallObjectMethod(map, putMethod, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);

        return map;
    }

} // namespace quantis
