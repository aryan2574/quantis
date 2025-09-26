#!/bin/bash

# Quantis Trading Platform - Start All Services
# Simple script to start all services

set -e

echo "🚀 Starting Quantis Trading Platform..."

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker Compose is available
if command -v docker-compose &> /dev/null; then
    print_status "Starting services with Docker Compose..."
    docker-compose -f infra/docker-compose.yml up -d
    print_status "Services started successfully!"
    echo ""
    echo "Access points:"
    echo "  Frontend: http://localhost:3000"
    echo "  GraphQL: http://localhost:8080/graphql"
    echo "  Market Data: http://localhost:8082"
else
    print_warning "Docker Compose not available. Starting services manually..."
    
    # Start services in background
    print_status "Starting Market Data Service..."
    cd services/market-data-service
    mvn spring-boot:run &
    MARKET_DATA_PID=$!
    cd ../..
    
    print_status "Starting Dashboard Gateway..."
    cd services/dashboard-gateway
    mvn spring-boot:run &
    GATEWAY_PID=$!
    cd ../..
    
    print_status "Starting Trader Dashboard..."
    cd services/trader-dashboard
    npm run dev &
    DASHBOARD_PID=$!
    cd ../..
    
    echo ""
    print_status "Services started successfully!"
    echo "PIDs: Market Data ($MARKET_DATA_PID), Gateway ($GATEWAY_PID), Dashboard ($DASHBOARD_PID)"
    echo ""
    echo "Access points:"
    echo "  Frontend: http://localhost:3000"
    echo "  GraphQL: http://localhost:8080/graphql"
    echo "  Market Data: http://localhost:8082"
    echo ""
    echo "To stop services, press Ctrl+C or kill the PIDs"
fi