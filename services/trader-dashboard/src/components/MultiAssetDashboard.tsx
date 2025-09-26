import React, { useState, useEffect, useCallback } from "react";
import {
  MultiAssetManager,
  AssetTypeConfig,
} from "../lib/multi-asset/multi-asset-manager";
import { AssetType } from "../types/asset-types";
import { Symbol } from "../types/symbol";
import { MarketData } from "../types/market-data";
import { Position } from "../types/position";

interface MultiAssetDashboardProps {
  userId: string;
}

export const MultiAssetDashboard: React.FC<MultiAssetDashboardProps> = ({
  userId,
}) => {
  const [multiAssetManager] = useState(() => new MultiAssetManager());
  const [selectedAssetType, setSelectedAssetType] = useState<AssetType>(
    AssetType.CRYPTO
  );
  const [symbols, setSymbols] = useState<Symbol[]>([]);
  const [marketData, setMarketData] = useState<Map<string, MarketData>>(
    new Map()
  );
  const [positions, setPositions] = useState<Position[]>([]);
  const [portfolio, setPortfolio] = useState<any>(null);
  const [assetAllocation, setAssetAllocation] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Load symbols for selected asset type
  const loadSymbols = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const symbolsData = await multiAssetManager.getSymbolsForAssetType(
        selectedAssetType
      );
      setSymbols(symbolsData);
    } catch (err) {
      setError("Failed to load symbols");
      console.error("Error loading symbols:", err);
    } finally {
      setLoading(false);
    }
  }, [selectedAssetType, multiAssetManager]);

  // Load market data for symbols
  const loadMarketData = useCallback(async () => {
    if (symbols.length === 0) return;

    try {
      const symbolNames = symbols.map((s) => s.symbol);
      const marketDataMap = await multiAssetManager.getMarketDataBatch(
        symbolNames
      );
      setMarketData(marketDataMap);
    } catch (err) {
      console.error("Error loading market data:", err);
    }
  }, [symbols, multiAssetManager]);

  // Load positions for user
  const loadPositions = useCallback(async () => {
    try {
      const positionsData = await multiAssetManager.getUserPositions(
        userId,
        selectedAssetType
      );
      setPositions(positionsData);
    } catch (err) {
      console.error("Error loading positions:", err);
    }
  }, [userId, selectedAssetType, multiAssetManager]);

  // Load portfolio
  const loadPortfolio = useCallback(async () => {
    try {
      const portfolioData = await multiAssetManager.getUserPortfolio(userId);
      setPortfolio(portfolioData);
    } catch (err) {
      console.error("Error loading portfolio:", err);
    }
  }, [userId, multiAssetManager]);

  // Load asset allocation
  const loadAssetAllocation = useCallback(async () => {
    try {
      const allocationData = await multiAssetManager.getAssetAllocation(userId);
      setAssetAllocation(allocationData);
    } catch (err) {
      console.error("Error loading asset allocation:", err);
    }
  }, [userId, multiAssetManager]);

  // Initial load
  useEffect(() => {
    loadSymbols();
  }, [loadSymbols]);

  // Load market data when symbols change
  useEffect(() => {
    loadMarketData();
  }, [loadMarketData]);

  // Load positions when asset type changes
  useEffect(() => {
    loadPositions();
  }, [loadPositions]);

  // Load portfolio and allocation
  useEffect(() => {
    loadPortfolio();
    loadAssetAllocation();
  }, [loadPortfolio, loadAssetAllocation]);

  // Refresh market data periodically
  useEffect(() => {
    const interval = setInterval(() => {
      loadMarketData();
    }, 5000); // Refresh every 5 seconds

    return () => clearInterval(interval);
  }, [loadMarketData]);

  // Handle asset type change
  const handleAssetTypeChange = (assetType: AssetType) => {
    setSelectedAssetType(assetType);
    setSymbols([]);
    setMarketData(new Map());
    setPositions([]);
  };

  // Handle order placement
  // const _handlePlaceOrder = async (order: Order) => {
  //   try {
  //     setLoading(true);
  //     const placedOrder = await multiAssetManager.placeOrder(order);
  //     if (placedOrder) {
  //       // Refresh positions and portfolio
  //       await loadPositions();
  //       await loadPortfolio();
  //       await loadAssetAllocation();
  //     }
  //   } catch (err) {
  //     setError("Failed to place order");
  //     console.error("Error placing order:", err);
  //   } finally {
  //     setLoading(false);
  //   }
  // };

  // Handle order cancellation
  // const _handleCancelOrder = async (orderId: string) => {
  //   try {
  //     const success = await multiAssetManager.cancelOrder(orderId);
  //     if (success) {
  //       // Refresh positions and portfolio
  //       await loadPositions();
  //       await loadPortfolio();
  //       await loadAssetAllocation();
  //     }
  //   } catch (err) {
  //     setError("Failed to cancel order");
  //     console.error("Error cancelling order:", err);
  //   }
  // };

  // Handle order modification
  // const _handleModifyOrder = async (
  //   orderId: string,
  //   quantity: number,
  //   price: number
  // ) => {
  //   try {
  //     const modifiedOrder = await multiAssetManager.modifyOrder(
  //       orderId,
  //       quantity,
  //       price
  //     );
  //     if (modifiedOrder) {
  //       // Refresh positions and portfolio
  //       await loadPositions();
  //       await loadPortfolio();
  //       await loadAssetAllocation();
  //     }
  //   } catch (err) {
  //     setError("Failed to modify order");
  //     console.error("Error modifying order:", err);
  //   }
  // };

  // Get asset type configuration
  const getAssetTypeConfig = (
    assetType: AssetType
  ): AssetTypeConfig | undefined => {
    return multiAssetManager.getAssetTypeConfig(assetType);
  };

  // Check if market is open
  const isMarketOpen = (assetType: AssetType): boolean => {
    return multiAssetManager.isMarketOpen(assetType);
  };

  // Format price
  const formatPrice = (price: number, assetType: AssetType): string => {
    return multiAssetManager.formatPrice(price, assetType);
  };

  // Format quantity
  const formatQuantity = (quantity: number, assetType: AssetType): string => {
    return multiAssetManager.formatQuantity(quantity, assetType);
  };

  // Get market status
  const getMarketStatus = (assetType: AssetType) => {
    const config = getAssetTypeConfig(assetType);
    const isOpen = isMarketOpen(assetType);
    const nextOpen = multiAssetManager.getNextMarketOpen(assetType);
    const nextClose = multiAssetManager.getNextMarketClose(assetType);

    return {
      config,
      isOpen,
      nextOpen,
      nextClose,
    };
  };

  if (loading && symbols.length === 0) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        <strong className="font-bold">Error:</strong>
        <span className="block sm:inline"> {error}</span>
        <button
          onClick={() => {
            setError(null);
            loadSymbols();
          }}
          className="ml-4 bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600"
        >
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Asset Type Selector */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Asset Type Selection</h2>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
          {multiAssetManager.getSupportedAssetTypes().map((assetType) => {
            const config = getAssetTypeConfig(assetType);
            const isOpen = isMarketOpen(assetType);
            const isSelected = selectedAssetType === assetType;

            return (
              <button
                key={assetType}
                onClick={() => handleAssetTypeChange(assetType)}
                className={`p-4 rounded-lg border-2 transition-all ${
                  isSelected
                    ? "border-blue-500 bg-blue-50"
                    : "border-gray-200 hover:border-gray-300"
                }`}
              >
                <div className="text-left">
                  <div className="font-medium text-sm">{config?.name}</div>
                  <div className="text-xs text-gray-500 mt-1">
                    {config?.description}
                  </div>
                  <div
                    className={`text-xs mt-2 ${
                      isOpen ? "text-green-600" : "text-red-600"
                    }`}
                  >
                    {isOpen ? "Market Open" : "Market Closed"}
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Market Status */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">Market Status</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {multiAssetManager.getSupportedAssetTypes().map((assetType) => {
            const status = getMarketStatus(assetType);

            return (
              <div key={assetType} className="border rounded-lg p-4">
                <div className="font-medium">{status.config?.name}</div>
                <div className="text-sm text-gray-600 mt-1">
                  Trading Hours: {status.config?.tradingHours}
                </div>
                <div
                  className={`text-sm mt-2 ${
                    status.isOpen ? "text-green-600" : "text-red-600"
                  }`}
                >
                  {status.isOpen ? "Market Open" : "Market Closed"}
                </div>
                {status.nextOpen && (
                  <div className="text-xs text-gray-500 mt-1">
                    Next Open: {status.nextOpen.toLocaleString()}
                  </div>
                )}
                {status.nextClose && (
                  <div className="text-xs text-gray-500 mt-1">
                    Next Close: {status.nextClose.toLocaleString()}
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Portfolio Overview */}
      {portfolio && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Portfolio Overview</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-blue-50 p-4 rounded-lg">
              <div className="text-sm text-blue-600">Total Value</div>
              <div className="text-2xl font-bold text-blue-800">
                ${portfolio.totalValue?.toLocaleString() || "0"}
              </div>
            </div>
            <div className="bg-green-50 p-4 rounded-lg">
              <div className="text-sm text-green-600">Total P&L</div>
              <div
                className={`text-2xl font-bold ${
                  portfolio.totalPnL >= 0 ? "text-green-800" : "text-red-800"
                }`}
              >
                ${portfolio.totalPnL?.toLocaleString() || "0"}
              </div>
            </div>
            <div className="bg-purple-50 p-4 rounded-lg">
              <div className="text-sm text-purple-600">Total Return</div>
              <div
                className={`text-2xl font-bold ${
                  portfolio.totalReturn >= 0
                    ? "text-purple-800"
                    : "text-red-800"
                }`}
              >
                {portfolio.totalReturn?.toFixed(2) || "0"}%
              </div>
            </div>
            <div className="bg-orange-50 p-4 rounded-lg">
              <div className="text-sm text-orange-600">Positions</div>
              <div className="text-2xl font-bold text-orange-800">
                {positions.length}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Asset Allocation */}
      {assetAllocation.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Asset Allocation</h2>
          <div className="space-y-3">
            {assetAllocation.map((allocation, index) => (
              <div key={index} className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-4 h-4 bg-blue-500 rounded"></div>
                  <span className="font-medium">{allocation.assetType}</span>
                </div>
                <div className="text-right">
                  <div className="font-medium">
                    ${allocation.value?.toLocaleString()}
                  </div>
                  <div className="text-sm text-gray-500">
                    {allocation.percentage?.toFixed(2)}%
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Symbols and Market Data */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-semibold mb-4">
          {getAssetTypeConfig(selectedAssetType)?.name} Symbols
        </h2>
        {symbols.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Symbol
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Price
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Change
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Volume
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {symbols.map((symbol) => {
                  const data = marketData.get(symbol.symbol);
                  return (
                    <tr key={symbol.symbol}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-gray-900">
                          {symbol.symbol}
                        </div>
                        <div className="text-sm text-gray-500">
                          {symbol.baseSymbol}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {data
                            ? formatPrice(data.price, selectedAssetType)
                            : "N/A"}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div
                          className={`text-sm ${
                            data && data.change >= 0
                              ? "text-green-600"
                              : "text-red-600"
                          }`}
                        >
                          {data
                            ? `${
                                data.change >= 0 ? "+" : ""
                              }${data.change.toFixed(2)}`
                            : "N/A"}
                        </div>
                        <div
                          className={`text-xs ${
                            data && data.changePercent >= 0
                              ? "text-green-600"
                              : "text-red-600"
                          }`}
                        >
                          {data
                            ? `${
                                data.changePercent >= 0 ? "+" : ""
                              }${data.changePercent.toFixed(2)}%`
                            : "N/A"}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm text-gray-900">
                          {data
                            ? formatQuantity(data.volume, selectedAssetType)
                            : "N/A"}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <button
                          onClick={() => {
                            // Open trading dialog for this symbol
                            console.log("Trade symbol:", symbol.symbol);
                          }}
                          className="text-blue-600 hover:text-blue-900"
                        >
                          Trade
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-center py-8 text-gray-500">
            No symbols available for{" "}
            {getAssetTypeConfig(selectedAssetType)?.name}
          </div>
        )}
      </div>

      {/* Positions */}
      {positions.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Current Positions</h2>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Symbol
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Quantity
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Avg Price
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Current Price
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    P&L
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {positions.map((position) => (
                  <tr key={position.symbol}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">
                        {position.symbol}
                      </div>
                      <div className="text-sm text-gray-500">
                        {position.assetType}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {formatQuantity(position.quantity, position.assetType)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {formatPrice(position.averagePrice, position.assetType)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm text-gray-900">
                        {formatPrice(position.currentPrice, position.assetType)}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div
                        className={`text-sm ${
                          position.unrealizedPnL >= 0
                            ? "text-green-600"
                            : "text-red-600"
                        }`}
                      >
                        {formatPrice(
                          position.unrealizedPnL,
                          position.assetType
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button
                        onClick={() => {
                          // Open position management dialog
                          console.log("Manage position:", position.symbol);
                        }}
                        className="text-blue-600 hover:text-blue-900"
                      >
                        Manage
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default MultiAssetDashboard;
