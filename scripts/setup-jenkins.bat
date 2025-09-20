@echo off
REM ==================== JENKINS SETUP SCRIPT (Windows) ====================
REM This script sets up Jenkins for the Quantis project

echo 🚀 Setting up Jenkins for Quantis Trading Platform...

REM ==================== PREREQUISITES ====================
echo 📋 Checking prerequisites...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker first.
    exit /b 1
)

REM Check if Docker Compose is available
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker Compose is not installed. Please install Docker Compose first.
    exit /b 1
)

echo ✅ Prerequisites check passed!

REM ==================== CREATE NETWORK ====================
echo 🌐 Creating Quantis network...
docker network create quantis-network 2>nul || echo Network already exists

REM ==================== START INFRASTRUCTURE ====================
echo 🏗️ Starting infrastructure services...
cd infra
docker-compose up -d postgres redis cassandra elasticsearch

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 30 /nobreak >nul

REM ==================== START JENKINS ====================
echo 🔧 Starting Jenkins...
cd jenkins
docker-compose -f docker-compose.jenkins.yml up -d

REM Wait for Jenkins to be ready
echo ⏳ Waiting for Jenkins to be ready...
timeout /t 60 /nobreak >nul

REM ==================== VERIFY SETUP ====================
echo 🔍 Verifying Jenkins setup...

REM Check if Jenkins is accessible
curl -f http://localhost:8080/login >nul 2>&1
if %errorlevel% equ 0 (
    echo ✅ Jenkins is running and accessible!
    echo.
    echo 🎉 Jenkins Setup Complete!
    echo.
    echo 📋 Access Information:
    echo    URL: http://localhost:8080
    echo    Username: admin
    echo    Password: admin123
    echo.
    echo 🔧 Next Steps:
    echo    1. Open http://localhost:8080 in your browser
    echo    2. Login with admin/admin123
    echo    3. Create a new Pipeline job
    echo    4. Use the Jenkinsfile from infra/jenkins/Jenkinsfile
    echo    5. Configure Git repository
    echo.
    echo 📚 Jenkins Documentation:
    echo    - Pipeline Syntax: https://jenkins.io/doc/book/pipeline/syntax/
    echo    - Docker Integration: https://jenkins.io/doc/book/pipeline/docker/
    echo    - Maven Integration: https://jenkins.io/doc/pipeline/steps/maven/
    echo.
) else (
    echo ❌ Jenkins is not accessible. Please check the logs:
    echo    docker logs quantis-jenkins
    exit /b 1
)

REM ==================== SHOW LOGS ====================
echo 📊 Jenkins logs (last 20 lines):
docker logs --tail 20 quantis-jenkins

echo.
echo 🎯 Jenkins is ready for Quantis CI/CD pipeline!

pause
