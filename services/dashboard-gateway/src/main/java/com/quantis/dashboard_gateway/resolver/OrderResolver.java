package com.quantis.dashboard_gateway.resolver;

import com.quantis.dashboard_gateway.client.OrderClient;
import com.quantis.dashboard_gateway.model.OrderData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * GraphQL Resolver for Order Management Operations
 * 
 * Provides order management via gRPC to Order Ingress Service
 * while maintaining high performance for trading operations
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class OrderResolver {

    private final OrderClient orderClient;

    /**
     * Place a new order
     */
    @MutationMapping
    public Mono<OrderData.OrderResponse> placeOrder(@Argument OrderData.PlaceOrderInput input) {
        log.info("GraphQL mutation: placeOrder for user: {}, symbol: {}", input.getUserId(), input.getSymbol());
        
        return orderClient.placeOrder(input)
            .map(order -> OrderData.OrderResponse.builder()
                .success(true)
                .orderId(order.getOrderId())
                .message("Order placed successfully")
                .order(order)
                .errors(List.of())
                .build())
            .onErrorResume(error -> {
                log.error("Error placing order: {}", error.getMessage());
                return Mono.just(OrderData.OrderResponse.builder()
                    .success(false)
                    .orderId("")
                    .message("Failed to place order: " + error.getMessage())
                    .order(null)
                    .errors(List.of(error.getMessage()))
                    .build());
            });
    }

    /**
     * Cancel an existing order
     */
    @MutationMapping
    public Mono<OrderData.CancelResponse> cancelOrder(@Argument String orderId) {
        log.info("GraphQL mutation: cancelOrder for orderId: {}", orderId);
        
        return orderClient.cancelOrder(orderId)
            .map(success -> OrderData.CancelResponse.builder()
                .success(success)
                .message(success ? "Order cancelled successfully" : "Failed to cancel order")
                .orderId(orderId)
                .errors(success ? List.of() : List.of("Order cancellation failed"))
                .build())
            .onErrorResume(error -> {
                log.error("Error cancelling order: {}", error.getMessage());
                return Mono.just(OrderData.CancelResponse.builder()
                    .success(false)
                    .message("Failed to cancel order: " + error.getMessage())
                    .orderId(orderId)
                    .errors(List.of(error.getMessage()))
                    .build());
            });
    }

    /**
     * Modify an existing order
     */
    @MutationMapping
    public Mono<OrderData.OrderResponse> modifyOrder(@Argument OrderData.ModifyOrderInput input) {
        log.info("GraphQL mutation: modifyOrder for orderId: {}", input.getOrderId());
        
        // This would typically call a modify order service
        // For now, return a not implemented response
        return Mono.just(OrderData.OrderResponse.builder()
            .success(false)
            .orderId(input.getOrderId())
            .message("Order modification not implemented yet")
            .order(null)
            .errors(List.of("Order modification not implemented"))
            .build());
    }

    /**
     * Get order history for a user
     */
    @QueryMapping
    public Mono<List<OrderData.Order>> orderHistory(
            @Argument String userId,
            @Argument Integer limit,
            @Argument String status) {
        log.debug("GraphQL query: orderHistory for user: {}, limit: {}", userId, limit);
        
        return orderClient.getOrderHistory(userId, limit, status)
            .map(orders -> orders.stream()
                .map(order -> OrderData.Order.builder()
                    .orderId(order.getOrderId())
                    .userId(order.getUserId())
                    .symbol(order.getSymbol())
                    .side(order.getSide())
                    .quantity(order.getQuantity())
                    .price(order.getPrice())
                    .orderType(order.getOrderType())
                    .timeInForce(order.getTimeInForce())
                    .status(order.getStatus())
                    .filledQuantity(order.getFilledQuantity())
                    .averagePrice(order.getAveragePrice())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .executedAt(order.getExecutedAt())
                    .commission(order.getCommission())
                    .metadata(order.getMetadata())
                    .build())
                .toList());
    }

    /**
     * Get active orders for a user
     */
    @QueryMapping
    public Mono<List<OrderData.Order>> activeOrders(@Argument String userId) {
        log.debug("GraphQL query: activeOrders for user: {}", userId);
        
        return orderClient.getActiveOrders(userId)
            .map(orders -> orders.stream()
                .map(order -> OrderData.Order.builder()
                    .orderId(order.getOrderId())
                    .userId(order.getUserId())
                    .symbol(order.getSymbol())
                    .side(order.getSide())
                    .quantity(order.getQuantity())
                    .price(order.getPrice())
                    .orderType(order.getOrderType())
                    .timeInForce(order.getTimeInForce())
                    .status(order.getStatus())
                    .filledQuantity(order.getFilledQuantity())
                    .averagePrice(order.getAveragePrice())
                    .createdAt(order.getCreatedAt())
                    .updatedAt(order.getUpdatedAt())
                    .executedAt(order.getExecutedAt())
                    .commission(order.getCommission())
                    .metadata(order.getMetadata())
                    .build())
                .toList());
    }

    /**
     * Get order status
     */
    @QueryMapping
    public Mono<OrderData.Order> order(@Argument String orderId) {
        log.debug("GraphQL query: order for orderId: {}", orderId);
        
        return orderClient.getOrderStatus(orderId)
            .map(order -> OrderData.Order.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .orderType(order.getOrderType())
                .timeInForce(order.getTimeInForce())
                .status(order.getStatus())
                .filledQuantity(order.getFilledQuantity())
                .averagePrice(order.getAveragePrice())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .executedAt(order.getExecutedAt())
                .commission(order.getCommission())
                .metadata(order.getMetadata())
                .build());
    }

    /**
     * Subscribe to order updates
     */
    @SubscriptionMapping
    public Flux<OrderData.Order> orderUpdates(@Argument String userId) {
        log.info("GraphQL subscription: orderUpdates for user: {}", userId);
        
        return Flux.interval(Duration.ofSeconds(2)) // Update every 2 seconds
            .flatMap(tick -> orderClient.getActiveOrders(userId))
            .flatMap(Flux::fromIterable)
            .map(order -> OrderData.Order.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .symbol(order.getSymbol())
                .side(order.getSide())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .orderType(order.getOrderType())
                .timeInForce(order.getTimeInForce())
                .status(order.getStatus())
                .filledQuantity(order.getFilledQuantity())
                .averagePrice(order.getAveragePrice())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .executedAt(order.getExecutedAt())
                .commission(order.getCommission())
                .metadata(order.getMetadata())
                .build());
    }
}
