// Asset type definitions
export enum AssetType {
  STOCK = "STOCK",
  STOCKS = "STOCKS", // Alias for STOCK
  CRYPTO = "CRYPTO",
  CRYPTO_SPOT = "CRYPTO_SPOT",
  CRYPTO_FUTURES = "CRYPTO_FUTURES",
  CRYPTO_PERPETUAL = "CRYPTO_PERPETUAL",
  CRYPTO_OPTIONS = "CRYPTO_OPTIONS",
  FOREX = "FOREX",
  COMMODITY = "COMMODITY",
  BOND = "BOND",
  ETF = "ETF",
  OPTION = "OPTION",
  OPTIONS = "OPTIONS", // Alias for OPTION
  FUTURE = "FUTURE",
  FUTURES = "FUTURES", // Alias for FUTURE
  ENTERPRISE_TOKENS = "ENTERPRISE_TOKENS",
}

export interface Asset {
  symbol: string;
  name: string;
  type: AssetType;
  exchange: string;
  isActive: boolean;
  lastPrice?: number;
  lastUpdate?: string;
}

export interface AssetMetadata {
  symbol: string;
  name: string;
  description?: string;
  sector?: string;
  industry?: string;
  marketCap?: number;
  currency: string;
}
