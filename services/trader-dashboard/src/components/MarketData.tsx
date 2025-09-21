import { useEffect, useState } from "react";
import { useMarketData } from "@/lib/graphql/hooks";
import {
  formatCurrency,
  formatNumber,
  formatPercentage,
  getPriceChangeColor,
  cn,
} from "@/lib/utils";
import { TrendingUp, TrendingDown, BarChart3, Volume2 } from "lucide-react";

interface MarketDataProps {
  symbol: string;
}

export function MarketData({ symbol }: MarketDataProps) {
  const { data: marketDataQuery, loading, error } = useMarketData(symbol);
  const [isLoading, setIsLoading] = useState(true);

  // Use only GraphQL data - no fallback to mock data
  const data = marketDataQuery?.marketData;

  useEffect(() => {
    // Set loading state based on GraphQL data availability
    if (marketDataQuery?.marketData) {
      setIsLoading(false);
    } else if (error) {
      setIsLoading(false);
    }
  }, [marketDataQuery, error]);

  // Loading state
  if (loading || isLoading) {
    return (
      <div className="space-y-4">
        <div className="animate-pulse">
          <div className="h-8 bg-muted rounded mb-4"></div>
          <div className="space-y-2">
            {[1, 2, 3, 4].map((i) => (
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
          <p>Error loading market data</p>
          <p className="text-sm">{error.message}</p>
        </div>
      </div>
    );
  }

  // No data state
  if (!data) {
    return (
      <div className="space-y-4">
        <div className="text-center text-muted-foreground">
          <p>No market data available</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Price and Change */}
      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">Price</span>
          <span className="font-mono text-2xl font-bold">
            {formatCurrency(Number(data.lastPrice))}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">Change</span>
          <div className="flex items-center space-x-2">
            {Number(data.change) >= 0 ? (
              <TrendingUp className="h-4 w-4 text-success" />
            ) : (
              <TrendingDown className="h-4 w-4 text-danger" />
            )}
            <span
              className={cn(
                "font-mono text-sm font-semibold",
                getPriceChangeColor(Number(data.change))
              )}
            >
              {formatCurrency(Number(data.change))} (
              {formatPercentage(Number(data.changePercent))})
            </span>
          </div>
        </div>
      </div>

      {/* Market Stats */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">High</span>
          </div>
          <span className="font-mono text-sm font-semibold">
            {formatCurrency(Number(data.high24h))}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Low</span>
          </div>
          <span className="font-mono text-sm font-semibold">
            {formatCurrency(Number(data.low24h))}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Open</span>
          </div>
          <span className="font-mono text-sm font-semibold">
            {formatCurrency(Number(data.open24h))}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <Volume2 className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Volume</span>
          </div>
          <span className="font-mono text-sm font-semibold">
            {formatNumber(Number(data.volume), 0)}
          </span>
        </div>
      </div>

      {/* Last Update */}
      <div className="pt-2 border-t">
        <div className="flex items-center justify-between">
          <span className="text-xs text-muted-foreground">Last Update</span>
          <span className="text-xs text-muted-foreground">
            {new Date(data.timestamp).toLocaleTimeString()}
          </span>
        </div>
      </div>
    </div>
  );
}
