import { useEffect, useState } from "react";
import { usePortfolio, usePortfolioUpdates } from "@/lib/graphql/hooks";
import {
  formatCurrency,
  formatNumber,
  formatPercentage,
  getPriceChangeColor,
  cn,
} from "@/lib/utils";
import { TrendingUp, TrendingDown, DollarSign } from "lucide-react";

export function Portfolio() {
  const { data: portfolioData, loading, error, refetch } = usePortfolio();
  const { data: portfolioUpdate } = usePortfolioUpdates();
  const [isLoading, setIsLoading] = useState(true);

  // Update local state when GraphQL data changes
  useEffect(() => {
    if (portfolioData?.portfolio) {
      setIsLoading(false);
    }
  }, [portfolioData]);

  // Handle real-time portfolio updates
  useEffect(() => {
    if (portfolioUpdate?.portfolioUpdates) {
      // Refetch portfolio data when updates are received
      refetch();
    }
  }, [portfolioUpdate, refetch]);

  // Fallback to mock data if GraphQL is not available
  useEffect(() => {
    if (error && !portfolioData) {
      console.warn("GraphQL Portfolio data unavailable, using mock data");
      // You could set mock data here as fallback
      setIsLoading(false);
    }
  }, [error, portfolioData]);

  if (loading || isLoading) {
    return (
      <div className="space-y-4">
        <div className="animate-pulse">
          <div className="h-8 bg-muted rounded mb-4"></div>
          <div className="space-y-2">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-16 bg-muted rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center text-muted-foreground">
        <p>Error loading portfolio data</p>
        <p className="text-sm">{error.message}</p>
      </div>
    );
  }

  const portfolio = portfolioData?.portfolio;
  if (!portfolio) {
    return (
      <div className="text-center text-muted-foreground">
        <p>No portfolio data available</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Portfolio Summary */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <DollarSign className="h-4 w-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Cash Balance</span>
          </div>
          <span className="font-mono text-sm font-semibold">
            {formatCurrency(Number(portfolio.cashBalance))}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">Total Value</span>
          <span className="font-mono text-lg font-bold">
            {formatCurrency(Number(portfolio.totalValue))}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <span className="text-sm text-muted-foreground">Unrealized P&L</span>
          <div className="flex items-center space-x-2">
            {Number(portfolio.unrealizedPnl) >= 0 ? (
              <TrendingUp className="h-4 w-4 text-success" />
            ) : (
              <TrendingDown className="h-4 w-4 text-danger" />
            )}
            <span
              className={cn(
                "font-mono text-sm font-semibold",
                getPriceChangeColor(Number(portfolio.unrealizedPnl))
              )}
            >
              {formatCurrency(Number(portfolio.unrealizedPnl))}
            </span>
          </div>
        </div>
      </div>

      {/* Positions */}
      <div className="space-y-2">
        <h4 className="text-sm font-semibold text-muted-foreground">
          Positions
        </h4>
        <div className="space-y-2">
          {portfolio.positions?.map((position: any) => {
            const totalPnL =
              Number(position.unrealizedPnl) + Number(position.realizedPnl);
            const pnlPercent =
              (totalPnL /
                (Number(position.quantity) * Number(position.averagePrice))) *
              100;

            return (
              <div
                key={position.symbol}
                className="p-3 border rounded-lg space-y-2"
              >
                <div className="flex items-center justify-between">
                  <span className="font-semibold">{position.symbol}</span>
                  <span className="font-mono text-sm">
                    {formatCurrency(Number(position.currentPrice))}
                  </span>
                </div>

                <div className="flex items-center justify-between text-sm text-muted-foreground">
                  <span>{formatNumber(Number(position.quantity))} shares</span>
                  <span>
                    Avg: {formatCurrency(Number(position.averagePrice))}
                  </span>
                </div>

                <div className="flex items-center justify-between">
                  <span className="text-sm text-muted-foreground">P&L</span>
                  <div className="flex items-center space-x-2">
                    {totalPnL >= 0 ? (
                      <TrendingUp className="h-3 w-3 text-success" />
                    ) : (
                      <TrendingDown className="h-3 w-3 text-danger" />
                    )}
                    <span
                      className={cn(
                        "font-mono text-sm font-semibold",
                        getPriceChangeColor(totalPnL)
                      )}
                    >
                      {formatCurrency(totalPnL)} ({formatPercentage(pnlPercent)}
                      )
                    </span>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
