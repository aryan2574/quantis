package com.quantis.dashboard_gateway.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

/**
 * DataLoader Performance Example
 * 
 * This class demonstrates the performance benefits of using DataLoaders
 * in GraphQL by comparing N+1 queries vs batched queries.
 * 
 * Performance Comparison:
 * - Without DataLoader: 1 + N queries (could be 100+ requests)
 * - With DataLoader: 3 batched queries (Portfolio + Orders + Market Data)
 * 
 * Example Scenario:
 * Dashboard query for 10 users, each with 5 positions and 3 active orders
 * - Without DataLoader: 1 + (10 * 5) + (10 * 3) = 81 queries
 * - With DataLoader: 3 batched queries
 */
@Component
@Slf4j
public class DataLoaderPerformanceExample {

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Simulate N+1 Query Problem (Without DataLoader)
     * 
     * This demonstrates the inefficient approach where each field resolution
     * triggers a separate service call.
     */
    public void demonstrateNPlusOneProblem() {
        log.info("=== Demonstrating N+1 Query Problem ===");
        
        List<String> userIds = List.of("user1", "user2", "user3", "user4", "user5");
        long startTime = System.currentTimeMillis();
        
        // Simulate GraphQL query: portfolio { positions { marketData } }
        CompletableFuture<Void> portfolioQuery = CompletableFuture.runAsync(() -> {
            log.info("1. Portfolio query for {} users", userIds.size());
            // 1 query to portfolio service
            simulateServiceCall("Portfolio Service", 50);
        }, executor);
        
        CompletableFuture<Void> positionsQuery = CompletableFuture.runAsync(() -> {
            log.info("2. Positions query for {} users", userIds.size());
            // N queries to portfolio service (one per user)
            userIds.forEach(userId -> {
                simulateServiceCall("Portfolio Service - Positions for " + userId, 30);
            });
        }, executor);
        
        CompletableFuture<Void> marketDataQuery = CompletableFuture.runAsync(() -> {
            log.info("3. Market data query for {} positions", userIds.size() * 5); // 5 positions per user
            // N*M queries to market data service (one per position)
            userIds.forEach(userId -> {
                IntStream.range(0, 5).forEach(i -> {
                    simulateServiceCall("Market Data Service - " + userId + "_position_" + i, 20);
                });
            });
        }, executor);
        
        CompletableFuture.allOf(portfolioQuery, positionsQuery, marketDataQuery)
            .thenRun(() -> {
                long endTime = System.currentTimeMillis();
                long totalQueries = 1 + userIds.size() + (userIds.size() * 5);
                log.info("N+1 Problem Results:");
                log.info("- Total queries: {}", totalQueries);
                log.info("- Total time: {}ms", endTime - startTime);
                log.info("- Average time per query: {}ms", (endTime - startTime) / totalQueries);
            });
    }

    /**
     * Simulate DataLoader Optimization (With DataLoader)
     * 
     * This demonstrates the efficient approach where multiple requests
     * are batched into single service calls.
     */
    public void demonstrateDataLoaderOptimization() {
        log.info("=== Demonstrating DataLoader Optimization ===");
        
        List<String> userIds = List.of("user1", "user2", "user3", "user4", "user5");
        long startTime = System.currentTimeMillis();
        
        // Simulate GraphQL query: portfolio { positions { marketData } }
        CompletableFuture<Void> portfolioBatch = CompletableFuture.runAsync(() -> {
            log.info("1. Batched portfolio query for {} users", userIds.size());
            // 1 batched query to portfolio service
            simulateServiceCall("Portfolio Service - Batch", 50);
        }, executor);
        
        CompletableFuture<Void> positionsBatch = CompletableFuture.runAsync(() -> {
            log.info("2. Batched positions query for {} users", userIds.size());
            // 1 batched query to portfolio service
            simulateServiceCall("Portfolio Service - Positions Batch", 30);
        }, executor);
        
        CompletableFuture<Void> marketDataBatch = CompletableFuture.runAsync(() -> {
            log.info("3. Batched market data query for {} symbols", userIds.size() * 5);
            // 1 batched query to market data service
            simulateServiceCall("Market Data Service - Batch", 20);
        }, executor);
        
        CompletableFuture.allOf(portfolioBatch, positionsBatch, marketDataBatch)
            .thenRun(() -> {
                long endTime = System.currentTimeMillis();
                int totalQueries = 3; // Only 3 batched queries
                log.info("DataLoader Optimization Results:");
                log.info("- Total queries: {}", totalQueries);
                log.info("- Total time: {}ms", endTime - startTime);
                log.info("- Average time per query: {}ms", (endTime - startTime) / totalQueries);
            });
    }

