import { gql } from "@apollo/client";

// ==================== PORTFOLIO QUERIES ====================

export const GET_PORTFOLIO = gql`
  query GetPortfolio($userId: String!) {
    portfolio(userId: $userId) {
      userId
      totalValue
      cashBalance
      positionsValue
      unrealizedPnl
      realizedPnl
      currency
      lastUpdated
      positions {
        userId
        symbol
        quantity
        averagePrice
        currentPrice
        marketValue
        unrealizedPnl
        realizedPnl
        lastUpdated
        change
        changePercent
      }
      performance {
        userId
        period
        totalReturn
        totalReturnPercent
        sharpeRatio
        maxDrawdown
        volatility
        winRate
        profitFactor
        totalTrades
        winningTrades
        losingTrades
        averageWin
        averageLoss
        largestWin
        largestLoss
        startValue
        endValue
        lastUpdated
      }
    }
  }
`;

export const GET_POSITIONS = gql`
  query GetPositions($userId: String!) {
    positions(userId: $userId) {
      userId
      symbol
      quantity
      averagePrice
      currentPrice
      marketValue
      unrealizedPnl
      realizedPnl
      lastUpdated
      change
      changePercent
    }
  }
`;

export const GET_POSITION = gql`
  query GetPosition($userId: String!, $symbol: String!) {
    position(userId: $userId, symbol: $symbol) {
      userId
      symbol
      quantity
      averagePrice
      currentPrice
      marketValue
      unrealizedPnl
      realizedPnl
      lastUpdated
      change
      changePercent
    }
  }
`;

export const GET_CASH_BALANCE = gql`
  query GetCashBalance($userId: String!) {
    cashBalance(userId: $userId) {
      userId
      balance
      currency
      availableBalance
      pendingBalance
      lastUpdated
    }
  }
`;

// ==================== TRADING QUERIES ====================

export const GET_TRADING_HISTORY = gql`
  query GetTradingHistory(
    $userId: String!
    $limit: Int
    $startTime: Long
    $endTime: Long
  ) {
    tradingHistory(
      userId: $userId
      limit: $limit
      startTime: $startTime
      endTime: $endTime
    ) {
      tradeId
      orderId
      userId
      symbol
      side
      quantity
      price
      totalValue
      executedAt
      status
      commission
      counterpartyOrderId
    }
  }
`;

export const GET_ORDER_HISTORY = gql`
  query GetOrderHistory($userId: String!, $limit: Int, $status: String) {
    orderHistory(userId: $userId, limit: $limit, status: $status) {
      orderId
      userId
      symbol
      side
      quantity
      price
      orderType
      timeInForce
      status
      filledQuantity
      averagePrice
      createdAt
      updatedAt
      executedAt
      commission
      metadata
    }
  }
`;

export const GET_ACTIVE_ORDERS = gql`
  query GetActiveOrders($userId: String!) {
    activeOrders(userId: $userId) {
      orderId
      userId
      symbol
      side
      quantity
      price
      orderType
      timeInForce
      status
      filledQuantity
      averagePrice
      createdAt
      updatedAt
      executedAt
      commission
      metadata
    }
  }
`;

// ==================== MARKET DATA QUERIES ====================

export const GET_MARKET_DATA = gql`
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
      status
      high24h
      low24h
      open24h
    }
  }
`;

export const GET_ORDER_BOOK = gql`
  query GetOrderBook($symbol: String!, $depth: Int) {
    orderBook(symbol: $symbol, depth: $depth) {
      symbol
      bids {
        price
        quantity
        orderCount
        totalValue
      }
      asks {
        price
        quantity
        orderCount
        totalValue
      }
      timestamp
      status
      spread
      midPrice
    }
  }
`;

export const GET_RECENT_TRADES = gql`
  query GetRecentTrades($symbol: String!, $limit: Int) {
    recentTrades(symbol: $symbol, limit: $limit) {
      tradeId
      symbol
      side
      quantity
      price
      timestamp
      totalValue
    }
  }
`;

export const GET_HISTORICAL_DATA = gql`
  query GetHistoricalData(
    $symbol: String!
    $interval: String!
    $startTime: Long
    $endTime: Long
    $limit: Int
  ) {
    historicalData(
      symbol: $symbol
      interval: $interval
      startTime: $startTime
      endTime: $endTime
      limit: $limit
    ) {
      timestamp
      open
      high
      low
      close
      volume
      vwap
    }
  }
`;

