import { useQuery, useMutation, useSubscription } from "@apollo/client";
import { useAuthStore } from "@/stores/authStore";
import {
  GET_PORTFOLIO,
  GET_POSITIONS,
  GET_POSITION,
  GET_CASH_BALANCE,
  GET_TRADING_HISTORY,
  GET_ORDER_HISTORY,
  GET_ACTIVE_ORDERS,
  GET_MARKET_DATA,
  GET_ORDER_BOOK,
  GET_RECENT_TRADES,
  GET_HISTORICAL_DATA,
  GET_MARKET_SUMMARY,
  GET_PORTFOLIO_PERFORMANCE,
  GET_RISK_METRICS,
  GET_TRADE_ANALYTICS,
  GET_DASHBOARD_OVERVIEW,
} from "./queries";
import {
  PLACE_ORDER,
  CANCEL_ORDER,
  MODIFY_ORDER,
  UPDATE_WATCHLIST,
} from "./mutations";
import {
  PORTFOLIO_UPDATES_SUBSCRIPTION,
  POSITION_UPDATES_SUBSCRIPTION,
  MARKET_DATA_UPDATES_SUBSCRIPTION,
  TRADE_UPDATES_SUBSCRIPTION,
  ORDER_UPDATES_SUBSCRIPTION,
  DASHBOARD_UPDATES_SUBSCRIPTION,
} from "./subscriptions";

// ==================== PORTFOLIO HOOKS ====================

export const usePortfolio = () => {
  const { user } = useAuthStore();
  return useQuery(GET_PORTFOLIO, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
    pollInterval: 5000, // Poll every 5 seconds
  });
};

export const usePositions = () => {
  const { user } = useAuthStore();
  return useQuery(GET_POSITIONS, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
    pollInterval: 5000,
  });
};

export const usePosition = (symbol: string) => {
  const { user } = useAuthStore();
  return useQuery(GET_POSITION, {
    variables: { userId: user?.id || "demo-user", symbol },
    skip: !user?.id || !symbol,
    pollInterval: 5000,
  });
};

export const useCashBalance = () => {
  const { user } = useAuthStore();
  return useQuery(GET_CASH_BALANCE, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
    pollInterval: 5000,
  });
};

// ==================== TRADING HOOKS ====================

export const useTradingHistory = (
  limit = 50,
  startTime?: number,
  endTime?: number
) => {
  const { user } = useAuthStore();
  return useQuery(GET_TRADING_HISTORY, {
    variables: {
      userId: user?.id || "demo-user",
      limit,
      startTime,
      endTime,
    },
    skip: !user?.id,
    pollInterval: 10000,
  });
};

export const useOrderHistory = (limit = 50, status?: string) => {
  const { user } = useAuthStore();
  return useQuery(GET_ORDER_HISTORY, {
    variables: {
      userId: user?.id || "demo-user",
      limit,
      status,
    },
    skip: !user?.id,
    pollInterval: 10000,
  });
};

export const useActiveOrders = () => {
  const { user } = useAuthStore();
  return useQuery(GET_ACTIVE_ORDERS, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
    pollInterval: 2000, // More frequent polling for active orders
  });
};

// ==================== MARKET DATA HOOKS ====================

export const useMarketData = (symbol: string) => {
  return useQuery(GET_MARKET_DATA, {
    variables: { symbol },
    skip: !symbol,
    pollInterval: 1000, // Real-time market data
  });
};

export const useOrderBook = (symbol: string, depth = 10) => {
  return useQuery(GET_ORDER_BOOK, {
    variables: { symbol, depth },
    skip: !symbol,
    pollInterval: 500, // High-frequency order book updates
  });
};

export const useRecentTrades = (symbol: string, limit = 20) => {
  return useQuery(GET_RECENT_TRADES, {
    variables: { symbol, limit },
    skip: !symbol,
    pollInterval: 1000,
  });
};

export const useHistoricalData = (
  symbol: string,
  interval: string,
  startTime?: number,
  endTime?: number,
  limit = 100
) => {
  return useQuery(GET_HISTORICAL_DATA, {
    variables: { symbol, interval, startTime, endTime, limit },
    skip: !symbol || !interval,
  });
};

