#include "TradingEngineJNI.h"
#include <iostream>

// Global instance
static quantis::TradingEngineJNI *g_tradingEngine = nullptr;

extern "C"
{

    JNIEXPORT jint JNICALL JNI_OnLoad([[maybe_unused]] JavaVM *vm, [[maybe_unused]] void *reserved)
    {
        std::cout << "TradingEngineJNI JNI_OnLoad called" << std::endl;
        g_tradingEngine = new quantis::TradingEngineJNI();
        return JNI_VERSION_1_8;
    }

    JNIEXPORT void JNICALL JNI_OnUnload([[maybe_unused]] JavaVM *vm, [[maybe_unused]] void *reserved)
    {
        std::cout << "TradingEngineJNI JNI_OnUnload called" << std::endl;
        delete g_tradingEngine;
        g_tradingEngine = nullptr;
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_addOrder(JNIEnv *env, jobject obj, jstring orderId, jstring userId, jstring symbol,
                                                                                             jstring side, jlong quantity, jdouble price)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->addOrder(env, obj, orderId, userId, symbol, side, quantity, price);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_removeOrder(JNIEnv *env, jobject obj, jstring orderId)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->removeOrder(env, obj, orderId);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_updateOrder(JNIEnv *env, jobject obj, jstring orderId, jstring userId, jstring symbol,
                                                                                                jstring side, jlong quantity, jdouble price)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->updateOrder(env, obj, orderId, userId, symbol, side, quantity, price);
    }

    JNIEXPORT jobjectArray JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_getMarketData(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return nullptr;
        }
        return g_tradingEngine->getMarketData(env, obj, symbol);
    }

    JNIEXPORT jlong JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_getOrderCount(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return 0;
        }
        return g_tradingEngine->getOrderCount(env, obj, symbol);
    }

    JNIEXPORT jdouble JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_getSpread(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return 0.0;
        }
        return g_tradingEngine->getSpread(env, obj, symbol);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_isSymbolHalted(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->isSymbolHalted(env, obj, symbol);
    }

    JNIEXPORT jobjectArray JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_getExecutedTrades(JNIEnv *env, jobject obj, jstring orderId)
    {
        if (!g_tradingEngine)
        {
            return nullptr;
        }
        return g_tradingEngine->getExecutedTrades(env, obj, orderId);
    }

    // Ultra-low latency market data methods
    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_updateMarketData(JNIEnv *env, jobject obj, jstring symbol, jdouble bestBid, jdouble bestAsk, jdouble lastPrice, jlong volume)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->updateMarketData(env, obj, symbol, bestBid, bestAsk, lastPrice, volume);
    }

    JNIEXPORT jdoubleArray JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_getMarketDataLockFree(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return nullptr;
        }
        return g_tradingEngine->getMarketDataLockFree(env, obj, symbol);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_hasValidMarketData(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->hasValidMarketData(env, obj, symbol);
    }

    // C++ Market Data Service control methods
    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_startMarketDataService(JNIEnv *env, jobject obj)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->startMarketDataService(env, obj);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_stopMarketDataService(JNIEnv *env, jobject obj)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->stopMarketDataService(env, obj);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_isMarketDataServiceRunning(JNIEnv *env, jobject obj)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->isMarketDataServiceRunning(env, obj);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_addSymbol(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->addSymbol(env, obj, symbol);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_removeSymbol(JNIEnv *env, jobject obj, jstring symbol)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->removeSymbol(env, obj, symbol);
    }

    JNIEXPORT jobjectArray JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_getSymbols(JNIEnv *env, jobject obj)
    {
        if (!g_tradingEngine)
        {
            return nullptr;
        }
        return g_tradingEngine->getSymbols(env, obj);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_setApiKey(JNIEnv *env, jobject obj, jstring apiKey)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->setApiKey(env, obj, apiKey);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_setUpdateInterval(JNIEnv *env, jobject obj, jlong intervalMs)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->setUpdateInterval(env, obj, intervalMs);
    }

    JNIEXPORT jobject JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_getPerformanceMetrics(JNIEnv *env, jobject obj)
    {
        if (!g_tradingEngine)
        {
            return nullptr;
        }
        return g_tradingEngine->getPerformanceMetrics(env, obj);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_resetMetrics(JNIEnv *env, jobject obj)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->resetMetrics(env, obj);
    }

    JNIEXPORT jboolean JNICALL Java_com_quantis_trading_engine_jni_TradingEngineJNI_isHealthy(JNIEnv *env, jobject obj)
    {
        if (!g_tradingEngine)
        {
            return JNI_FALSE;
        }
        return g_tradingEngine->isHealthy(env, obj);
    }

} // extern "C"