export const GET_MARKET_SUMMARY = gql`
  query GetMarketSummary($symbols: [String!]!) {
    marketSummary(symbols: $symbols) {
      symbol
      lastPrice
      change
      changePercent
      volume
      high24h
      low24h
      marketCap
    }
  }
`;

// ==================== ANALYTICS QUERIES ====================

export const GET_PORTFOLIO_PERFORMANCE = gql`
  query GetPortfolioPerformance($userId: String!, $period: String!) {
    portfolioPerformance(userId: $userId, period: $period) {
      userId
      period
      totalReturn
      totalReturnPercent
      sharpeRatio
      maxDrawdown
      volatility
      winRate
      profitFactor
      totalTrades
      winningTrades
      losingTrades
      averageWin
      averageLoss
      largestWin
      largestLoss
      startValue
      endValue
      lastUpdated
    }
  }
`;

export const GET_RISK_METRICS = gql`
  query GetRiskMetrics($userId: String!) {
    riskMetrics(userId: $userId) {
      userId
      portfolioValue
      cashBalance
      marginUsed
      marginAvailable
      buyingPower
      dayTradingBuyingPower
      riskScore
      concentrationRisk
      sectorExposure {
        sector
        percentage
        value
        riskLevel
      }
      positionLimits {
        symbol
        maxPosition
        currentPosition
        limitPercentage
        riskLevel
      }
      lastUpdated
    }
  }
`;

export const GET_TRADE_ANALYTICS = gql`
  query GetTradeAnalytics($userId: String!, $symbol: String, $period: String!) {
    tradeAnalytics(userId: $userId, symbol: $symbol, period: $period) {
      userId
      symbol
      period
      totalTrades
      totalVolume
      totalValue
      averageTradeSize
      largestTrade
      smallestTrade
      buyTrades
      sellTrades
      buyVolume
      sellVolume
      netPosition
      vwap
      lastTradeAt
      tradingDays
      tradesPerDay
    }
  }
`;

// ==================== DASHBOARD QUERIES ====================

export const GET_DASHBOARD_OVERVIEW = gql`
  query GetDashboardOverview($userId: String!) {
    dashboardOverview(userId: $userId) {
      userId
      portfolio {
        userId
        totalValue
        cashBalance
        positionsValue
        unrealizedPnl
        realizedPnl
        currency
        lastUpdated
        positions {
          userId
          symbol
          quantity
          averagePrice
          currentPrice
          marketValue
          unrealizedPnl
          realizedPnl
          lastUpdated
          change
          changePercent
        }
      }
      watchlist {
        symbol
        bestBid
        bestAsk
        lastPrice
        spread
        volume
        change
        changePercent
        timestamp
        status
        high24h
        low24h
        open24h
      }
      recentTrades {
        tradeId
        orderId
        userId
        symbol
        side
        quantity
        price
        totalValue
        executedAt
        status
        commission
        counterpartyOrderId
      }
      activeOrders {
        orderId
        userId
        symbol
        side
        quantity
        price
        orderType
        timeInForce
        status
        filledQuantity
        averagePrice
        createdAt
        updatedAt
        executedAt
        commission
        metadata
      }
      marketSummary {
        symbol
        lastPrice
        change
        changePercent
        volume
        high24h
        low24h
        marketCap
      }
      performance {
        userId
        period
        totalReturn
        totalReturnPercent
        sharpeRatio
        maxDrawdown
        volatility
        winRate
        profitFactor
        totalTrades
        winningTrades
        losingTrades
        averageWin
        averageLoss
        largestWin
        largestLoss
        startValue
        endValue
        lastUpdated
      }
      riskMetrics {
        userId
        portfolioValue
        cashBalance
        marginUsed
        marginAvailable
        buyingPower
        dayTradingBuyingPower
        riskScore
        concentrationRisk
        sectorExposure {
          sector
          percentage
          value
          riskLevel
        }
        positionLimits {
          symbol
          maxPosition
          currentPosition
          limitPercentage
          riskLevel
        }
        lastUpdated
      }
      alerts {
        id
        userId
        type
        severity
        title
        message
        symbol
        price
        timestamp
        acknowledged
        actionRequired
      }
      lastUpdated
    }
  }
`;
