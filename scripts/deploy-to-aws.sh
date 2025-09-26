#!/bin/bash

# ==================== QUANTIS AWS DEPLOYMENT SCRIPT ====================
# Complete deployment of Quantis Trading Platform to AWS EKS

set -e

echo "🚀 Deploying Quantis Trading Platform to AWS EKS..."

# ==================== PREREQUISITES ====================
echo "📋 Checking prerequisites..."

# Check if kubectl is configured
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ kubectl is not configured. Please run:"
    echo "   aws eks update-kubeconfig --region us-west-2 --name quantis-trading-cluster"
    exit 1
fi

# Check if cluster exists
if ! kubectl get nodes &> /dev/null; then
    echo "❌ EKS cluster not accessible. Please check your cluster setup."
    exit 1
fi

echo "✅ Prerequisites check passed!"

# ==================== BUILD DOCKER IMAGES ====================
echo "🐳 Building Docker images..."

# Build all service images
services=(
    "auth-service"
    "market-data-service"
    "order-ingress"
    "portfolio-service"
    "risk-service"
    "trading-engine"
    "update-service"
    "dashboard-gateway"
    "cassandra-writer-service"
    "elasticsearch-writer-service"
    "postgres-writer-service"
    "trader-dashboard"
)

for service in "${services[@]}"; do
    echo "🔨 Building $service..."
    docker build -t quantis/$service:latest services/$service/
    echo "✅ $service built successfully!"
done

echo "✅ All Docker images built!"

# ==================== PUSH TO ECR ====================
echo "📤 Pushing images to ECR..."

# Get AWS account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION="us-west-2"
ECR_REGISTRY="$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com"

# Create ECR repositories
for service in "${services[@]}"; do
    echo "📦 Creating ECR repository for $service..."
    aws ecr create-repository --repository-name quantis/$service --region $AWS_REGION 2>/dev/null || echo "Repository already exists"
done

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY

# Tag and push images
for service in "${services[@]}"; do
    echo "📤 Pushing $service to ECR..."
    docker tag quantis/$service:latest $ECR_REGISTRY/quantis/$service:latest
    docker push $ECR_REGISTRY/quantis/$service:latest
    echo "✅ $service pushed to ECR!"
done

echo "✅ All images pushed to ECR!"

# ==================== UPDATE KUBERNETES MANIFESTS ====================
echo "🔧 Updating Kubernetes manifests..."

# Update image references in deployments
for service in "${services[@]}"; do
    if [ -f "k8s/${service}-deployment.yaml" ]; then
        sed -i "s|image: quantis/$service:latest|image: $ECR_REGISTRY/quantis/$service:latest|g" k8s/${service}-deployment.yaml
    fi
done

# Update dashboard deployment
sed -i "s|image: quantis/trader-dashboard:latest|image: $ECR_REGISTRY/quantis/trader-dashboard:latest|g" k8s/dashboard-deployment.yaml

echo "✅ Kubernetes manifests updated!"

# ==================== DEPLOY TO KUBERNETES ====================
echo "🚀 Deploying to Kubernetes..."

# Apply all manifests
kubectl apply -f k8s/

echo "✅ Quantis Trading Platform deployed to Kubernetes!"

# ==================== WAIT FOR DEPLOYMENT ====================
echo "⏳ Waiting for deployment to complete..."

# Wait for pods to be ready
kubectl wait --for=condition=ready pod --all -n quantis-trading --timeout=600s

echo "✅ All pods are ready!"

# ==================== VERIFY DEPLOYMENT ====================
echo "🔍 Verifying deployment..."

# Show pod status
echo "📊 Pod status:"
kubectl get pods -n quantis-trading

# Show services
echo "🌐 Services:"
kubectl get services -n quantis-trading

# Show ingress
echo "🔗 Ingress:"
kubectl get ingress -n quantis-trading

# Show HPA
echo "📈 Horizontal Pod Autoscalers:"
kubectl get hpa -n quantis-trading

echo ""
echo "🎉 Quantis Trading Platform deployed successfully to AWS EKS!"
echo ""
echo "📋 Access Information:"
echo "   Dashboard: http://trading.quantis.com"
echo "   API: http://api.quantis.com"
echo "   Monitoring: http://monitoring.quantis.com/grafana"
echo ""
echo "🔧 Useful Commands:"
echo "   kubectl get pods -n quantis-trading"
echo "   kubectl get services -n quantis-trading"
echo "   kubectl logs -f deployment/trading-engine -n quantis-trading"
echo "   kubectl scale deployment trading-engine --replicas=5 -n quantis-trading"
echo ""
echo "📊 Monitoring:"
echo "   Grafana: http://monitoring.quantis.com/grafana (admin/admin123)"
echo "   Prometheus: http://monitoring.quantis.com/prometheus"
echo ""
echo "🎯 Your Quantis Trading Platform is now running on AWS EKS!"
