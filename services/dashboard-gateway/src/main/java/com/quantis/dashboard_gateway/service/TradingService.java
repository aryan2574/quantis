package com.quantis.dashboard_gateway.service;

import com.quantis.dashboard_gateway.client.OrderClient;
import com.quantis.dashboard_gateway.model.OrderData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Trading Service
 * 
 * Service layer that wraps the OrderClient for trading operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradingService {

    private final OrderClient orderClient;

    public Mono<OrderData.Order> placeOrder(OrderData.PlaceOrderInput input) {
        return orderClient.placeOrder(input);
    }

    public Mono<Boolean> cancelOrder(String orderId) {
        return orderClient.cancelOrder(orderId);
    }

    public Mono<List<OrderData.Order>> getTradingHistory(String userId, Integer limit, Long startTime, Long endTime) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<List<OrderData.Order>> getOrderHistory(String userId, Integer limit, String status) {
        return orderClient.getOrderHistory(userId, limit, status);
    }

    public Mono<List<OrderData.Order>> getActiveOrders(String userId) {
        return orderClient.getActiveOrders(userId);
    }

    public Mono<List<OrderData.Order>> getRecentTrades(String userId, int limit) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }

    public Mono<List<OrderData.Order>> getOrderUpdates(String userId) {
        // TODO: Implement when client method is available
        return Mono.just(List.of());
    }
}