export const useMarketSummary = (symbols: string[]) => {
  return useQuery(GET_MARKET_SUMMARY, {
    variables: { symbols },
    skip: symbols.length === 0,
    pollInterval: 5000,
  });
};

// ==================== ANALYTICS HOOKS ====================

export const usePortfolioPerformance = (period = "1M") => {
  const { user } = useAuthStore();
  return useQuery(GET_PORTFOLIO_PERFORMANCE, {
    variables: { userId: user?.id || "demo-user", period },
    skip: !user?.id,
    pollInterval: 30000, // Less frequent for performance metrics
  });
};

export const useRiskMetrics = () => {
  const { user } = useAuthStore();
  return useQuery(GET_RISK_METRICS, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
    pollInterval: 10000,
  });
};

export const useTradeAnalytics = (symbol?: string, period = "1M") => {
  const { user } = useAuthStore();
  return useQuery(GET_TRADE_ANALYTICS, {
    variables: {
      userId: user?.id || "demo-user",
      symbol,
      period,
    },
    skip: !user?.id,
    pollInterval: 30000,
  });
};

// ==================== DASHBOARD HOOKS ====================

export const useDashboardOverview = () => {
  const { user } = useAuthStore();
  return useQuery(GET_DASHBOARD_OVERVIEW, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
    pollInterval: 5000,
  });
};

// ==================== MUTATION HOOKS ====================

export const usePlaceOrder = () => {
  return useMutation(PLACE_ORDER, {
    refetchQueries: [
      "GetActiveOrders",
      "GetOrderHistory",
      "GetPortfolio",
      "GetPositions",
    ],
    awaitRefetchQueries: true,
  });
};

export const useCancelOrder = () => {
  return useMutation(CANCEL_ORDER, {
    refetchQueries: ["GetActiveOrders", "GetOrderHistory", "GetPortfolio"],
    awaitRefetchQueries: true,
  });
};

export const useModifyOrder = () => {
  return useMutation(MODIFY_ORDER, {
    refetchQueries: ["GetActiveOrders", "GetOrderHistory"],
    awaitRefetchQueries: true,
  });
};

export const useUpdateWatchlist = () => {
  return useMutation(UPDATE_WATCHLIST, {
    refetchQueries: ["GetDashboardOverview"],
    awaitRefetchQueries: true,
  });
};

// ==================== SUBSCRIPTION HOOKS ====================

export const usePortfolioUpdates = () => {
  const { user } = useAuthStore();
  return useSubscription(PORTFOLIO_UPDATES_SUBSCRIPTION, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
  });
};

export const usePositionUpdates = () => {
  const { user } = useAuthStore();
  return useSubscription(POSITION_UPDATES_SUBSCRIPTION, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
  });
};

export const useMarketDataUpdates = (symbols: string[]) => {
  return useSubscription(MARKET_DATA_UPDATES_SUBSCRIPTION, {
    variables: { symbols },
    skip: symbols.length === 0,
  });
};

export const useTradeUpdates = (symbols: string[]) => {
  return useSubscription(TRADE_UPDATES_SUBSCRIPTION, {
    variables: { symbols },
    skip: symbols.length === 0,
  });
};

export const useOrderUpdates = () => {
  const { user } = useAuthStore();
  return useSubscription(ORDER_UPDATES_SUBSCRIPTION, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
  });
};

export const useDashboardUpdates = () => {
  const { user } = useAuthStore();
  return useSubscription(DASHBOARD_UPDATES_SUBSCRIPTION, {
    variables: { userId: user?.id || "demo-user" },
    skip: !user?.id,
  });
};

// ==================== UTILITY HOOKS ====================

export const useGraphQLError = () => {
  // Custom hook to handle GraphQL errors consistently
  const handleError = (error: any) => {
    console.error("GraphQL Error:", error);

    if (error.networkError) {
      console.error("Network Error:", error.networkError);
    }

    if (error.graphQLErrors) {
      error.graphQLErrors.forEach((err: any) => {
        console.error("GraphQL Error:", err.message);
      });
    }
  };

  return { handleError };
};

export const useGraphQLLoading = () => {
  // Custom hook to handle loading states consistently
  const isLoading = (queries: any[]) => {
    return queries.some((query) => query.loading);
  };

  const hasError = (queries: any[]) => {
    return queries.some((query) => query.error);
  };

  return { isLoading, hasError };
};
