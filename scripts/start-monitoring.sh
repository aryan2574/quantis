#!/bin/bash

# ==================== QUANTIS MONITORING STARTUP SCRIPT ====================
# This script starts the monitoring infrastructure (Prometheus, Grafana, ELK)

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}📊 Starting Quantis Monitoring Infrastructure${NC}"
echo "This will start Prometheus, Grafana, and ELK stack for monitoring"
echo ""

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Docker is running${NC}"
}

# Function to check if port is available
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        echo -e "${YELLOW}⚠️  Port $port is already in use${NC}"
        return 1
    else
        return 0
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local service_name=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}⏳ Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$url" > /dev/null 2>&1; then
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

# Function to start monitoring services
start_monitoring() {
    echo -e "${BLUE}🚀 Starting monitoring services...${NC}"
    
    # Check if monitoring directory exists
    if [ ! -d "infra/monitoring" ]; then
        echo -e "${RED}❌ Monitoring directory not found. Please run from project root.${NC}"
        exit 1
    fi
    
    # Start monitoring services
    cd infra/monitoring
    docker-compose -f docker-compose.monitoring.yml up -d
    
    echo -e "${GREEN}✅ Monitoring services started${NC}"
    echo ""
}

# Function to show service URLs
show_urls() {
    echo -e "${BLUE}🌐 Monitoring Service URLs:${NC}"
    echo ""
    echo -e "${GREEN}📊 Prometheus:${NC}     http://localhost:9090"
    echo -e "${GREEN}📈 Grafana:${NC}        http://localhost:3000 (admin/admin)"
    echo -e "${GREEN}🔍 Elasticsearch:${NC}  http://localhost:9200"
    echo -e "${GREEN}📋 Kibana:${NC}         http://localhost:5601"
    echo -e "${GREEN}🔍 Jaeger:${NC}         http://localhost:16686"
    echo ""
}

# Function to show service status
show_status() {
    echo -e "${BLUE}📊 Monitoring Service Status:${NC}"
    echo ""
    
    cd infra/monitoring
    docker-compose -f docker-compose.monitoring.yml ps
    echo ""
}

# Function to stop monitoring services
stop_monitoring() {
    echo -e "${YELLOW}🛑 Stopping monitoring services...${NC}"
    
    cd infra/monitoring
    docker-compose -f docker-compose.monitoring.yml down
    
    echo -e "${GREEN}✅ Monitoring services stopped${NC}"
}

# Function to show logs
show_logs() {
    local service=$1
    if [ -z "$service" ]; then
        echo -e "${YELLOW}Usage: $0 logs <service-name>${NC}"
        echo "Available services: prometheus, grafana, elasticsearch, logstash, kibana, jaeger"
        return 1
    fi
    
    cd infra/monitoring
    docker-compose -f docker-compose.monitoring.yml logs -f "$service"
}

# Main execution
case "${1:-start}" in
    "start")
        echo -e "${BLUE}🔧 Prerequisites Check:${NC}"
        check_docker
        
        # Check for port conflicts
        echo -e "${BLUE}🔍 Checking for port conflicts...${NC}"
        ports=(9090 3000 9200 5601 16686)
        for port in "${ports[@]}"; do
            if ! check_port $port; then
                echo -e "${YELLOW}⚠️  Some ports are in use. Services may not start properly.${NC}"
            fi
        done
        
        echo -e "${GREEN}✅ Prerequisites check completed${NC}"
        echo ""
        
        start_monitoring
        
        # Wait for services to be ready
        echo -e "${BLUE}⏳ Waiting for services to be ready...${NC}"
        wait_for_service "Prometheus" "http://localhost:9090"
        wait_for_service "Grafana" "http://localhost:3000"
        wait_for_service "Elasticsearch" "http://localhost:9200"
        wait_for_service "Kibana" "http://localhost:5601"
        
        echo ""
        show_urls
        show_status
        
        echo -e "${BLUE}📚 Next Steps:${NC}"
        echo "1. Open Grafana: http://localhost:3000 (admin/admin)"
        echo "2. Import the Quantis Trading Platform dashboard"
        echo "3. Check Prometheus: http://localhost:9090"
        echo "4. View logs in Kibana: http://localhost:5601"
        echo "5. Monitor traces in Jaeger: http://localhost:16686"
        echo "6. Stop monitoring: $0 stop"
        ;;
        
    "stop")
        stop_monitoring
        ;;
        
    "status")
        show_status
        ;;
        
    "logs")
        show_logs "$2"
        ;;
        
    "restart")
        stop_monitoring
        sleep 3
        $0 start
        ;;
        
    *)
        echo -e "${BLUE}📖 Quantis Monitoring Manager${NC}"
        echo ""
        echo "Usage: $0 <command>"
        echo ""
        echo "Commands:"
        echo "  start     - Start monitoring services (default)"
        echo "  stop      - Stop monitoring services"
        echo "  restart   - Restart monitoring services"
        echo "  status    - Show service status"
        echo "  logs <service> - Show logs for a specific service"
        echo ""
        echo "Available services: prometheus, grafana, elasticsearch, logstash, kibana, jaeger"
        echo ""
        echo "Examples:"
        echo "  $0 start"
        echo "  $0 logs prometheus"
        echo "  $0 status"
        echo "  $0 stop"
        ;;
esac
