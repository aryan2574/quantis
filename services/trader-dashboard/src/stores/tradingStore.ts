import { create } from "zustand";

export interface OrderBookLevel {
  price: number;
  quantity: number;
  orders: number;
}

export interface OrderBook {
  symbol: string;
  bids: OrderBookLevel[];
  asks: OrderBookLevel[];
  lastUpdate: string;
}

export interface Trade {
  id: string;
  symbol: string;
  price: number;
  quantity: number;
  side: "BUY" | "SELL";
  timestamp: string;
  orderId: string;
}

export interface MarketData {
  symbol: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  high: number;
  low: number;
  open: number;
  lastUpdate: string;
}

export interface Order {
  id: string;
  symbol: string;
  side: "BUY" | "SELL";
  quantity: number;
  price: number;
  orderType: "MARKET" | "LIMIT" | "STOP";
  status: "PENDING" | "FILLED" | "CANCELLED" | "REJECTED";
  timestamp: string;
  filledQuantity?: number;
  averagePrice?: number;
}

export interface Portfolio {
  balance: number;
  positions: {
    symbol: string;
    quantity: number;
    averagePrice: number;
    currentPrice: number;
    unrealizedPnL: number;
    realizedPnL: number;
  }[];
  totalValue: number;
  totalPnL: number;
}

interface TradingState {
  // Order Book
  orderBooks: Record<string, OrderBook>;
  selectedSymbol: string;

  // Market Data
  marketData: Record<string, MarketData>;

  // Trades
  recentTrades: Trade[];

  // Orders
  orders: Order[];

  // Portfolio
  portfolio: Portfolio | null;

  // AI Recommendations
  recommendations: {
    id: string;
    type: "ALERT" | "RECOMMENDATION" | "ANOMALY";
    title: string;
    message: string;
    severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
    timestamp: string;
    symbol?: string;
  }[];

  // Actions
  setSelectedSymbol: (symbol: string) => void;
  updateOrderBook: (symbol: string, orderBook: OrderBook) => void;
  updateMarketData: (symbol: string, data: MarketData) => void;
  addTrade: (trade: Trade) => void;
  addOrder: (order: Order) => void;
  updateOrder: (orderId: string, updates: Partial<Order>) => void;
  setPortfolio: (portfolio: Portfolio) => void;
  addRecommendation: (
    recommendation: Omit<TradingState["recommendations"][0], "id" | "timestamp">
  ) => void;
  clearRecommendations: () => void;
}

export const useTradingStore = create<TradingState>((set) => ({
  orderBooks: {},
  selectedSymbol: "AAPL",
  marketData: {},
  recentTrades: [],
  orders: [],
  portfolio: null,
  recommendations: [],

  setSelectedSymbol: (symbol: string) => {
    set({ selectedSymbol: symbol });
  },

  updateOrderBook: (symbol: string, orderBook: OrderBook) => {
    set((state) => ({
      orderBooks: {
        ...state.orderBooks,
        [symbol]: orderBook,
      },
    }));
  },

  updateMarketData: (symbol: string, data: MarketData) => {
    set((state) => ({
      marketData: {
        ...state.marketData,
        [symbol]: data,
      },
    }));
  },

  addTrade: (trade: Trade) => {
    set((state) => ({
      recentTrades: [trade, ...state.recentTrades].slice(0, 100), // Keep last 100 trades
    }));
  },

  addOrder: (order: Order) => {
    set((state) => ({
      orders: [order, ...state.orders],
    }));
  },

  updateOrder: (orderId: string, updates: Partial<Order>) => {
    set((state) => ({
      orders: state.orders.map((order) =>
        order.id === orderId ? { ...order, ...updates } : order
      ),
    }));
  },

  setPortfolio: (portfolio: Portfolio) => {
    set({ portfolio });
  },

  addRecommendation: (recommendation) => {
    const newRecommendation = {
      ...recommendation,
      id: `rec_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      timestamp: new Date().toISOString(),
    };

    set((state) => ({
      recommendations: [newRecommendation, ...state.recommendations].slice(
        0,
        50
      ), // Keep last 50 recommendations
    }));
  },

  clearRecommendations: () => {
    set({ recommendations: [] });
  },
}));