    /**
     * Demonstrate Real Trading Platform Scenario
     * 
     * This shows a realistic scenario where a dashboard loads data for
     * multiple users with complex relationships.
     */
    public void demonstrateTradingPlatformScenario() {
        log.info("=== Trading Platform Dashboard Scenario ===");
        
        // Simulate a trading platform dashboard with:
        // - 20 users
        // - Each user has 10 positions
        // - Each user has 5 active orders
        // - Each position needs market data
        // - Each order needs market data
        
        List<String> userIds = IntStream.rangeClosed(1, 20)
            .mapToObj(i -> "user" + i)
            .toList();
        
        log.info("Scenario: {} users, {} positions, {} orders", 
            userIds.size(), userIds.size() * 10, userIds.size() * 5);
        
        // Without DataLoader
        long startTime = System.currentTimeMillis();
        int totalQueriesWithoutDataLoader = 1 + // Portfolio overview
            userIds.size() + // User portfolios
            userIds.size() * 10 + // Positions
            userIds.size() * 5 + // Active orders
            userIds.size() * 10 + // Market data for positions
            userIds.size() * 5; // Market data for orders
        
        log.info("Without DataLoader: {} queries", totalQueriesWithoutDataLoader);
        
        // With DataLoader
        int totalQueriesWithDataLoader = 3; // 3 batched queries
        
        log.info("With DataLoader: {} queries", totalQueriesWithDataLoader);
        
        // Calculate performance improvement
        double improvement = (double) totalQueriesWithoutDataLoader / totalQueriesWithDataLoader;
        log.info("Performance improvement: {}x fewer queries", improvement);
        
        // Estimate time savings (assuming 50ms per query)
        long timeWithoutDataLoader = totalQueriesWithoutDataLoader * 50;
        long timeWithDataLoader = totalQueriesWithDataLoader * 50;
        long timeSaved = timeWithoutDataLoader - timeWithDataLoader;
        
        log.info("Estimated time savings: {}ms ({}s)", timeSaved, timeSaved / 1000.0);
    }

    /**
     * Demonstrate GraphQL Query Examples
     */
    public void demonstrateGraphQLQueries() {
        log.info("=== GraphQL Query Examples ===");
        
        // Example 1: Simple query (no N+1 problem)
        log.info("Example 1: Simple Portfolio Query");
        log.info("Query: { portfolio(userId: \"user1\") { totalValue cashBalance } }");
        log.info("Queries: 1 (no optimization needed)");
        
        // Example 2: N+1 problem
        log.info("\nExample 2: N+1 Problem Query");
        log.info("Query: {");
        log.info("  portfolio(userId: \"user1\") {");
        log.info("    positions {");
        log.info("      symbol");
        log.info("      marketData { bestBid bestAsk }");
        log.info("    }");
        log.info("  }");
        log.info("}");
        log.info("Without DataLoader: 1 + N queries (N = number of positions)");
        log.info("With DataLoader: 2 batched queries");
        
        // Example 3: Complex dashboard query
        log.info("\nExample 3: Complex Dashboard Query");
        log.info("Query: {");
        log.info("  portfolio(userId: \"user1\") {");
        log.info("    totalValue");
        log.info("    positions {");
        log.info("      symbol");
        log.info("      marketData { bestBid bestAsk lastPrice }");
        log.info("    }");
        log.info("  }");
        log.info("  activeOrders(userId: \"user1\") {");
        log.info("    symbol");
        log.info("    marketData { bestBid bestAsk }");
        log.info("  }");
        log.info("}");
        log.info("Without DataLoader: 1 + N + M queries");
        log.info("With DataLoader: 3 batched queries");
    }

    /**
     * Simulate a service call with latency
     */
    private void simulateServiceCall(String serviceName, int latencyMs) {
        try {
            Thread.sleep(latencyMs);
            log.debug("Service call completed: {} ({}ms)", serviceName, latencyMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Run all demonstrations
     */
    public void runAllDemonstrations() {
        demonstrateGraphQLQueries();
        demonstrateNPlusOneProblem();
        
        // Wait a bit between demonstrations
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        demonstrateDataLoaderOptimization();
        
        // Wait a bit before final demonstration
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        demonstrateTradingPlatformScenario();
    }
}
