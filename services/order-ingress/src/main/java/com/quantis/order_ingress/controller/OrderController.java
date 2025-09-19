package com.quantis.order_ingress.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantis.order_ingress.model.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for handling order-related HTTP requests.
 * This controller receives trading orders via HTTP and publishes them to Kafka for processing.
 * 
 * @RestController: Marks this class as a REST controller - Spring will create HTTP endpoints
 * @RequestMapping("/api/orders"): All endpoints in this controller will be prefixed with /api/orders
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    /**
     * KafkaTemplate is Spring's high-level API for sending messages to Kafka topics.
     * It handles the low-level details of connecting to Kafka and sending messages.
     * 
     * <String, String> means:
     * - First String: The key type for Kafka messages (we use orderId as key)
     * - Second String: The value type for Kafka messages (we send JSON as string)
     */
    private final KafkaTemplate<String, String> kafkaTemplate;
    
    /**
     * ObjectMapper is Jackson's utility for converting Java objects to/from JSON.
     * We use it to serialize Order objects to JSON strings before sending to Kafka.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Constructor injection - Spring automatically provides the KafkaTemplate bean.
     * This is the preferred way to inject dependencies in Spring Boot.
     * 
     * @param kafkaTemplate The Kafka template bean configured by Spring Boot
     */
    public OrderController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Health check endpoint to verify the service is running.
     * 
     * @GetMapping("/health"): Creates a GET endpoint at /api/orders/health
     * @return ResponseEntity with service status information
     */
    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        // Returns a simple JSON response indicating the service is up
        return ResponseEntity.ok(Map.of(
            "status", "UP",           // Service status
            "service", "order-ingress" // Service name
        ));
    }

    /**
     * Main endpoint for placing trading orders.
     * This method:
     * 1. Validates the incoming order
     * 2. Generates a unique order ID
     * 3. Converts the order to JSON
     * 4. Sends it to Kafka for processing
     * 5. Returns a confirmation response
     * 
     * @PostMapping: Creates a POST endpoint at /api/orders (inherits from class-level @RequestMapping)
     * @param order The order object deserialized from the HTTP request body
     * @return ResponseEntity with order acceptance confirmation or error message
     * @throws Exception If there's an error serializing the order to JSON
     */
    @PostMapping
    public Mono<ResponseEntity<Object>> placeOrder(@RequestBody Order order) throws Exception {
        
        // STEP 1: VALIDATION
        // Check if the trading symbol is provided and not empty
        if (order.getSymbol() == null || order.getSymbol().isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                "error", "Symbol cannot be empty"
            )));
        }

        // Validate userId format (must be valid UUID)
        if (order.getUserId() == null || order.getUserId().isBlank()) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                "error", "User ID cannot be empty"
            )));
        }
        
        try {
            UUID.fromString(order.getUserId());
        } catch (IllegalArgumentException e) {
            return Mono.just(ResponseEntity.badRequest().body(Map.of(
                "error", "User ID must be a valid UUID format (e.g., 550e8400-e29b-41d4-a716-446655440000)"
            )));
        }

        // STEP 2: ORDER ID GENERATION
        // Generate a unique identifier for this order
        // UUID.randomUUID() creates a globally unique identifier like: "550e8400-e29b-41d4-a716-446655440000"
        String orderId = UUID.randomUUID().toString();
        order.setOrderId(orderId);

        // STEP 3: JSON SERIALIZATION
        // Convert the Order object to a JSON string for Kafka
        // Example output: {"orderId":"550e8400-e29b-41d4-a716-446655440000","userId":"u1","symbol":"AAPL",...}
        String jsonOrder = objectMapper.writeValueAsString(order);

        // STEP 4: KAFKA PUBLISHING
        // Send the order to the "orders" Kafka topic
        // Parameters:
        // - "orders": The Kafka topic name
        // - orderId: The message key (used for partitioning and ordering)
        // - jsonOrder: The message value (the JSON string)
        var future = kafkaTemplate.send("orders", orderId, jsonOrder);

        // STEP 5: RESPONSE
        // Return HTTP 200 OK after the send completes successfully (non-blocking)
        return Mono.fromFuture(future).map(r -> ResponseEntity.ok(Map.of(
            "message", "Order accepted",
            "orderId", orderId
        )));
    }
}
