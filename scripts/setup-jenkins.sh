#!/bin/bash

# ==================== JENKINS SETUP SCRIPT ====================
# This script sets up Jenkins for the Quantis project

set -e

echo "🚀 Setting up Jenkins for Quantis Trading Platform..."

# ==================== PREREQUISITES ====================
echo "📋 Checking prerequisites..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

echo "✅ Prerequisites check passed!"

# ==================== CREATE NETWORK ====================
echo "🌐 Creating Quantis network..."
docker network create quantis-network 2>/dev/null || echo "Network already exists"

# ==================== START INFRASTRUCTURE ====================
echo "🏗️ Starting infrastructure services..."
cd infra
docker-compose up -d postgres redis cassandra elasticsearch

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 30

# ==================== START JENKINS ====================
echo "🔧 Starting Jenkins..."
cd jenkins
docker-compose -f docker-compose.jenkins.yml up -d

# Wait for Jenkins to be ready
echo "⏳ Waiting for Jenkins to be ready..."
sleep 60

# ==================== VERIFY SETUP ====================
echo "🔍 Verifying Jenkins setup..."

# Check if Jenkins is accessible
if curl -f http://localhost:8080/login > /dev/null 2>&1; then
    echo "✅ Jenkins is running and accessible!"
    echo ""
    echo "🎉 Jenkins Setup Complete!"
    echo ""
    echo "📋 Access Information:"
    echo "   URL: http://localhost:8080"
    echo "   Username: admin"
    echo "   Password: admin123"
    echo ""
    echo "🔧 Next Steps:"
    echo "   1. Open http://localhost:8080 in your browser"
    echo "   2. Login with admin/admin123"
    echo "   3. Create a new Pipeline job"
    echo "   4. Use the Jenkinsfile from infra/jenkins/Jenkinsfile"
    echo "   5. Configure Git repository"
    echo ""
    echo "📚 Jenkins Documentation:"
    echo "   - Pipeline Syntax: https://jenkins.io/doc/book/pipeline/syntax/"
    echo "   - Docker Integration: https://jenkins.io/doc/book/pipeline/docker/"
    echo "   - Maven Integration: https://jenkins.io/doc/pipeline/steps/maven/"
    echo ""
else
    echo "❌ Jenkins is not accessible. Please check the logs:"
    echo "   docker logs quantis-jenkins"
    exit 1
fi

# ==================== SHOW LOGS ====================
echo "📊 Jenkins logs (last 20 lines):"
docker logs --tail 20 quantis-jenkins

echo ""
echo "🎯 Jenkins is ready for Quantis CI/CD pipeline!"
