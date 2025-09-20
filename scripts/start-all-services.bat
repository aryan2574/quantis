@echo off
REM Quantis Trading Platform - Start All Services
REM Simple script to start all services

echo 🚀 Starting Quantis Trading Platform...

REM Check if Docker Compose is available
docker-compose --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Starting services with Docker Compose...
    docker-compose -f infra\docker-compose.yml up -d
    echo [INFO] Services started successfully!
    echo.
    echo Access points:
    echo   Frontend: http://localhost:3000
    echo   GraphQL: http://localhost:8080/graphql
    echo   Market Data: http://localhost:8082
) else (
    echo [WARNING] Docker Compose not available. Starting services manually...
    
    REM Start services in background
    echo [INFO] Starting Market Data Service...
    cd services\market-data-service
    start "Market Data Service" mvn spring-boot:run
    cd ..\..
    
    echo [INFO] Starting Dashboard Gateway...
    cd services\dashboard-gateway
    start "Dashboard Gateway" mvn spring-boot:run
    cd ..\..
    
    echo [INFO] Starting Trader Dashboard...
    cd services\trader-dashboard
    start "Trader Dashboard" npm run dev
    cd ..\..
    
    echo.
    echo [INFO] Services started successfully!
    echo.
    echo Access points:
    echo   Frontend: http://localhost:3000
    echo   GraphQL: http://localhost:8080/graphql
    echo   Market Data: http://localhost:8082
    echo.
    echo Services are running in separate windows. Close them to stop.
)

pause