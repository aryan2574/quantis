#!/bin/bash

# ==================== KUBERNETES DEPLOYMENT SCRIPT ====================
# Complete deployment of Quantis Trading Platform to Kubernetes

set -e

echo "🚀 Deploying Quantis Trading Platform to Kubernetes..."

# ==================== PREREQUISITES ====================
echo "📋 Checking prerequisites..."

# Check if kubectl is configured
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ kubectl is not configured. Please configure kubectl first."
    exit 1
fi

# Check if cluster is accessible
if ! kubectl get nodes &> /dev/null; then
    echo "❌ Kubernetes cluster not accessible. Please check your cluster connection."
    exit 1
fi

echo "✅ Prerequisites check passed!"

# ==================== CREATE NAMESPACE ====================
echo "📁 Creating namespace..."

kubectl apply -f k8s/namespace.yaml

echo "✅ Namespace created!"

# ==================== DEPLOY CONFIGURATION ====================
echo "⚙️ Deploying configuration..."

kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

echo "✅ Configuration deployed!"

# ==================== DEPLOY SECURITY POLICIES ====================
echo "🔒 Deploying security policies..."

kubectl apply -f k8s/security-policies.yaml

echo "✅ Security policies deployed!"

# ==================== DEPLOY INFRASTRUCTURE ====================
echo "🏗️ Deploying infrastructure services..."

# Deploy databases first
kubectl apply -f k8s/database-deployments.yaml

# Wait for databases to be ready
echo "⏳ Waiting for databases to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n quantis-trading --timeout=300s
kubectl wait --for=condition=ready pod -l app=redis -n quantis-trading --timeout=300s
kubectl wait --for=condition=ready pod -l app=cassandra -n quantis-trading --timeout=300s

# Deploy messaging infrastructure
kubectl apply -f k8s/kafka-deployment.yaml

# Wait for Kafka to be ready
echo "⏳ Waiting for Kafka to be ready..."
kubectl wait --for=condition=ready pod -l app=kafka -n quantis-trading --timeout=300s

echo "✅ Infrastructure deployed!"

# ==================== DEPLOY CORE SERVICES ====================
echo "🔧 Deploying core services..."

# Deploy services in dependency order
kubectl apply -f k8s/auth-service-deployment.yaml
kubectl apply -f k8s/market-data-deployment.yaml
kubectl apply -f k8s/order-ingress-deployment.yaml
kubectl apply -f k8s/portfolio-service-deployment.yaml
kubectl apply -f k8s/risk-service-deployment.yaml
kubectl apply -f k8s/trading-engine-deployment.yaml
kubectl apply -f k8s/update-service-deployment.yaml

echo "✅ Core services deployed!"

# ==================== DEPLOY WRITER SERVICES ====================
echo "📝 Deploying writer services..."

kubectl apply -f k8s/writer-services-deployment.yaml

echo "✅ Writer services deployed!"

# ==================== DEPLOY DASHBOARD ====================
echo "🖥️ Deploying dashboard..."

kubectl apply -f k8s/dashboard-deployment.yaml

echo "✅ Dashboard deployed!"

# ==================== DEPLOY MONITORING ====================
echo "📊 Deploying monitoring..."

kubectl apply -f k8s/monitoring.yaml

echo "✅ Monitoring deployed!"

# ==================== DEPLOY AUTO-SCALING ====================
echo "📈 Deploying auto-scaling..."

kubectl apply -f k8s/hpa.yaml

echo "✅ Auto-scaling deployed!"

# ==================== DEPLOY INGRESS ====================
echo "🌐 Deploying ingress..."

kubectl apply -f k8s/ingress.yaml

echo "✅ Ingress deployed!"

# ==================== WAIT FOR DEPLOYMENT ====================
echo "⏳ Waiting for all deployments to be ready..."

# Wait for all pods to be ready
kubectl wait --for=condition=ready pod --all -n quantis-trading --timeout=600s
kubectl wait --for=condition=ready pod --all -n monitoring --timeout=300s

echo "✅ All deployments are ready!"

# ==================== VERIFY DEPLOYMENT ====================
echo "🔍 Verifying deployment..."

# Show pod status
echo "📊 Pod status in quantis-trading namespace:"
kubectl get pods -n quantis-trading

echo ""
echo "📊 Pod status in monitoring namespace:"
kubectl get pods -n monitoring

# Show services
echo ""
echo "🌐 Services:"
kubectl get services -n quantis-trading

# Show ingress
echo ""
echo "🔗 Ingress:"
kubectl get ingress -n quantis-trading
kubectl get ingress -n monitoring

# Show HPA
echo ""
echo "📈 Horizontal Pod Autoscalers:"
kubectl get hpa -n quantis-trading

echo ""
echo "🎉 Quantis Trading Platform deployed successfully to Kubernetes!"
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
echo "🎯 Your Quantis Trading Platform is now running on Kubernetes!"
