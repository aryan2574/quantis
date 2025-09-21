# Quantis Trading Dashboard

A professional-grade React-based trading dashboard for the Quantis trading platform. This dashboard provides real-time market data visualization, order management, portfolio tracking, and AI-driven recommendations.

## Features

### 🎯 Core Trading Features

- **Real-time Order Book**: Live Level 2 market data with bid/ask spreads
- **Order Entry**: Place mock buy/sell orders with market and limit order types
- **Portfolio Management**: View balances, positions, and P&L in real-time
- **Trade Blotter**: Live feed of executed trades with detailed information
- **Market Data**: Real-time price updates, volume, and market statistics

### 🤖 AI-Powered Insights

- **Smart Recommendations**: AI-driven trading suggestions based on market patterns
- **Anomaly Detection**: Automatic alerts for unusual market activity
- **Sentiment Analysis**: Real-time market sentiment indicators
- **Risk Alerts**: Proactive risk management notifications

### 🎨 Professional UI/UX

- **Responsive Design**: Optimized for desktop, tablet, and mobile
- **Dark/Light Themes**: Professional trading interface themes
- **Real-time Updates**: WebSocket-powered live data feeds
- **Customizable Layout**: Drag-and-drop dashboard customization

## Technology Stack

### Frontend

- **React 18**: Modern React with hooks and concurrent features
- **TypeScript**: Type-safe development
- **Vite**: Fast build tool and development server
- **Tailwind CSS**: Utility-first CSS framework
- **Radix UI**: Accessible component primitives
- **Recharts**: Data visualization library
- **Zustand**: Lightweight state management
- **Apollo Client**: GraphQL client with caching

### Backend Integration

- **GraphQL**: Unified API through Dashboard Gateway
- **WebSockets**: Real-time data streaming
- **gRPC**: High-performance service communication
- **REST APIs**: Standard HTTP endpoints

## Quick Start

### Prerequisites

- Node.js 18+
- npm or yarn
- Running Quantis backend services

### Installation

1. **Install dependencies**:

   ```bash
   cd services/trader-dashboard
   npm install
   ```

2. **Start development server**:

   ```bash
   npm run dev
   ```

3. **Access the dashboard**:
   Open http://localhost:3000 in your browser

### Using Startup Scripts

**Windows**:

```bash
scripts/start-dashboard.bat
```

**Linux/macOS**:

```bash
scripts/start-dashboard.sh
```

## Project Structure

```
services/trader-dashboard/
├── src/
│   ├── components/           # React components
│   │   ├── ui/              # Reusable UI components
│   │   ├── TradingDashboard.tsx
│   │   ├── OrderBook.tsx
│   │   ├── Portfolio.tsx
│   │   ├── MarketData.tsx
│   │   ├── OrderEntry.tsx
│   │   ├── TradeBlotter.tsx
│   │   └── AIRecommendations.tsx
│   ├── stores/              # State management
│   │   ├── authStore.ts
│   │   └── tradingStore.ts
│   ├── lib/                 # Utilities and configurations
│   │   ├── apollo.ts        # GraphQL client
│   │   ├── websocket.ts     # WebSocket service
│   │   ├── utils.ts         # Helper functions
│   │   └── graphql/         # GraphQL queries and mutations
│   ├── hooks/               # Custom React hooks
│   └── App.tsx              # Main application component
├── public/                  # Static assets
├── package.json
├── vite.config.ts
├── tailwind.config.js
├── tsconfig.json
└── Dockerfile
```

## Component Architecture

### TradingDashboard

Main dashboard component that orchestrates all trading modules:

- Real-time data subscriptions
- Layout management
- State coordination

### OrderBook

Displays live Level 2 market data:

- Bid/ask price levels
- Order quantities and counts
- Real-time spread calculations
- Color-coded price movements

### Portfolio

Shows user's trading account:

- Cash balance
- Position holdings
- Realized/unrealized P&L
- Performance metrics

### MarketData

Real-time market information:

- Current price and changes
- High/low/open values
- Trading volume
- Market statistics

### OrderEntry

Order placement interface:

- Buy/sell side selection
- Market and limit orders
- Quantity and price inputs
- Order validation

### TradeBlotter

Live trade execution feed:

- Recent trade history
- Price, quantity, and side
- Timestamp information
- Trade statistics

### AIRecommendations

AI-powered insights:

- Trading recommendations
- Market anomaly alerts
- Risk notifications
- Sentiment analysis

## State Management

The dashboard uses Zustand for state management with two main stores:

### AuthStore

- User authentication
- Session management
- Token handling

### TradingStore

- Order book data
- Market data
- Trade history
- Portfolio information
- AI recommendations

## Real-time Data

### WebSocket Integration

- **Order Book Updates**: Live bid/ask changes
- **Trade Streams**: Real-time trade executions
- **Market Data**: Price and volume updates
- **Order Status**: Order state changes

### GraphQL Queries

- **Portfolio Data**: Account balances and positions
- **Order History**: Past and pending orders
- **Market Information**: Historical and current data
- **Trade Records**: Execution history

## Styling and Theming

### Tailwind CSS Configuration

- Custom color palette for trading
- Professional typography
- Responsive grid layouts
- Animation utilities

### Component Design

- Consistent spacing and sizing
- Accessible color contrasts
- Professional trading aesthetics
- Mobile-responsive layouts

## Development

### Available Scripts

- `npm run dev`: Start development server
- `npm run build`: Build for production
- `npm run preview`: Preview production build
- `npm run lint`: Run ESLint

### Environment Variables

- `REACT_APP_API_URL`: GraphQL Gateway URL
- `REACT_APP_WS_URL`: WebSocket server URL

### Hot Reloading

The development server supports hot module replacement for fast development cycles.

## Production Deployment

### Docker Build

```bash
docker build -t quantis-dashboard .
docker run -p 3000:80 quantis-dashboard
```

### Nginx Configuration

The production build includes optimized Nginx configuration for:

- Static asset caching
- API proxy routing
- WebSocket proxying
- Security headers

## Integration with Backend Services

### GraphQL Gateway

- Unified API endpoint
- Data aggregation
- Authentication handling
- Request optimization

### Market Data Service

- Real-time data streaming
- WebSocket connections
- Market data normalization
- Subscription management

### Order Ingress Service

- Order placement
- Order status tracking
- Risk validation
- Execution reporting

## Performance Optimizations

### Data Loading

- Apollo Client caching
- Optimistic updates
- Pagination support
- Background refetching

### Real-time Updates

- WebSocket connection pooling
- Efficient data serialization
- Selective subscriptions
- Connection management

### UI Performance

- React.memo optimization
- Virtual scrolling for large lists
- Debounced user inputs
- Lazy component loading

## Security

### Authentication

- JWT token management
- Secure token storage
- Session validation
- Logout handling

### Data Protection

- HTTPS enforcement
- Secure WebSocket connections
- Input validation
- XSS protection

## Monitoring and Analytics

### Error Tracking

- Client-side error logging
- Performance monitoring
- User interaction tracking
- Real-time diagnostics

### Performance Metrics

- Page load times
- API response times
- WebSocket latency
- User engagement metrics

## Contributing

1. Follow the existing code style
2. Add TypeScript types for all props
3. Include proper error handling
4. Write meaningful commit messages
5. Test on multiple screen sizes

## License

This project is part of the Quantis trading platform and follows the same licensing terms.
