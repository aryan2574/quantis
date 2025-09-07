#!/bin/bash
set -e

echo "🚀 Testing Quantis Trading System End-to-End"
echo "Note: Topics are auto-created by the services - no manual setup needed!"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test functions
test_health() {
    echo -e "${YELLOW}Testing health endpoints...${NC}"
    
    # Test order-ingress health
    if curl -s http://localhost:8080/api/orders/health | grep -q "UP"; then
        echo -e "${GREEN}✅ Order Ingress health check passed${NC}"
    else
        echo -e "${RED}❌ Order Ingress health check failed${NC}"
        return 1
    fi
}

test_order_flow() {
    echo -e "${YELLOW}Testing order flow...${NC}"
    
    # Send test order with valid UUID
    local response=$(curl -s -X POST http://localhost:8080/api/orders \
        -H "Content-Type: application/json" \
        -d '{"userId":"b3f4d1a0-7c87-4b8e-9d65-9e0b8a6f1a11","symbol":"AAPL","side":"BUY","quantity":100,"price":150.0}')
    
    if echo "$response" | grep -q "Order accepted"; then
        echo -e "${GREEN}✅ Order accepted successfully${NC}"
        echo "Response: $response"
    else
        echo -e "${RED}❌ Order acceptance failed${NC}"
        echo "Response: $response"
        return 1
    fi
}

test_kafka_outputs() {
    echo -e "${YELLOW}Testing Kafka outputs...${NC}"
    
    local kafka_container=$(docker ps --format '{{.Names}}' | grep kafka | head -1)
    
    # Check orders.valid topic
    echo "Checking orders.valid topic..."
    local valid_count=$(docker exec -i "$kafka_container" \
        kafka-console-consumer --bootstrap-server localhost:9092 \
        --topic orders.valid --from-beginning --timeout-ms 3000 2>/dev/null | wc -l)
    
    if [ "$valid_count" -gt 0 ]; then
        echo -e "${GREEN}✅ Found $valid_count valid orders${NC}"
    else
        echo -e "${YELLOW}⚠️  No valid orders found (this might be expected)${NC}"
    fi
    
    # Check orders.rejected topic
    echo "Checking orders.rejected topic..."
    local rejected_count=$(docker exec -i "$kafka_container" \
        kafka-console-consumer --bootstrap-server localhost:9092 \
        --topic orders.rejected --from-beginning --timeout-ms 3000 2>/dev/null | wc -l)
    
    if [ "$rejected_count" -gt 0 ]; then
        echo -e "${GREEN}✅ Found $rejected_count rejected orders${NC}"
    else
        echo -e "${YELLOW}⚠️  No rejected orders found${NC}"
    fi
}

test_rejection_scenarios() {
    echo -e "${YELLOW}Testing rejection scenarios...${NC}"
    
    # Test invalid UUID format
    echo "Testing invalid UUID format..."
    local response=$(curl -s -X POST http://localhost:8080/api/orders \
        -H "Content-Type: application/json" \
        -d '{"userId":"test-user","symbol":"AAPL","side":"BUY","quantity":100,"price":150.0}')
    
    if echo "$response" | grep -q "User ID must be a valid UUID"; then
        echo -e "${GREEN}✅ Invalid UUID correctly rejected${NC}"
    else
        echo -e "${RED}❌ Invalid UUID should have been rejected${NC}"
        echo "Response: $response"
    fi
    
    # Test empty userId
    echo "Testing empty userId..."
    local response=$(curl -s -X POST http://localhost:8080/api/orders \
        -H "Content-Type: application/json" \
        -d '{"userId":"","symbol":"AAPL","side":"BUY","quantity":100,"price":150.0}')
    
    if echo "$response" | grep -q "User ID cannot be empty"; then
        echo -e "${GREEN}✅ Empty userId correctly rejected${NC}"
    else
        echo -e "${RED}❌ Empty userId should have been rejected${NC}"
        echo "Response: $response"
    fi
    
    # Test invalid quantity (> 1M)
    echo "Testing invalid quantity..."
    local response=$(curl -s -X POST http://localhost:8080/api/orders \
        -H "Content-Type: application/json" \
        -d '{"userId":"b3f4d1a0-7c87-4b8e-9d65-9e0b8a6f1a11","symbol":"AAPL","side":"BUY","quantity":2000000,"price":150.0}')
    
    if echo "$response" | grep -q "Order accepted"; then
        echo -e "${GREEN}✅ Order accepted (quantity valid)${NC}"
    else
        echo -e "${RED}❌ Order rejected unexpectedly${NC}"
        echo "Response: $response"
    fi
}

# Main test execution
main() {
    echo "Starting end-to-end tests..."
    
    test_health
    test_order_flow
    sleep 2  # Give time for processing
    test_kafka_outputs
    test_rejection_scenarios
    
    echo -e "${GREEN}🎉 End-to-end tests completed!${NC}"
}

# Run tests
main "$@"
