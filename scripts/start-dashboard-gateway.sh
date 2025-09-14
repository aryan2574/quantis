#!/bin/bash

# ==================== DASHBOARD GATEWAY STARTUP SCRIPT ====================
# Starts the GraphQL Gateway Service for Trading Dashboard

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICE_NAME="dashboard-gateway"
SERVICE_DIR="services/dashboard-gateway"
LOG_FILE="logs/dashboard-gateway.log"
PID_FILE="logs/dashboard-gateway.pid"
PORT=8085

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if service is running
check_service() {
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            return 0
        else
            rm -f "$PID_FILE"
            return 1
        fi
    fi
    return 1
}

# Function to check if port is available
check_port() {
    if lsof -Pi :$PORT -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 1
    fi
    return 0
}

# Function to wait for service to be ready
wait_for_service() {
    local max_attempts=30
    local attempt=1
    
    print_status "Waiting for $SERVICE_NAME to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s http://localhost:$PORT/actuator/health > /dev/null 2>&1; then
            print_success "$SERVICE_NAME is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    print_error "$SERVICE_NAME failed to start within expected time"
    return 1
}

# Function to start the service
start_service() {
    print_status "Starting $SERVICE_NAME..."
    
    # Check if already running
    if check_service; then
        print_warning "$SERVICE_NAME is already running (PID: $(cat $PID_FILE))"
        return 0
    fi
    
    # Check if port is available
    if ! check_port; then
        print_error "Port $PORT is already in use"
        return 1
    fi
    
    # Create logs directory
    mkdir -p logs
    
    # Change to service directory
    cd "$SERVICE_DIR" || {
        print_error "Failed to change to directory: $SERVICE_DIR"
        exit 1
    }
    
    # Start the service
    print_status "Starting $SERVICE_NAME on port $PORT..."
    nohup ./mvnw spring-boot:run > "../$LOG_FILE" 2>&1 &
    echo $! > "../$PID_FILE"
    
    # Change back to root directory
    cd ..
    
    # Wait for service to be ready
    if wait_for_service; then
        print_success "$SERVICE_NAME started successfully!"
        print_status "GraphQL endpoint: http://localhost:$PORT/graphql"
        print_status "GraphiQL interface: http://localhost:$PORT/graphiql"
        print_status "Health check: http://localhost:$PORT/actuator/health"
        print_status "Logs: $LOG_FILE"
        return 0
    else
        print_error "Failed to start $SERVICE_NAME"
        return 1
    fi
}

# Function to stop the service
stop_service() {
    print_status "Stopping $SERVICE_NAME..."
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat "$PID_FILE")
        if ps -p "$PID" > /dev/null 2>&1; then
            kill "$PID"
            sleep 5
            
            if ps -p "$PID" > /dev/null 2>&1; then
                print_warning "Service didn't stop gracefully, forcing..."
                kill -9 "$PID"
            fi
            
            rm -f "$PID_FILE"
            print_success "$SERVICE_NAME stopped"
        else
            print_warning "$SERVICE_NAME is not running"
            rm -f "$PID_FILE"
        fi
    else
        print_warning "$SERVICE_NAME is not running"
    fi
}

# Function to restart the service
restart_service() {
    print_status "Restarting $SERVICE_NAME..."
    stop_service
    sleep 2
    start_service
}

# Function to show service status
show_status() {
    print_status "$SERVICE_NAME Status:"
    
    if check_service; then
        PID=$(cat "$PID_FILE")
        print_success "Service is running (PID: $PID)"
        
        # Show port status
        if check_port; then
            print_warning "Port $PORT is not listening"
        else
            print_success "Port $PORT is listening"
        fi
        
        # Show health status
        if curl -s http://localhost:$PORT/actuator/health > /dev/null 2>&1; then
            print_success "Health check: OK"
        else
            print_warning "Health check: FAILED"
        fi
        
        # Show recent logs
        if [ -f "$LOG_FILE" ]; then
            print_status "Recent logs (last 10 lines):"
            tail -10 "$LOG_FILE"
        fi
    else
        print_error "Service is not running"
    fi
}

# Function to show logs
show_logs() {
    if [ -f "$LOG_FILE" ]; then
        print_status "Showing logs for $SERVICE_NAME:"
        tail -f "$LOG_FILE"
    else
        print_error "Log file not found: $LOG_FILE"
    fi
}

# Function to show help
show_help() {
    echo "Usage: $0 {start|stop|restart|status|logs|help}"
    echo ""
    echo "Commands:"
    echo "  start   - Start the $SERVICE_NAME"
    echo "  stop    - Stop the $SERVICE_NAME"
    echo "  restart - Restart the $SERVICE_NAME"
    echo "  status  - Show service status"
    echo "  logs    - Show service logs"
    echo "  help    - Show this help message"
    echo ""
    echo "Service Information:"
    echo "  Name: $SERVICE_NAME"
    echo "  Port: $PORT"
    echo "  GraphQL: http://localhost:$PORT/graphql"
    echo "  GraphiQL: http://localhost:$PORT/graphiql"
    echo "  Health: http://localhost:$PORT/actuator/health"
}

# Main script logic
case "$1" in
    start)
        start_service
        ;;
    stop)
        stop_service
        ;;
    restart)
        restart_service
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        print_error "Invalid command: $1"
        show_help
        exit 1
        ;;
esac
