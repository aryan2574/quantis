#!/bin/bash

# Quantis Trading Platform - Docker Startup Script
# This script starts the entire application using Docker Compose

set -e

echo "🐳 Starting Quantis Trading Platform with Docker..."

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

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if .env file exists
if [ ! -f ".env" ]; then
    if [ -f "docker.env.template" ]; then
        print_warning "Creating .env file from template..."
        cp docker.env.template .env
        print_warning "Please edit .env file with your actual API keys and secrets"
        print_warning "Then run this script again."
        exit 1
    else
        print_error ".env file not found. Please create one with your API keys."
        exit 1
    fi
fi

# Check if API keys are set
if grep -q "your_.*_api_key_here" .env; then
    print_warning "Please update .env file with your actual API keys before starting."
    exit 1
fi

print_status "Building and starting all services..."

# Build and start all services
docker-compose -f infra/docker-compose.yml up --build -d

print_status "Waiting for services to start..."
sleep 30

# Check service health
print_status "Checking service health..."

services=(
    "postgres:5432"
    "redis:6379"
    "kafka:9092"
    "elasticsearch:9200"
    "cassandra:9042"
)

for service in "${services[@]}"; do
    IFS=':' read -r name port <<< "$service"
    if nc -z localhost $port 2>/dev/null; then
        print_status "✅ $name is running on port $port"
    else
        print_warning "⚠️  $name is not responding on port $port"
    fi
done

echo ""
print_status "🚀 Quantis Trading Platform is starting up!"
echo ""
echo "Access points:"
echo "  Frontend Dashboard: http://localhost:3000"
echo "  GraphQL Gateway: http://localhost:8086/graphql"
echo "  Market Data API: http://localhost:8082"
echo "  Order Ingress: http://localhost:8080"
echo "  Portfolio Service: http://localhost:8083"
echo "  Risk Service: http://localhost:8081"
echo "  Trading Engine: http://localhost:8084"
echo "  Update Service: http://localhost:8085"
echo ""
echo "To view logs: docker-compose -f infra/docker-compose.yml logs -f"
echo "To stop: docker-compose -f infra/docker-compose.yml down"
echo ""
print_status "Services are starting up. Please wait a few minutes for all services to be ready."
