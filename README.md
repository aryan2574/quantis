# ⚡ Quantis - Real time trading platform

> **The future of trading is here** 🚀

A lightning-fast, enterprise-grade trading platform that handles **crypto, forex, stocks, and futures** with microsecond precision. Built for traders who demand speed, reliability, and cutting-edge technology.

## 🎯 **What Makes Quantis Special?**

- **⚡ Ultra-Fast**: Process 100,000+ orders per second with our hybrid Java/C++ engine
- **🌍 Multi-Asset**: Trade Bitcoin, EUR/USD, Apple stock, and futures all in one platform
- **🔒 Bank-Grade Security**: JWT auth, RBAC, encryption, and fraud detection
- **📊 Real-Time Everything**: Live market data, instant portfolio updates, WebSocket streaming
- **🏗️ Enterprise Ready**: Kubernetes, monitoring, auto-scaling, and cloud deployment

## 🛠️ **Tech Stack**

### **Frontend**

- **React 18** + **TypeScript** - Modern, type-safe UI
- **Tailwind CSS** + **Radix UI** - Beautiful, accessible components
- **Apollo GraphQL** - Smart data fetching
- **Vite** - Lightning-fast builds

### **Backend**

- **Java 21** + **Spring Boot 3** - Enterprise-grade services
- **C++17** - High-performance trading engine
- **NestJS** - Modern Node.js framework for auth
- **gRPC** - Ultra-fast service communication
- **GraphQL** - Flexible API queries

### **Infrastructure**

- **Docker** + **Kubernetes** - Container orchestration
- **PostgreSQL** + **Citus** - Distributed database
- **Redis** - Lightning-fast caching
- **Kafka** - Event streaming
- **Prometheus** + **Grafana** - Monitoring & metrics

## 🚀 **Get Started in 3 Steps**

### **1. Clone & Setup**

```bash
git clone https://github.com/aryan2574/quantis
cd quantis
```

### **2. Configure API Keys**

```bash
# Copy the template
cp infra/docker.env.template .env

# Edit .env with your API keys:
# ALPHA_VANTAGE_API_KEY=your_key_here
# EXCHANGE_RATE_API_KEY=your_key_here
# POLYGON_API_KEY=your_key_here
```

### **3. Launch Everything**

```bash
# 🐳 Docker (Recommended - One Command Magic!)
./scripts/start-docker.sh

# Or manually:
docker-compose -f infra/docker-compose.yml up --build -d
```

**That's it!** 🎉 Your trading platform is now running at:

- **📱 Dashboard**: http://localhost:3000
- **🔍 GraphQL Playground**: http://localhost:8086/graphql
- **📊 Market Data**: http://localhost:8082

## 🏗️ **Architecture**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   📱 React      │    │   🔗 GraphQL     │    │   ⚡ Trading     │
│   Dashboard     │◄──►│   Gateway       │◄──►│   Engine (C++)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   📊 Market     │    │   📝 Order     │    │   💼 Portfolio  │
│   Data Service  │    │   Ingress      │    │   Service       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   🛡️ Risk      │    │   🔐 Auth       │    │   📈 Real-time   │
│   Service       │    │   Service       │    │   Updates       │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🎮 **Development Options**

### **🐳 Docker (Easiest)**

```bash
# One command to rule them all
./scripts/start-docker.sh
```

### **💻 Local Development**

```bash
# Start individual services
cd services/trader-dashboard && npm run dev
cd services/market-data-service && mvn spring-boot:run
cd services/dashboard-gateway && mvn spring-boot:run
```

### **☁️ Production (AWS)**

```bash
# Deploy to AWS EKS
./scripts/deploy-to-aws.sh
```

## 📊 **Performance Metrics**

| Metric               | Value               |
| -------------------- | ------------------- |
| **Order Processing** | 100,000+ orders/sec |
| **Response Time**    | < 100ms             |
| **Concurrent Users** | 1,000+              |
| **Data Latency**     | < 1 second          |

## 🔧 **Configuration**

### **Required API Keys**

```bash
# Get your free API keys from:
# - Alpha Vantage: https://www.alphavantage.co/support/#api-key
# - ExchangeRate: https://www.exchangerate-api.com/
# - Polygon: https://polygon.io/
```

### **Environment Setup**

```bash
# Copy template
cp infra/docker.env.template .env

# Add your keys
echo "ALPHA_VANTAGE_API_KEY=your_key" >> .env
echo "EXCHANGE_RATE_API_KEY=your_key" >> .env
echo "POLYGON_API_KEY=your_key" >> .env
```

## 🚀 **Deployment**

### **Docker Compose**

```bash
docker-compose -f infra/docker-compose.yml up --build -d
```

### **Kubernetes**

```bash
kubectl apply -f k8s/
```

### **AWS EKS**

```bash
eksctl create cluster -f aws/eks-cluster.yaml
```

## 📈 **Monitoring**

- **📊 Metrics**: Prometheus + Grafana
- **📝 Logs**: ELK Stack
- **🔍 Tracing**: Jaeger
- **❤️ Health**: Spring Boot Actuator

## 🧪 **Testing**

```bash
# Backend tests
mvn test

# Frontend tests
cd services/trader-dashboard && npm test

# Integration tests
./scripts/test-local-system.sh
```

## 🤝 **Contributing**

1. **Fork** the repo
2. **Create** a feature branch
3. **Code** your changes
4. **Test** everything
5. **Submit** a PR

---

<div align="center">

**⚡ Built with passion for the future of trading ⚡**

[![GitHub stars](https://img.shields.io/github/stars/aryan2574/quantis?style=social)](https://github.com/aryan2574/quantis)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-blue?logo=kubernetes)](https://kubernetes.io/)

</div>
