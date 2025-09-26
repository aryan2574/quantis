import { gql } from "@apollo/client";

// ==================== PORTFOLIO SUBSCRIPTIONS ====================

export const PORTFOLIO_UPDATES_SUBSCRIPTION = gql`
  subscription PortfolioUpdates($userId: String!) {
    portfolioUpdates(userId: $userId) {
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
      timestamp
      changes {
        type
        symbol
        oldValue
        newValue
        change
        changePercent
      }
    }
  }
`;

export const POSITION_UPDATES_SUBSCRIPTION = gql`
  subscription PositionUpdates($userId: String!) {
    positionUpdates(userId: $userId) {
      userId
      position {
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
      timestamp
      change
      changePercent
    }
  }
`;

// ==================== MARKET DATA SUBSCRIPTIONS ====================

export const MARKET_DATA_UPDATES_SUBSCRIPTION = gql`
  subscription MarketDataUpdates($symbols: [String!]!) {
    marketDataUpdates(symbols: $symbols) {
      symbols {
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
      timestamp
    }
  }
`;

export const TRADE_UPDATES_SUBSCRIPTION = gql`
  subscription TradeUpdates($symbols: [String!]!) {
    tradeUpdates(symbols: $symbols) {
      trades {
        tradeId
        symbol
        side
        quantity
        price
        timestamp
        totalValue
      }
      timestamp
    }
  }
`;

// ==================== ORDER SUBSCRIPTIONS ====================

export const ORDER_UPDATES_SUBSCRIPTION = gql`
  subscription OrderUpdates($userId: String!) {
    orderUpdates(userId: $userId) {
      userId
      order {
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
      timestamp
      changeType
    }
  }
`;

// ==================== DASHBOARD SUBSCRIPTIONS ====================

export const DASHBOARD_UPDATES_SUBSCRIPTION = gql`
  subscription DashboardUpdates($userId: String!) {
    dashboardUpdates(userId: $userId) {
      userId
      overview {
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
      timestamp
      updateType
    }
  }
`;
