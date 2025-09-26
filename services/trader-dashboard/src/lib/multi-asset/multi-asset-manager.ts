import { AssetType } from "../../types/asset-types";
import { Symbol } from "../../types/symbol";
import { MarketData } from "../../types/market-data";
import { Order } from "../../types/order";
import { Position } from "../../types/position";

/**
 * Multi-Asset Manager
 *
 * Handles frontend operations for all asset classes
 */
export class MultiAssetManager {
  private assetTypeConfigs: Map<AssetType, AssetTypeConfig> = new Map();
  private symbolCache: Map<string, Symbol> = new Map();
  private marketDataCache: Map<string, MarketData> = new Map();

  constructor() {
    this.initializeAssetTypeConfigs();
  }

  /**
   * Initialize asset type configurations
   */
  private initializeAssetTypeConfigs(): void {
    // Crypto Spot
    this.assetTypeConfigs.set(AssetType.CRYPTO_SPOT, {
      name: "Cryptocurrency Spot",
      description: "Direct cryptocurrency trading",
      tradingHours: "24/7",
      minOrderSize: 0.001,
      maxOrderSize: 1000000,
      defaultLeverage: 1,
      requiresMargin: false,
      settlementPeriod: "T+0",
      dataRefreshInterval: 1000,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 8,
      quantityPrecision: 8,
    });

    // Crypto Futures
    this.assetTypeConfigs.set(AssetType.CRYPTO_FUTURES, {
      name: "Cryptocurrency Futures",
      description: "Cryptocurrency futures contracts",
      tradingHours: "24/7",
      minOrderSize: 0.001,
      maxOrderSize: 1000000,
      defaultLeverage: 10,
      requiresMargin: true,
      settlementPeriod: "T+0",
      dataRefreshInterval: 1000,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 8,
      quantityPrecision: 8,
    });

    // Crypto Perpetual
    this.assetTypeConfigs.set(AssetType.CRYPTO_PERPETUAL, {
      name: "Cryptocurrency Perpetual",
      description: "Perpetual cryptocurrency contracts",
      tradingHours: "24/7",
      minOrderSize: 0.001,
      maxOrderSize: 1000000,
      defaultLeverage: 20,
      requiresMargin: true,
      settlementPeriod: "T+0",
      dataRefreshInterval: 1000,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 8,
      quantityPrecision: 8,
    });

    // Forex
    this.assetTypeConfigs.set(AssetType.FOREX, {
      name: "Foreign Exchange",
      description: "Currency pair trading",
      tradingHours: "24/5",
      minOrderSize: 1000,
      maxOrderSize: 10000000,
      defaultLeverage: 50,
      requiresMargin: true,
      settlementPeriod: "T+2",
      dataRefreshInterval: 500,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 5,
      quantityPrecision: 2,
    });

    // Stocks
    this.assetTypeConfigs.set(AssetType.STOCKS, {
      name: "Stocks",
      description: "Equity securities",
      tradingHours: "9:30-16:00 EST",
      minOrderSize: 1,
      maxOrderSize: 1000000,
      defaultLeverage: 1,
      requiresMargin: false,
      settlementPeriod: "T+2",
      dataRefreshInterval: 1000,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 2,
      quantityPrecision: 0,
    });

    // Futures
    this.assetTypeConfigs.set(AssetType.FUTURES, {
      name: "Futures",
      description: "Commodity and financial futures",
      tradingHours: "Varies by contract",
      minOrderSize: 1,
      maxOrderSize: 10000,
      defaultLeverage: 10,
      requiresMargin: true,
      settlementPeriod: "T+0",
      dataRefreshInterval: 500,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 4,
      quantityPrecision: 0,
    });

    // Options
    this.assetTypeConfigs.set(AssetType.OPTIONS, {
      name: "Options",
      description: "Equity options contracts",
      tradingHours: "9:30-16:00 EST",
      minOrderSize: 1,
      maxOrderSize: 10000,
      defaultLeverage: 1,
      requiresMargin: true,
      settlementPeriod: "T+1",
      dataRefreshInterval: 1000,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 2,
      quantityPrecision: 0,
    });

    // Crypto Options
    this.assetTypeConfigs.set(AssetType.CRYPTO_OPTIONS, {
      name: "Cryptocurrency Options",
      description: "Cryptocurrency options contracts",
      tradingHours: "24/7",
      minOrderSize: 0.001,
      maxOrderSize: 1000000,
      defaultLeverage: 1,
      requiresMargin: true,
      settlementPeriod: "T+0",
      dataRefreshInterval: 1000,
      supportedOrderTypes: ["MARKET", "LIMIT", "STOP", "STOP_LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 8,
      quantityPrecision: 8,
    });

    // Enterprise Tokens
    this.assetTypeConfigs.set(AssetType.ENTERPRISE_TOKENS, {
      name: "Enterprise Tokens",
      description: "Private enterprise tokens",
      tradingHours: "24/7",
      minOrderSize: 1,
      maxOrderSize: 1000000,
      defaultLeverage: 1,
      requiresMargin: false,
      settlementPeriod: "T+0",
      dataRefreshInterval: 5000,
      supportedOrderTypes: ["MARKET", "LIMIT"],
      supportedSides: ["BUY", "SELL"],
      pricePrecision: 2,
      quantityPrecision: 0,
    });
  }

  /**
   * Get asset type configuration
   */
  public getAssetTypeConfig(assetType: AssetType): AssetTypeConfig | undefined {
    return this.assetTypeConfigs.get(assetType);
  }

  /**
   * Get all supported asset types
   */
  public getSupportedAssetTypes(): AssetType[] {
    return Array.from(this.assetTypeConfigs.keys());
  }

  /**
   * Get symbols for asset type
   */
  public async getSymbolsForAssetType(assetType: AssetType): Promise<Symbol[]> {
    const cacheKey = `symbols_${assetType}`;
    const cachedSymbols = this.symbolCache.get(cacheKey);
    if (cachedSymbols) {
      return [cachedSymbols];
    }

    try {
      const response = await fetch(`/api/symbols?assetType=${assetType}`);
      const symbols: Symbol[] = await response.json();

      // Cache symbols
      symbols.forEach((symbol) => {
        this.symbolCache.set(symbol.symbol, symbol);
      });

      return symbols;
    } catch (error) {
      console.error("Error fetching symbols:", error);
      return [];
    }
  }

  /**
   * Get market data for symbol
   */
  public async getMarketData(symbol: string): Promise<MarketData | null> {
    const cachedData = this.marketDataCache.get(symbol);
    if (cachedData && !this.isMarketDataStale(cachedData)) {
      return cachedData;
    }

    try {
      const response = await fetch(`/api/market-data/${symbol}`);
      const marketData: MarketData = await response.json();

      // Cache market data
      this.marketDataCache.set(symbol, marketData);

      return marketData;
    } catch (error) {
      console.error("Error fetching market data:", error);
      return null;
    }
  }

  /**
   * Get market data for multiple symbols
   */
  public async getMarketDataBatch(
    symbols: string[]
  ): Promise<Map<string, MarketData>> {
    const marketDataMap = new Map<string, MarketData>();

    try {
      const response = await fetch("/api/market-data/batch", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ symbols }),
      });

