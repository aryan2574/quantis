package com.quantis.order_ingress.grpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.order_ingress.grpc.OrderIngressProto.*;
import com.quantis.order_ingress.model.Order;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * gRPC service implementation for Order Ingress
 * Provides high-performance order placement for algorithmic trading clients
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class OrderIngressGrpcService extends OrderIngressServiceGrpc.OrderIngressServiceImplBase {
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    // Track order status for streaming updates
    private final Map<String, OrderStatus> orderStatusMap = new ConcurrentHashMap<>();
    
    @Override
    public void placeOrder(PlaceOrderRequest request, StreamObserver<PlaceOrderResponse> responseObserver) {
        try {
            log.debug("Received gRPC order request: user={}, symbol={}, side={}, qty={}, price={}", 
                     request.getUserId(), request.getSymbol(), request.getSide(), 
                     request.getQuantity(), request.getPrice());
            
            // Validate request
            if (!isValidOrderRequest(request)) {
                responseObserver.onNext(PlaceOrderResponse.newBuilder()
                    .setOrderId("")
                    .setStatus("REJECTED")
                    .setMessage("Invalid order request")
                    .setTimestamp(System.currentTimeMillis())
                    .build());
                responseObserver.onCompleted();
                return;
            }
            
            // Generate order ID
            String orderId = UUID.randomUUID().toString();
            
            // Create order object
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(request.getUserId());
            order.setSymbol(request.getSymbol());
            order.setSide(request.getSide());
            order.setQuantity(request.getQuantity());
            order.setPrice(request.getPrice());
            
            // Track order status
            orderStatusMap.put(orderId, OrderStatus.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .status("PENDING")
                .symbol(request.getSymbol())
                .side(request.getSide())
                .quantity(request.getQuantity())
                .price(request.getPrice())
                .timestamp(System.currentTimeMillis())
                .build());
            
            // Publish to Kafka
            String orderJson = objectMapper.writeValueAsString(order);
            kafkaTemplate.send("orders", orderId, orderJson);
            
            log.info("Order placed via gRPC: orderId={}, user={}, symbol={}", 
                    orderId, request.getUserId(), request.getSymbol());
            
            // Send response
            responseObserver.onNext(PlaceOrderResponse.newBuilder()
                .setOrderId(orderId)
                .setStatus("ACCEPTED")
                .setMessage("Order accepted for processing")
                .setTimestamp(System.currentTimeMillis())
                .build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error processing gRPC order request", e);
            responseObserver.onNext(PlaceOrderResponse.newBuilder()
                .setOrderId("")
                .setStatus("REJECTED")
                .setMessage("Internal server error: " + e.getMessage())
                .setTimestamp(System.currentTimeMillis())
                .build());
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void cancelOrder(CancelOrderRequest request, StreamObserver<CancelOrderResponse> responseObserver) {
        try {
            log.debug("Received gRPC cancel request: orderId={}, user={}", 
                     request.getOrderId(), request.getUserId());
            
            OrderStatus orderStatus = orderStatusMap.get(request.getOrderId());
            if (orderStatus == null || !orderStatus.getUserId().equals(request.getUserId())) {
                responseObserver.onNext(CancelOrderResponse.newBuilder()
                    .setOrderId(request.getOrderId())
                    .setStatus("NOT_FOUND")
                    .setMessage("Order not found or access denied")
                    .setTimestamp(System.currentTimeMillis())
                    .build());
                responseObserver.onCompleted();
                return;
            }
            
            // Update status
            orderStatus.setStatus("CANCELLED");
            orderStatus.setLastUpdated(System.currentTimeMillis());
            
            // Publish cancellation to Kafka
            String cancelMessage = String.format(
                "{\"orderId\":\"%s\",\"userId\":\"%s\",\"action\":\"CANCEL\",\"timestamp\":%d}",
                request.getOrderId(), request.getUserId(), System.currentTimeMillis()
            );
            kafkaTemplate.send("order.cancellations", request.getOrderId(), cancelMessage);
            
            log.info("Order cancelled via gRPC: orderId={}, user={}", 
                    request.getOrderId(), request.getUserId());
            
            responseObserver.onNext(CancelOrderResponse.newBuilder()
                .setOrderId(request.getOrderId())
                .setStatus("CANCELLED")
                .setMessage("Order cancelled successfully")
                .setTimestamp(System.currentTimeMillis())
                .build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error processing gRPC cancel request", e);
            responseObserver.onNext(CancelOrderResponse.newBuilder()
                .setOrderId(request.getOrderId())
                .setStatus("FAILED")
                .setMessage("Internal server error: " + e.getMessage())
                .setTimestamp(System.currentTimeMillis())
                .build());
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void getOrderStatus(GetOrderStatusRequest request, StreamObserver<GetOrderStatusResponse> responseObserver) {
        try {
            OrderStatus orderStatus = orderStatusMap.get(request.getOrderId());
            if (orderStatus == null || !orderStatus.getUserId().equals(request.getUserId())) {
                responseObserver.onNext(GetOrderStatusResponse.newBuilder()
                    .setOrderId(request.getOrderId())
                    .setStatus("NOT_FOUND")
                    .setTimestamp(System.currentTimeMillis())
                    .build());
                responseObserver.onCompleted();
                return;
            }
            
            responseObserver.onNext(GetOrderStatusResponse.newBuilder()
                .setOrderId(orderStatus.getOrderId())
                .setStatus(orderStatus.getStatus())
                .setSymbol(orderStatus.getSymbol())
                .setSide(orderStatus.getSide())
                .setQuantity(orderStatus.getQuantity())
                .setPrice(orderStatus.getPrice())
                .setFilledQuantity(orderStatus.getFilledQuantity())
                .setAveragePrice(orderStatus.getAveragePrice())
                .setTimestamp(orderStatus.getTimestamp())
                .setLastUpdated(orderStatus.getLastUpdated())
                .build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error getting order status", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void streamOrderUpdates(StreamOrderUpdatesRequest request, StreamObserver<OrderUpdate> responseObserver) {
        log.info("Starting order update stream for user: {}", request.getUserId());
        
        // Create a flux that emits order updates
        Flux<OrderUpdate> orderUpdates = Flux.create(sink -> {
            // In a real implementation, this would subscribe to Kafka topics
            // For now, we'll simulate periodic updates
            // Implement real-time streaming from Kafka
            
            // Keep the stream alive
            sink.onRequest(n -> {
                // Send periodic heartbeats
                sink.next(OrderUpdate.newBuilder()
                    .setOrderId("heartbeat")
                    .setUserId(request.getUserId())
                    .setStatus("HEARTBEAT")
                    .setTimestamp(System.currentTimeMillis())
                    .setUpdateType("HEARTBEAT")
                    .build());
            });
        });
        
        orderUpdates.subscribe(
            update -> {
                try {
                    responseObserver.onNext(update);
                } catch (Exception e) {
                    log.error("Error sending order update", e);
                }
            },
            error -> {
                log.error("Error in order update stream", error);
                responseObserver.onError(error);
            },
            () -> {
                log.info("Order update stream completed for user: {}", request.getUserId());
                responseObserver.onCompleted();
            }
        );
    }
    
    @Override
    public void placeBulkOrders(PlaceBulkOrdersRequest request, StreamObserver<PlaceBulkOrdersResponse> responseObserver) {
        try {
            log.info("Received bulk order request: user={}, count={}", 
                    request.getUserId(), request.getOrdersCount());
            
            PlaceBulkOrdersResponse.Builder responseBuilder = PlaceBulkOrdersResponse.newBuilder()
                .setTimestamp(System.currentTimeMillis());
            
            for (OrderRequest orderRequest : request.getOrdersList()) {
                try {
                    // Generate order ID
                    String orderId = UUID.randomUUID().toString();
                    
                    // Create order object
                    Order order = new Order();
                    order.setOrderId(orderId);
                    order.setUserId(request.getUserId());
                    order.setSymbol(orderRequest.getSymbol());
                    order.setSide(orderRequest.getSide());
                    order.setQuantity(orderRequest.getQuantity());
                    order.setPrice(orderRequest.getPrice());
                    
                    // Track order status
                    orderStatusMap.put(orderId, OrderStatus.builder()
                        .orderId(orderId)
                        .userId(request.getUserId())
                        .status("PENDING")
                        .symbol(orderRequest.getSymbol())
                        .side(orderRequest.getSide())
                        .quantity(orderRequest.getQuantity())
                        .price(orderRequest.getPrice())
                        .timestamp(System.currentTimeMillis())
                        .build());
                    
                    // Publish to Kafka
                    String orderJson = objectMapper.writeValueAsString(order);
                    kafkaTemplate.send("orders", orderId, orderJson);
                    
                    responseBuilder.addResults(OrderResult.newBuilder()
                        .setClientOrderId(orderRequest.getClientOrderId())
                        .setOrderId(orderId)
                        .setStatus("ACCEPTED")
                        .setMessage("Order accepted")
                        .build());
                    
                } catch (Exception e) {
                    log.error("Error processing bulk order", e);
                    responseBuilder.addResults(OrderResult.newBuilder()
                        .setClientOrderId(orderRequest.getClientOrderId())
                        .setOrderId("")
                        .setStatus("REJECTED")
                        .setMessage("Error: " + e.getMessage())
                        .build());
                }
            }
            
            log.info("Processed bulk orders: user={}, total={}", 
                    request.getUserId(), request.getOrdersCount());
            
            responseObserver.onNext(responseBuilder.build());
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error processing bulk orders", e);
            responseObserver.onError(e);
        }
    }
    
    private boolean isValidOrderRequest(PlaceOrderRequest request) {
        return request.getUserId() != null && !request.getUserId().isEmpty()
            && request.getSymbol() != null && !request.getSymbol().isEmpty()
            && request.getSide() != null && (request.getSide().equals("BUY") || request.getSide().equals("SELL"))
            && request.getQuantity() > 0
            && request.getPrice() > 0;
    }
    
    // Inner class for order status tracking
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class OrderStatus {
        private String orderId;
        private String userId;
        private String status;
        private String symbol;
        private String side;
        private long quantity;
        private double price;
        private long filledQuantity;
        private double averagePrice;
        private long timestamp;
        private long lastUpdated;
    }
}
