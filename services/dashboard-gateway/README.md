# 🚀 **Dashboard GraphQL Gateway Service**

## **Overview**

The **Dashboard GraphQL Gateway** is a high-performance aggregation service that provides a unified GraphQL API for the trading dashboard. It aggregates data from all microservices while maintaining **ultra-low latency** for core trading operations.

## **🏗️ Architecture**

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend Dashboard                       │
│              (React/Vue/Angular + GraphQL)                 │
└─────────────────────┬───────────────────────────────────────┘
                      │ GraphQL (HTTP/2)
┌─────────────────────▼───────────────────────────────────────┐
│                GraphQL Gateway Service                      │
│              (Dashboard Aggregation Layer)                 │
├─────────────────────────────────────────────────────────────┤
│ • Portfolio Service (gRPC) ← Keep for speed               │
│ • Market Data Service (GraphQL) ← Already exists          │
│ • Order Ingress Service (gRPC) ← Keep for speed           │
│ • Analytics Services (REST) ← Keep for speed              │
└─────────────────────────────────────────────────────────────┘
```

## **🎯 Key Features**

### **✅ Performance-First Design**

- **gRPC for internal communication** (10-100x faster than REST)
- **Redis caching** for improved response times
- **HTTP/2 multiplexing** for efficient connections
- **Connection pooling** and keep-alive optimization

### **✅ Unified GraphQL API**

- **Single endpoint** for all dashboard data
- **Real-time subscriptions** for live updates
- **Flexible queries** with field selection
- **Type-safe** with comprehensive schema

### **✅ Real-Time Updates**

- **Portfolio updates** every 5 seconds
- **Market data updates** every 500ms
- **Order updates** every 2 seconds
- **Position updates** every 3 seconds

### **✅ Security & Authentication**

- **JWT-based authentication** for user-specific data
- **CORS configuration** for frontend access
- **Rate limiting** to prevent abuse
- **Input validation** and sanitization

## **📊 GraphQL Schema**

### **Queries**

```graphql
type Query {
  # Portfolio Management
  portfolio(userId: String!): Portfolio
  positions(userId: String!): [Position!]!
  position(userId: String!, symbol: String!): Position
  cashBalance(userId: String!): CashBalance

  # Trading History
  tradingHistory(
    userId: String!
    limit: Int
    startTime: Long
    endTime: Long
  ): [TradeRecord!]!
  orderHistory(userId: String!, limit: Int, status: String): [Order!]!
  activeOrders(userId: String!): [Order!]!

  # Market Data
  marketData(symbol: String!): MarketData
  historicalData(
    symbol: String!
    interval: String!
    startTime: Long
    endTime: Long
    limit: Int
  ): [HistoricalData!]!
  orderBook(symbol: String!, depth: Int): OrderBook
  recentTrades(symbol: String!, limit: Int): [Trade!]!
  marketSummary(symbols: [String!]!): [MarketSummary!]!

  # Analytics & Risk
  portfolioPerformance(userId: String!, period: String!): PerformanceMetrics
  riskMetrics(userId: String!): RiskMetrics
  tradeAnalytics(
    userId: String!
    symbol: String
    period: String!
  ): TradeAnalytics

  # Dashboard Overview
  dashboardOverview(userId: String!): DashboardOverview
}
```

### **Mutations**

```graphql
type Mutation {
  # Order Management
  placeOrder(input: PlaceOrderInput!): OrderResponse!
  cancelOrder(orderId: String!): CancelResponse!
  modifyOrder(input: ModifyOrderInput!): OrderResponse!

  # Portfolio Operations
  updateWatchlist(userId: String!, symbols: [String!]!): WatchlistResponse!
}
```

### **Subscriptions**

```graphql
type Subscription {
  # Portfolio Updates
  portfolioUpdates(userId: String!): PortfolioUpdate!
  positionUpdates(userId: String!): PositionUpdate!

  # Market Data Updates
  marketDataUpdates(symbols: [String!]!): MarketDataUpdate!
  tradeUpdates(symbols: [String!]!): TradeUpdate!

  # Order Updates
  orderUpdates(userId: String!): OrderUpdate!

  # Dashboard Updates
  dashboardUpdates(userId: String!): DashboardUpdate!
}
```

## **🚀 Quick Start**

### **1. Prerequisites**

- Java 21
- Maven 3.9+
- Redis (for caching)
- All microservices running

### **2. Start the Service**

```bash
# Start infrastructure
./scripts/start-services-local.sh

# Start dashboard gateway
./scripts/start-dashboard-gateway.sh start
```

### **3. Access the Service**

- **GraphQL Endpoint**: `http://localhost:8085/graphql`
- **GraphiQL Interface**: `http://localhost:8085/graphiql`
- **Health Check**: `http://localhost:8085/actuator/health`

## **📝 Example Queries**

### **Get Portfolio Overview**

```graphql
query GetPortfolio($userId: String!) {
  portfolio(userId: $userId) {
    userId
    totalValue
    cashBalance
    positionsValue
    unrealizedPnl
    currency
    lastUpdated
    positions {
      symbol
      quantity
      averagePrice
      currentPrice
      marketValue
      unrealizedPnl
    }
  }
}
```

### **Get Market Data**

```graphql
query GetMarketData($symbol: String!) {
  marketData(symbol: $symbol) {
    symbol
    bestBid
    bestAsk
    lastPrice
    spread
    volume
    change
    changePercent
    timestamp
  }
}
```

### **Place Order**

```graphql
mutation PlaceOrder($input: PlaceOrderInput!) {
  placeOrder(input: $input) {
    success
    orderId
    message
    order {
      orderId
      userId
      symbol
      side
      quantity
      price
      status
    }
    errors
  }
}
```

