package com.quantis.dashboard_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration for connecting to all microservices
 * This class sets up WebClient instances for HTTP communication
 * with each service in the Quantis platform
 */
@Configuration
public class ServiceConfig {

    @Value("${services.portfolio.url:http://localhost:8081}")
    private String portfolioServiceUrl;

    @Value("${services.market-data.url:http://localhost:8082}")
    private String marketDataServiceUrl;

    @Value("${services.trading-engine.url:http://localhost:8083}")
    private String tradingEngineUrl;

    @Value("${services.order-ingress.url:http://localhost:8084}")
    private String orderIngressUrl;

    @Value("${services.risk-service.url:http://localhost:8086}")
    private String riskServiceUrl;

    @Value("${services.auth-service.url:http://localhost:8087}")
    private String authServiceUrl;

    @Value("${services.update-service.url:http://localhost:8088}")
    private String updateServiceUrl;

    /**
     * WebClient for Portfolio Service
     */
    @Bean("portfolioWebClient")
    public WebClient portfolioWebClient() {
        return WebClient.builder()
                .baseUrl(portfolioServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * WebClient for Market Data Service
     */
    @Bean("marketDataWebClient")
    public WebClient marketDataWebClient() {
        return WebClient.builder()
                .baseUrl(marketDataServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * WebClient for Trading Engine
     */
    @Bean("tradingEngineWebClient")
    public WebClient tradingEngineWebClient() {
        return WebClient.builder()
                .baseUrl(tradingEngineUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * WebClient for Order Ingress Service
     */
    @Bean("orderIngressWebClient")
    public WebClient orderIngressWebClient() {
        return WebClient.builder()
                .baseUrl(orderIngressUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * WebClient for Risk Service
     */
    @Bean("riskServiceWebClient")
    public WebClient riskServiceWebClient() {
        return WebClient.builder()
                .baseUrl(riskServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * WebClient for Auth Service
     */
    @Bean("authServiceWebClient")
    public WebClient authServiceWebClient() {
        return WebClient.builder()
                .baseUrl(authServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    /**
     * WebClient for Update Service
     */
    @Bean("updateServiceWebClient")
    public WebClient updateServiceWebClient() {
        return WebClient.builder()
                .baseUrl(updateServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
