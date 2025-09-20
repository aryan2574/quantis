@echo off
REM Quantis Trading Platform - Docker Startup Script for Windows
REM This script starts the entire application using Docker Compose

echo 🐳 Starting Quantis Trading Platform with Docker...

REM Check if Docker is installed
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker is not installed. Please install Docker first.
    pause
    exit /b 1
)

REM Check if Docker Compose is installed
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker Compose is not installed. Please install Docker Compose first.
    pause
    exit /b 1
)

REM Check if .env file exists
if not exist ".env" (
    if exist "docker.env.template" (
        echo [WARNING] Creating .env file from template...
        copy docker.env.template .env >nul
        echo [WARNING] Please edit .env file with your actual API keys and secrets
        echo [WARNING] Then run this script again.
        pause
        exit /b 1
    ) else (
        echo [ERROR] .env file not found. Please create one with your API keys.
        pause
        exit /b 1
    )
)

REM Check if API keys are set
findstr /C:"your_" .env >nul 2>&1
if %errorlevel% equ 0 (
    echo [WARNING] Please update .env file with your actual API keys before starting.
    pause
    exit /b 1
)

echo [INFO] Building and starting all services...

REM Build and start all services
docker-compose -f infra\docker-compose.yml up --build -d

echo [INFO] Waiting for services to start...
timeout /t 30 /nobreak >nul

echo.
echo [INFO] 🚀 Quantis Trading Platform is starting up!
echo.
echo Access points:
echo   Frontend Dashboard: http://localhost:3000
echo   GraphQL Gateway: http://localhost:8086/graphql
echo   Market Data API: http://localhost:8082
echo   Order Ingress: http://localhost:8080
echo   Portfolio Service: http://localhost:8083
echo   Risk Service: http://localhost:8081
echo   Trading Engine: http://localhost:8084
echo   Update Service: http://localhost:8085
echo.
echo To view logs: docker-compose -f infra\docker-compose.yml logs -f
echo To stop: docker-compose -f infra\docker-compose.yml down
echo.
echo [INFO] Services are starting up. Please wait a few minutes for all services to be ready.
pause
