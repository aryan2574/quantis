#pragma once

#include <jni.h>
#include <string>
#include <memory>
#include "OrderBook.h"
#include "CppMarketDataService.h" // Use real implementation

namespace quantis
{

    class TradingEngineJNI
    {
    private:
        std::map<std::string, std::unique_ptr<OrderBook>> orderBooks_;
        std::unique_ptr<CppMarketDataService> marketDataService_;

    public:
        TradingEngineJNI();
        ~TradingEngineJNI();

        // JNI methods
        jboolean addOrder(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId, jstring userId,
                          jstring symbol, jstring side, jlong quantity, jdouble price);

        jboolean removeOrder(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId);

        jboolean updateOrder(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId, jstring userId,
                             jstring symbol, jstring side, jlong quantity, jdouble price);

        jobjectArray getMarketData(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol);

        jlong getOrderCount(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol);

        jdouble getSpread(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol);

        jboolean isSymbolHalted([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj, [[maybe_unused]] jstring symbol);

        // Get executed trades for an order
        jobjectArray getExecutedTrades(JNIEnv *env, [[maybe_unused]] jobject obj, jstring orderId);

        // Ultra-low latency market data methods
        jboolean updateMarketData(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol, jdouble bestBid, jdouble bestAsk, jdouble lastPrice, jlong volume);
        jdoubleArray getMarketDataLockFree(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol);
        jboolean hasValidMarketData(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol);

        // C++ Market Data Service control methods
        jboolean startMarketDataService([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj);
        jboolean stopMarketDataService([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj);
        jboolean isMarketDataServiceRunning([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj);
        jboolean addSymbol(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol);
        jboolean removeSymbol(JNIEnv *env, [[maybe_unused]] jobject obj, jstring symbol);
        jobjectArray getSymbols(JNIEnv *env, [[maybe_unused]] jobject obj);
        jboolean setApiKey(JNIEnv *env, [[maybe_unused]] jobject obj, jstring apiKey);
        jboolean setUpdateInterval([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj, jlong intervalMs);
        jobject getPerformanceMetrics(JNIEnv *env, [[maybe_unused]] jobject obj);
        jboolean resetMetrics([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj);
        jboolean isHealthy([[maybe_unused]] JNIEnv *env, [[maybe_unused]] jobject obj);

    private:
        OrderBook *getOrderBook(const std::string &symbol);
        std::string jstringToString(JNIEnv *env, jstring jstr);
        jstring stringToJstring(JNIEnv *env, const std::string &str);
        jobject createTradeObject(JNIEnv *env, const Trade &trade);
        jobject createPerformanceMetricsObject(JNIEnv *env, const CppMarketDataService::PerformanceMetrics &metrics);
    };

} // namespace quantis
