import { useEffect, useState } from "react";
import { useRecentTrades } from "@/lib/graphql/hooks";
import { formatCurrency, formatNumber, formatTimestamp } from "@/lib/utils";
import { ArrowUp, ArrowDown } from "lucide-react";

interface TradeBlotterProps {
  symbol: string;
}

export function TradeBlotter({ symbol }: TradeBlotterProps) {
  const {
    data: recentTradesData,
    loading,
    error,
  } = useRecentTrades(symbol, 20);
  const [isConnected, setIsConnected] = useState(false);

  // Use only GraphQL data - no fallback to mock data
  const trades = recentTradesData?.recentTrades || [];

  useEffect(() => {
    // Set connection status based on GraphQL data availability
    if (recentTradesData?.recentTrades) {
      setIsConnected(true);
    } else if (error) {
      setIsConnected(false);
    }
  }, [recentTradesData, error]);

  // Loading state
  if (loading) {
    return (
      <div className="space-y-4">
        <div className="animate-pulse">
          <div className="h-4 bg-muted rounded mb-4"></div>
          <div className="space-y-2">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="h-6 bg-muted rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="space-y-4">
        <div className="text-center text-muted-foreground">
          <p>Error loading trade data</p>
          <p className="text-sm">{error.message}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <div
            className={`w-2 h-2 rounded-full ${
              isConnected ? "bg-success" : "bg-danger"
            }`}
          />
          <span className="text-sm text-muted-foreground">
            {isConnected ? "Live" : "Disconnected"}
          </span>
        </div>
        <span className="text-xs text-muted-foreground">
          {trades.length} trades
        </span>
      </div>

      {/* Trade Table */}
      <div className="space-y-1">
        {/* Table Header */}
        <div className="flex justify-between items-center text-xs font-semibold text-muted-foreground border-b pb-1">
          <span>Time</span>
          <span>Price</span>
          <span>Size</span>
          <span>Side</span>
        </div>

        {/* Trade Rows */}
        <div className="space-y-1 max-h-64 overflow-y-auto">
          {trades.length === 0 ? (
            <div className="text-center text-muted-foreground py-4">
              No trades available
            </div>
          ) : (
            trades.map((trade: any) => (
              <div
                key={trade.tradeId}
                className={`flex justify-between items-center py-1 px-2 text-sm rounded ${
                  trade.side === "BUY"
                    ? "bg-success/10 hover:bg-success/20"
                    : "bg-danger/10 hover:bg-danger/20"
                }`}
              >
                <span className="font-mono text-xs text-muted-foreground">
                  {formatTimestamp(trade.timestamp)}
                </span>

                <span className="font-mono font-semibold">
                  {formatCurrency(Number(trade.price))}
                </span>

                <span className="font-mono text-muted-foreground">
                  {formatNumber(Number(trade.quantity), 0)}
                </span>

                <div className="flex items-center space-x-1">
                  {trade.side === "BUY" ? (
                    <ArrowUp className="h-3 w-3 text-success" />
                  ) : (
                    <ArrowDown className="h-3 w-3 text-danger" />
                  )}
                  <span
                    className={`text-xs font-semibold ${
                      trade.side === "BUY" ? "text-success" : "text-danger"
                    }`}
                  >
                    {trade.side}
                  </span>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Trade Statistics */}
      {trades.length > 0 && (
        <div className="pt-2 border-t">
          <div className="grid grid-cols-2 gap-4 text-xs">
            <div>
              <span className="text-muted-foreground">Avg Price: </span>
              <span className="font-mono font-semibold">
                {formatCurrency(
                  trades.reduce(
                    (sum: number, trade: any) => sum + Number(trade.price),
                    0
                  ) / trades.length
                )}
              </span>
            </div>
            <div>
              <span className="text-muted-foreground">Total Volume: </span>
              <span className="font-mono font-semibold">
                {formatNumber(
                  trades.reduce(
                    (sum: number, trade: any) => sum + Number(trade.quantity),
                    0
                  ),
                  0
                )}
              </span>
            </div>
            <div>
              <span className="text-muted-foreground">Buy/Sell: </span>
              <span className="font-mono font-semibold">
                {trades.filter((t: any) => t.side === "BUY").length}/
                {trades.filter((t: any) => t.side === "SELL").length}
              </span>
            </div>
            <div>
              <span className="text-muted-foreground">Last Trade: </span>
              <span className="font-mono font-semibold">
                {formatTimestamp(trades[0]?.timestamp || "")}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