      const data: { [key: string]: MarketData } = await response.json();

      Object.entries(data).forEach(([symbol, marketData]) => {
        marketDataMap.set(symbol, marketData);
        this.marketDataCache.set(symbol, marketData);
      });

      return marketDataMap;
    } catch (error) {
      console.error("Error fetching batch market data:", error);
      return marketDataMap;
    }
  }

  /**
   * Place order for any asset type
   */
  public async placeOrder(order: Order): Promise<Order | null> {
    try {
      const response = await fetch("/api/orders", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(order),
      });

      if (!response.ok) {
        throw new Error(`Order placement failed: ${response.statusText}`);
      }

      const placedOrder: Order = await response.json();
      return placedOrder;
    } catch (error) {
      console.error("Error placing order:", error);
      return null;
    }
  }

  /**
   * Cancel order
   */
  public async cancelOrder(orderId: string): Promise<boolean> {
    try {
      const response = await fetch(`/api/orders/${orderId}/cancel`, {
        method: "POST",
      });

      return response.ok;
    } catch (error) {
      console.error("Error cancelling order:", error);
      return false;
    }
  }

  /**
   * Modify order
   */
  public async modifyOrder(
    orderId: string,
    quantity: number,
    price: number
  ): Promise<Order | null> {
    try {
      const response = await fetch(`/api/orders/${orderId}/modify`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ quantity, price }),
      });

      if (!response.ok) {
        throw new Error(`Order modification failed: ${response.statusText}`);
      }

      const modifiedOrder: Order = await response.json();
      return modifiedOrder;
    } catch (error) {
      console.error("Error modifying order:", error);
      return null;
    }
  }

  /**
   * Get positions for user
   */
  public async getUserPositions(
    userId: string,
    assetType?: AssetType
  ): Promise<Position[]> {
    try {
      const url = assetType
        ? `/api/positions?userId=${userId}&assetType=${assetType}`
        : `/api/positions?userId=${userId}`;

      const response = await fetch(url);
      const positions: Position[] = await response.json();

      return positions;
    } catch (error) {
      console.error("Error fetching positions:", error);
      return [];
    }
  }

  /**
   * Get portfolio for user
   */
  public async getUserPortfolio(userId: string): Promise<any> {
    try {
      const response = await fetch(`/api/portfolio/${userId}`);
      const portfolio = await response.json();

      return portfolio;
    } catch (error) {
      console.error("Error fetching portfolio:", error);
      return null;
    }
  }

  /**
   * Get asset allocation for portfolio
   */
  public async getAssetAllocation(userId: string): Promise<any[]> {
    try {
      const response = await fetch(`/api/portfolio/${userId}/allocation`);
      const allocation = await response.json();

      return allocation;
    } catch (error) {
      console.error("Error fetching asset allocation:", error);
      return [];
    }
  }

  /**
   * Get portfolio performance
   */
  public async getPortfolioPerformance(
    userId: string,
    period: string
  ): Promise<any> {
    try {
      const response = await fetch(
        `/api/portfolio/${userId}/performance?period=${period}`
      );
      const performance = await response.json();

      return performance;
    } catch (error) {
      console.error("Error fetching portfolio performance:", error);
      return null;
    }
  }

  /**
   * Get portfolio risk metrics
   */
  public async getPortfolioRiskMetrics(userId: string): Promise<any> {
    try {
      const response = await fetch(`/api/portfolio/${userId}/risk`);
      const riskMetrics = await response.json();

      return riskMetrics;
    } catch (error) {
      console.error("Error fetching portfolio risk metrics:", error);
      return null;
    }
  }

  /**
   * Check if market is open for asset type
   */
  public isMarketOpen(assetType: AssetType): boolean {
    const config = this.assetTypeConfigs.get(assetType);
    if (!config) return false;

    if (config.tradingHours === "24/7") {
      return true;
    }

    // Implement market hours logic
    const now = new Date();
    const hour = now.getHours();
    const day = now.getDay();

    if (config.tradingHours === "24/5") {
      // Forex: 24/5 (Sunday 5 PM EST to Friday 5 PM EST)
      return day !== 5 || hour < 17; // Friday 5 PM EST
    }

    if (config.tradingHours === "9:30-16:00 EST") {
      // Stocks: 9:30 AM to 4:00 PM EST, Monday to Friday
      if (day === 0 || day === 6) return false; // Weekend
      return hour >= 9 && hour < 16;
    }

    return true; // Default to open
  }

  /**
   * Get next market open time
   */
  public getNextMarketOpen(assetType: AssetType): Date | null {
    const config = this.assetTypeConfigs.get(assetType);
    if (!config) return null;

    if (config.tradingHours === "24/7") {
      return null; // Always open
    }

    // Implement next market open logic
    const now = new Date();
    const nextOpen = new Date(now);

    if (config.tradingHours === "24/5") {
      // Forex: Next Sunday 5 PM EST
      const daysUntilSunday = (7 - now.getDay()) % 7;
      nextOpen.setDate(now.getDate() + daysUntilSunday);
      nextOpen.setHours(17, 0, 0, 0);
    } else if (config.tradingHours === "9:30-16:00 EST") {
      // Stocks: Next Monday 9:30 AM EST
      const daysUntilMonday = (8 - now.getDay()) % 7;
      nextOpen.setDate(now.getDate() + daysUntilMonday);
      nextOpen.setHours(9, 30, 0, 0);
    }

    return nextOpen;
  }

  /**
   * Get next market close time
   */
  public getNextMarketClose(assetType: AssetType): Date | null {
    const config = this.assetTypeConfigs.get(assetType);
    if (!config) return null;

    if (config.tradingHours === "24/7") {
      return null; // Never closes
    }

    // Implement next market close logic
    const now = new Date();
    const nextClose = new Date(now);

    if (config.tradingHours === "24/5") {
      // Forex: Next Friday 5 PM EST
      const daysUntilFriday = (5 - now.getDay() + 7) % 7;
      nextClose.setDate(now.getDate() + daysUntilFriday);
      nextClose.setHours(17, 0, 0, 0);
    } else if (config.tradingHours === "9:30-16:00 EST") {
      // Stocks: Today 4:00 PM EST (if market is open) or next Friday 4:00 PM EST
      if (this.isMarketOpen(assetType)) {
        nextClose.setHours(16, 0, 0, 0);
      } else {
        const daysUntilFriday = (5 - now.getDay() + 7) % 7;
        nextClose.setDate(now.getDate() + daysUntilFriday);
        nextClose.setHours(16, 0, 0, 0);
      }
    }

    return nextClose;
  }

  /**
   * Format price for asset type
   */
  public formatPrice(price: number, assetType: AssetType): string {
    const config = this.assetTypeConfigs.get(assetType);
    if (!config) return price.toFixed(2);

    return price.toFixed(config.pricePrecision);
  }

  /**
   * Format quantity for asset type
   */
  public formatQuantity(quantity: number, assetType: AssetType): string {
    const config = this.assetTypeConfigs.get(assetType);
    if (!config) return quantity.toFixed(0);

    return quantity.toFixed(config.quantityPrecision);
  }

  /**
   * Validate order for asset type
   */
  public validateOrder(order: Order): string[] {
    const errors: string[] = [];
    const config = this.assetTypeConfigs.get(order.assetType);

    if (!config) {
      errors.push("Unsupported asset type");
      return errors;
    }

    // Validate quantity
    if (order.quantity < config.minOrderSize) {
      errors.push(`Minimum order size is ${config.minOrderSize}`);
    }
    if (order.quantity > config.maxOrderSize) {
      errors.push(`Maximum order size is ${config.maxOrderSize}`);
    }

    // Validate order type
    if (!config.supportedOrderTypes.includes(order.orderType)) {
      errors.push(
        `Order type ${order.orderType} not supported for ${config.name}`
      );
    }

    // Validate side
    if (!config.supportedSides.includes(order.side)) {
      errors.push(`Order side ${order.side} not supported for ${config.name}`);
    }

    // Validate market hours
    if (!this.isMarketOpen(order.assetType)) {
      errors.push(`Market is closed for ${config.name}`);
    }

    return errors;
  }

  /**
   * Check if market data is stale
   */
  private isMarketDataStale(marketData: MarketData): boolean {
    const now = Date.now();
    const dataAge = now - marketData.timestamp.getTime();
    return dataAge > 5000; // 5 seconds
  }

  /**
   * Clear cache
   */
  public clearCache(): void {
    this.symbolCache.clear();
    this.marketDataCache.clear();
  }
}

/**
 * Asset Type Configuration Interface
 */
export interface AssetTypeConfig {
  name: string;
  description: string;
  tradingHours: string;
  minOrderSize: number;
  maxOrderSize: number;
  defaultLeverage: number;
  requiresMargin: boolean;
  settlementPeriod: string;
  dataRefreshInterval: number;
  supportedOrderTypes: string[];
  supportedSides: string[];
  pricePrecision: number;
  quantityPrecision: number;
}
