import { AssetType } from "./asset-types";

// Position type definitions
export interface Position {
  symbol: string;
  assetType: AssetType;
  quantity: number;
  averagePrice: number;
  currentPrice: number;
  unrealizedPnL: number;
  realizedPnL: number;
}

export interface Portfolio {
  balance: number;
  positions: Position[];
  totalValue: number;
  totalPnL: number;
}

export interface CashBalance {
  currency: string;
  amount: number;
  available: number;
  reserved: number;
}

export interface PerformanceMetrics {
  totalReturn: number;
  dailyReturn: number;
  weeklyReturn: number;
  monthlyReturn: number;
  yearlyReturn: number;
  sharpeRatio: number;
  maxDrawdown: number;
}