### **Real-Time Portfolio Updates**

```graphql
subscription PortfolioUpdates($userId: String!) {
  portfolioUpdates(userId: $userId) {
    userId
    totalValue
    cashBalance
    positionsValue
    unrealizedPnl
    timestamp
  }
}
```

## **⚡ Performance Optimization**

### **Caching Strategy**

- **Portfolio data**: 30 seconds TTL
- **Market data**: 1 second TTL
- **Order data**: 5 seconds TTL
- **Analytics**: 1 minute TTL

### **Connection Optimization**

- **gRPC keep-alive**: 30 seconds
- **Connection pooling**: 20 max connections
- **Request timeout**: 30 seconds
- **Subscription timeout**: 5 minutes

### **Rate Limiting**

- **1000 requests per minute**
- **Burst capacity**: 100 requests
- **Per-user limits**: Configurable

## **🔧 Configuration**

### **Environment Variables**

```bash
# Server Configuration
SERVER_PORT=8085

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# gRPC Services
PORTFOLIO_SERVICE_HOST=localhost
PORTFOLIO_SERVICE_PORT=9090
ORDER_INGRESS_SERVICE_HOST=localhost
ORDER_INGRESS_SERVICE_PORT=9090

# Market Data Service
MARKET_DATA_SERVICE_URL=http://localhost:8084

# JWT Configuration
JWT_ISSUER_URI=https://your-auth-server
JWT_JWK_SET_URI=https://your-auth-server/.well-known/jwks.json
```

### **Application Properties**

```yaml
dashboard:
  gateway:
    # Performance tuning
    max-concurrent-requests: 1000
    request-timeout: 30000ms
    subscription-timeout: 300000ms

    # Caching configuration
    cache:
      portfolio-ttl: 30000ms
      market-data-ttl: 1000ms
      order-data-ttl: 5000ms
      analytics-ttl: 60000ms

    # Real-time updates
    real-time:
      market-data-interval: 500ms
      portfolio-interval: 5000ms
      order-interval: 2000ms
      position-interval: 3000ms
```

## **📊 Monitoring**

### **Health Checks**

- **Service health**: `/actuator/health`
- **GraphQL endpoint**: `/actuator/graphql`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

### **Key Metrics**

- **Request latency**: P50, P95, P99
- **Throughput**: Requests per second
- **Error rate**: 4xx, 5xx responses
- **Cache hit ratio**: Redis performance
- **gRPC connection health**: Service connectivity

## **🛠️ Development**

### **Project Structure**

```
services/dashboard-gateway/
├── src/main/java/com/quantis/dashboard_gateway/
│   ├── client/           # gRPC clients
│   ├── config/           # Configuration classes
│   ├── model/            # Data models
│   ├── resolver/         # GraphQL resolvers
│   └── DashboardGatewayApplication.java
├── src/main/resources/
│   ├── graphql/
│   │   └── schema.graphqls
│   ├── application.yml
│   └── application-docker.yml
├── Dockerfile
└── pom.xml
```

### **Adding New Resolvers**

1. Create resolver class in `resolver/` package
2. Add methods with `@QueryMapping`, `@MutationMapping`, or `@SubscriptionMapping`
3. Update GraphQL schema in `schema.graphqls`
4. Add corresponding data models in `model/` package

### **Adding New Clients**

1. Create client class in `client/` package
2. Add gRPC client configuration
3. Implement data conversion methods
4. Add error handling and fallbacks

## **🔒 Security**

### **Authentication**

- **JWT tokens** for user identification
- **OAuth2 Resource Server** configuration
- **Token validation** on each request
- **User context** extraction for data filtering

### **Authorization**

- **User-specific data** filtering
- **Role-based access** control
- **Rate limiting** per user
- **Input validation** and sanitization

## **🚀 Deployment**

### **Docker**

```bash
# Build image
docker build -t dashboard-gateway .

# Run container
docker run -d \
  --name dashboard-gateway \
  -p 8085:8085 \
  -e REDIS_HOST=redis \
  -e PORTFOLIO_SERVICE_HOST=portfolio-service \
  -e ORDER_INGRESS_SERVICE_HOST=order-ingress \
  dashboard-gateway
```

### **Kubernetes**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: dashboard-gateway
spec:
  replicas: 3
  selector:
    matchLabels:
      app: dashboard-gateway
  template:
    metadata:
      labels:
        app: dashboard-gateway
    spec:
      containers:
        - name: dashboard-gateway
          image: dashboard-gateway:latest
          ports:
            - containerPort: 8085
          env:
            - name: REDIS_HOST
              value: "redis-service"
            - name: PORTFOLIO_SERVICE_HOST
              value: "portfolio-service"
```

## **📈 Performance Benchmarks**

### **Target Performance**

- **GraphQL queries**: < 100ms P95
- **Real-time subscriptions**: < 50ms latency
- **Throughput**: 1000+ requests/second
- **Concurrent users**: 1000+ simultaneous

### **Optimization Techniques**

- **DataLoader** for N+1 query prevention
- **Connection pooling** for gRPC clients
- **Redis caching** for frequently accessed data
- **Async processing** for non-blocking operations
- **Compression** for large responses

## **🤝 Contributing**

1. **Follow coding standards** and best practices
2. **Add comprehensive tests** for new features
3. **Update documentation** for API changes
4. **Performance test** new features
5. **Security review** for authentication changes

## **📞 Support**

- **Documentation**: This README
- **Health checks**: `/actuator/health`
- **Logs**: `logs/dashboard-gateway.log`
- **Metrics**: `/actuator/metrics`

---

**🎯 The Dashboard GraphQL Gateway provides a high-performance, unified API for your trading dashboard while maintaining the speed and reliability of your core trading operations.**
