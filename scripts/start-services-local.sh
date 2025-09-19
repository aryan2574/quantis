#!/bin/bash

# ==================== QUANTIS LOCAL SERVICES STARTUP SCRIPT ====================
# This script helps you start all Quantis services locally for development and learning

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting Quantis Trading System Services Locally${NC}"
echo "This will help you learn how each service works step by step"
echo ""

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for a service to be ready
wait_for_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}⏳ Waiting for $service_name to be ready on port $port...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if check_port $port; then
            echo -e "${GREEN}✅ $service_name is ready!${NC}"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start within timeout${NC}"
    return 1
}

# Function to start a service
start_service() {
    local service_name=$1
    local service_path=$2
    local port=$3
    
    echo -e "${BLUE}📦 Starting $service_name...${NC}"
    
    if check_port $port; then
        echo -e "${YELLOW}⚠️  Port $port is already in use. Skipping $service_name${NC}"
        return 0
    fi
    
    # Create logs directory if it doesn't exist
    mkdir -p "$service_path/logs"
    
    # Start the service in background
    cd "$service_path"
    ./mvnw spring-boot:run > "logs/$service_name.log" 2>&1 &
    local pid=$!
    echo $pid > "logs/$service_name.pid"
    
    # Wait for service to be ready
    if wait_for_service "$service_name" "$port"; then
        echo -e "${GREEN}✅ $service_name started successfully (PID: $pid)${NC}"
        echo "   Logs: $service_path/logs/$service_name.log"
        echo "   PID: $pid"
        echo ""
    else
        echo -e "${RED}❌ Failed to start $service_name${NC}"
        echo "   Check logs: $service_path/logs/$service_name.log"
        return 1
    fi
}

# Function to stop all services
stop_services() {
    echo -e "${YELLOW}🛑 Stopping all services...${NC}"
    
    for service in order-ingress portfolio-service risk-service trading-engine; do
        local pid_file="services/$service/logs/$service.pid"
        if [ -f "$pid_file" ]; then
            local pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                echo -e "${YELLOW}   Stopping $service (PID: $pid)...${NC}"
                kill "$pid"
                rm "$pid_file"
            fi
        fi
    done
    
    echo -e "${GREEN}✅ All services stopped${NC}"
}

# Function to show service status
show_status() {
    echo -e "${BLUE}📊 Service Status:${NC}"
    echo ""
    
    for service in order-ingress portfolio-service risk-service trading-engine; do
        local port
        case $service in
            order-ingress) port=8080 ;;
            portfolio-service) port=8082 ;;
            risk-service) port=8081 ;;
            trading-engine) port=8083 ;;
        esac
        
        if check_port $port; then
            echo -e "${GREEN}✅ $service - Running on port $port${NC}"
        else
            echo -e "${RED}❌ $service - Not running${NC}"
        fi
    done
    echo ""
}

# Function to show logs
show_logs() {
    local service=$1
    if [ -z "$service" ]; then
        echo -e "${YELLOW}Usage: $0 logs <service-name>${NC}"
        echo "Available services: order-ingress, portfolio-service, risk-service, trading-engine"
        return 1
    fi
    
    local log_file="services/$service/logs/$service.log"
    if [ -f "$log_file" ]; then
        echo -e "${BLUE}📋 Showing logs for $service:${NC}"
        echo ""
        tail -f "$log_file"
    else
        echo -e "${RED}❌ Log file not found: $log_file${NC}"
        return 1
    fi
}

# Main execution
case "${1:-start}" in
    "start")
        echo -e "${BLUE}🔧 Prerequisites Check:${NC}"
        
        # Check if required tools are available
        if ! command -v java &> /dev/null; then
            echo -e "${RED}❌ Java is not installed or not in PATH${NC}"
            exit 1
        fi
        
        if ! command -v redis-server &> /dev/null; then
            echo -e "${YELLOW}⚠️  Redis server not found. Please start Redis manually:${NC}"
            echo "   redis-server"
            echo ""
        fi
        
        echo -e "${GREEN}✅ Prerequisites check completed${NC}"
        echo ""
        
        # Start services in order
        start_service "portfolio-service" "services/portfolio-service" 8082
        start_service "order-ingress" "services/order-ingress" 8080
        start_service "risk-service" "services/risk-service" 8081
        start_service "trading-engine" "services/trading-engine" 8083
        
        echo -e "${GREEN}🎉 All services started successfully!${NC}"
        echo ""
        show_status
        
        echo -e "${BLUE}📚 Next Steps:${NC}"
        echo "1. Test Order Ingress: curl -X POST http://localhost:8080/api/orders -H 'Content-Type: application/json' -d '{\"userId\":\"550e8400-e29b-41d4-a716-446655440000\",\"symbol\":\"AAPL\",\"side\":\"BUY\",\"quantity\":100,\"price\":150.0}'"
        echo "2. Check Portfolio Service: curl http://localhost:8082/actuator/health"
        echo "3. Check Risk Service: curl http://localhost:8081/actuator/health"
        echo "4. Check Trading Engine: curl http://localhost:8083/actuator/health"
        echo "5. View logs: $0 logs <service-name>"
        echo "6. Stop services: $0 stop"
        ;;
        
    "stop")
        stop_services
        ;;
        
    "status")
        show_status
        ;;
        
    "logs")
        show_logs "$2"
        ;;
        
    "restart")
        stop_services
        sleep 3
        $0 start
        ;;
        
    *)
        echo -e "${BLUE}📖 Quantis Local Services Manager${NC}"
        echo ""
        echo "Usage: $0 <command>"
        echo ""
        echo "Commands:"
        echo "  start     - Start all services (default)"
        echo "  stop      - Stop all services"
        echo "  restart   - Restart all services"
        echo "  status    - Show service status"
        echo "  logs <service> - Show logs for a specific service"
        echo ""
        echo "Available services: order-ingress, portfolio-service, risk-service, trading-engine"
        echo ""
        echo "Examples:"
        echo "  $0 start"
        echo "  $0 logs order-ingress"
        echo "  $0 status"
        echo "  $0 stop"
        ;;
esac
