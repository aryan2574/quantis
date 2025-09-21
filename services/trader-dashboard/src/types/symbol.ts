import { AssetType } from "./asset-types";

// Symbol type definitions
export interface Symbol {
  symbol: string;
  baseSymbol: string;
  name: string;
  exchange: string;
  assetType: AssetType;
  isActive: boolean;
  lastPrice?: number;
  lastUpdate?: string;
}

export interface SymbolData {
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
