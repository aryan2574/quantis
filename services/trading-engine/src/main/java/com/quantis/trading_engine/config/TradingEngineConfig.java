package com.quantis.trading_engine.config;

import com.quantis.trading_engine.jni.TradingEngineJNI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * Configuration for the C++ Trading Engine integration
 */
@Configuration
public class TradingEngineConfig {

    /**
     * Initialize the C++ Trading Engine JNI interface (disabled by default)
     * Enable with: cpp.engine.enabled=true
     */
    @Bean
    @ConditionalOnProperty(name = "cpp.engine.enabled", havingValue = "true", matchIfMissing = false)
    public TradingEngineJNI tradingEngineJNI() {
        try {
            return new TradingEngineJNI();
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException("Failed to initialize C++ Trading Engine. " +
                "Make sure the native library is built and available in the classpath.", e);
        }
    }
}
