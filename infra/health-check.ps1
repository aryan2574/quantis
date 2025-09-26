# ==================== QUANTIS HEALTH CHECK SCRIPT (PowerShell) ====================
# This script provides comprehensive health monitoring for all Docker containers

Write-Host "🔍 Quantis Platform Health Check" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host ""

# Function to check container health
function Check-ContainerHealth {
    param(
        [string]$ContainerName,
        [string]$ServiceName,
        [string]$HealthEndpoint = $null
    )
    
    Write-Host "📊 $ServiceName`: " -NoNewline
    
    # Check if container is running
    $containerExists = docker ps --format "{{.Names}}" | Select-String "^$ContainerName$"
    if (-not $containerExists) {
        Write-Host "❌ Container not running" -ForegroundColor Red
        return $false
    }
    
    # Check health status
    try {
        $healthStatus = docker inspect --format='{{.State.Health.Status}}' $ContainerName 2>$null
    } catch {
        $healthStatus = ""
    }
    
    switch ($healthStatus) {
        "healthy" {
            Write-Host "✅ Healthy" -ForegroundColor Green
        }
        "unhealthy" {
            Write-Host "❌ Unhealthy" -ForegroundColor Red
            Write-Host "   🔍 Checking logs..." -ForegroundColor Yellow
            $logs = docker logs --tail 5 $ContainerName 2>$null
            if ($logs) {
                $logs | ForEach-Object { Write-Host "   $_" -ForegroundColor Gray }
            }
        }
        "starting" {
            Write-Host "🔄 Starting..." -ForegroundColor Yellow
        }
        "" {
            Write-Host "⚠️  No health check configured" -ForegroundColor Yellow
        }
        default {
            Write-Host "❓ Unknown status: $healthStatus" -ForegroundColor Magenta
        }
    }
    
    # Test endpoint if provided
    if ($HealthEndpoint) {
        Write-Host "   🌐 Endpoint test: " -NoNewline
        try {
            $result = docker exec $ContainerName wget --no-verbose --tries=1 --spider $HealthEndpoint 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Host "✅ Accessible" -ForegroundColor Green
            } else {
                Write-Host "❌ Not accessible" -ForegroundColor Red
            }
        } catch {
            Write-Host "❌ Not accessible" -ForegroundColor Red
        }
    }
    
    Write-Host ""
    return $healthStatus -eq "healthy"
}

# Function to show container status table
function Show-ContainerStatus {
    Write-Host "📋 Container Status Overview" -ForegroundColor Cyan
    Write-Host "=============================" -ForegroundColor Cyan
    
    $containers = docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    $header = $containers | Select-Object -First 1
    $quantisContainers = $containers | Select-String -Pattern "(quantis|infra)"
    
    Write-Host $header -ForegroundColor White
    $quantisContainers | ForEach-Object { Write-Host $_ }
    Write-Host ""
}

# Function to show unhealthy containers
function Show-UnhealthyContainers {
    Write-Host "🚨 Unhealthy Containers" -ForegroundColor Red
    Write-Host "=======================" -ForegroundColor Red
    
    $allContainers = docker ps --format "{{.Names}}\t{{.Status}}" | Select-String -Pattern "(quantis|infra)"
    $unhealthyContainers = $allContainers | Where-Object { $_ -notmatch "healthy" }
    
    if ($unhealthyContainers.Count -eq 0) {
        Write-Host "✅ All containers are healthy!" -ForegroundColor Green
    } else {
        $unhealthyContainers | ForEach-Object { Write-Host $_ -ForegroundColor Red }
        Write-Host ""
        Write-Host "🔍 Detailed health checks:" -ForegroundColor Yellow
        Write-Host ""
    }
}

# Main execution
function Main {
    # Show overall container status
    Show-ContainerStatus
    
    # Check each service individually
    Write-Host "🔍 Individual Service Health Checks" -ForegroundColor Cyan
    Write-Host "===================================" -ForegroundColor Cyan
    
    # External services
    Check-ContainerHealth "infra-postgres-1" "PostgreSQL"
    Check-ContainerHealth "infra-redis-1" "Redis"
    Check-ContainerHealth "infra-kafka-1" "Kafka"
    Check-ContainerHealth "infra-cassandra-1" "Cassandra"
    Check-ContainerHealth "infra-elasticsearch-1" "Elasticsearch"
    Check-ContainerHealth "infra-zookeeper-1" "Zookeeper"
    
    Write-Host ""
    Write-Host "🏗️  Application Services" -ForegroundColor Cyan
    Write-Host "========================" -ForegroundColor Cyan
    
    # Application services
    Check-ContainerHealth "infra-auth-service-1" "Auth Service" "http://localhost:3001/health"
    Check-ContainerHealth "infra-market-data-service-1" "Market Data Service" "http://localhost:8082/actuator/health"
    Check-ContainerHealth "infra-order-ingress-1" "Order Ingress" "http://localhost:8080/actuator/health"
    Check-ContainerHealth "infra-portfolio-service-1" "Portfolio Service" "http://localhost:8082/actuator/health"
    Check-ContainerHealth "infra-risk-service-1" "Risk Service" "http://localhost:8081/actuator/health"
    Check-ContainerHealth "infra-trading-engine-1" "Trading Engine" "http://localhost:8083/actuator/health"
    Check-ContainerHealth "infra-update-service-1" "Update Service" "http://localhost:8084/actuator/health"
    Check-ContainerHealth "infra-dashboard-gateway-1" "Dashboard Gateway" "http://localhost:8085/actuator/health"
    Check-ContainerHealth "infra-cassandra-writer-service-1" "Cassandra Writer" "http://localhost:8087/actuator/health"
    Check-ContainerHealth "infra-trader-dashboard-1" "Trader Dashboard" "http://localhost:80/"
    
    Write-Host ""
    Show-UnhealthyContainers
    
    # Summary
    Write-Host "📈 Health Summary" -ForegroundColor Cyan
    Write-Host "================" -ForegroundColor Cyan
    
    $totalContainers = (docker ps --format "{{.Names}}" | Select-String -Pattern "(quantis|infra)").Count
    $healthyContainers = (docker ps --format "{{.Names}}\t{{.Status}}" | Select-String -Pattern "(quantis|infra)" | Select-String -Pattern "healthy").Count
    
    Write-Host "Total containers: $totalContainers" -ForegroundColor White
    Write-Host "Healthy containers: $healthyContainers" -ForegroundColor Green
    Write-Host "Unhealthy containers: $($totalContainers - $healthyContainers)" -ForegroundColor Red
    
    if ($healthyContainers -eq $totalContainers) {
        Write-Host ""
        Write-Host "🎉 All services are healthy! Quantis platform is ready." -ForegroundColor Green
    } else {
        Write-Host ""
        Write-Host "⚠️  Some services need attention. Check the details above." -ForegroundColor Yellow
    }
}

# Run main function
Main
