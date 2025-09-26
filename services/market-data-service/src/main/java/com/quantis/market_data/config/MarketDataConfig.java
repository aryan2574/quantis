package com.quantis.market_data.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Market Data Configuration
 * 
 * Configures real market data connections and API clients
 */
@Configuration
@EnableScheduling
@EnableAsync
public class MarketDataConfig {
    
    /**
     * Configure RestTemplate for market data API calls
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Configure timeout settings
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 seconds
        factory.setReadTimeout(10000); // 10 seconds
        
        restTemplate.setRequestFactory(factory);
        
        // Add error handler for market data APIs
        restTemplate.setErrorHandler(new MarketDataErrorHandler());
        
        return restTemplate;
    }
    
    /**
     * Configure async executor for market data operations
     */
    @Bean(name = "marketDataExecutor")
    public Executor marketDataExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("MarketData-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * Market Data Error Handler
     */
    private static class MarketDataErrorHandler implements org.springframework.web.client.ResponseErrorHandler {
        @Override
        public boolean hasError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
            return response.getStatusCode().isError();
        }
        
        @Override
        public void handleError(org.springframework.http.client.ClientHttpResponse response) throws java.io.IOException {
            System.err.println("Market data API error: " + response.getStatusCode());
        }
    }
}
