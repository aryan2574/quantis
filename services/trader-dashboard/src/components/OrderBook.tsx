import { useEffect, useState } from "react";
import { useOrderBook } from "@/lib/graphql/hooks";
import { formatNumber, formatCurrency } from "@/lib/utils";
import { cn } from "@/lib/utils";

interface OrderBookProps {
  symbol: string;
}

interface OrderBookLevel {
  price: number;
  quantity: number;
  orderCount: number;
  totalValue: number;
}

export function OrderBook({ symbol }: OrderBookProps) {
  const {
    data: orderBookData,
    loading,
    error,
    refetch,
  } = useOrderBook(symbol, 10);
  const [isConnected, setIsConnected] = useState(false);

  // Use only GraphQL data - no fallback to mock data
  const orderBook = orderBookData?.orderBook;

  useEffect(() => {
    // Set connection status based on GraphQL data availability
    if (orderBookData?.orderBook) {
      setIsConnected(true);
    } else if (error) {
      setIsConnected(false);
    }
  }, [orderBookData, error]);

  const OrderBookRow = ({
    level,
    isBid,
  }: {
    level: OrderBookLevel;
    isBid: boolean;
  }) => (
    <div className={cn("order-book-row", isBid ? "bid-row" : "ask-row")}>
      <span
        className={cn(
          "font-mono text-sm",
          isBid ? "text-success" : "text-danger"
        )}
      >
        {formatCurrency(level.price)}
      </span>
      <span className="font-mono text-sm text-muted-foreground">
        {formatNumber(level.quantity, 0)}
      </span>
      <span className="font-mono text-xs text-muted-foreground">
        {level.orderCount}
      </span>
    </div>
  );

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
          <p>Error loading order book data</p>
          <p className="text-sm">{error.message}</p>
          <button
            onClick={() => refetch()}
            className="mt-2 px-3 py-1 bg-primary text-primary-foreground rounded text-sm"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  // No data state
  if (!orderBook) {
    return (
      <div className="space-y-4">
        <div className="text-center text-muted-foreground">
          <p>No order book data available</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Connection Status */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <div
            className={cn(
              "w-2 h-2 rounded-full",
              isConnected ? "bg-success" : "bg-danger"
            )}
          />
          <span className="text-sm text-muted-foreground">
            {isConnected ? "Live" : "Disconnected"}
          </span>
        </div>
        <span className="text-xs text-muted-foreground">
          Last update: {new Date(orderBook.timestamp).toLocaleTimeString()}
        </span>
      </div>

      {/* Order Book Table */}
      <div className="space-y-2">
        {/* Header */}
        <div className="flex justify-between items-center text-xs font-semibold text-muted-foreground border-b pb-1">
          <span>Price</span>
          <span>Size</span>
          <span>Orders</span>
        </div>

        {/* Asks (Sell Orders) */}
        <div className="space-y-1">
          {orderBook.asks?.map((ask: any, index: number) => (
            <OrderBookRow key={`ask-${index}`} level={ask} isBid={false} />
          ))}
        </div>

        {/* Spread */}
        {orderBook.bids?.length > 0 && orderBook.asks?.length > 0 && (
          <div className="flex justify-between items-center py-2 border-y">
            <span className="text-sm font-semibold">
              Spread:{" "}
              {formatCurrency(
                Number(orderBook.asks[0].price) -
                  Number(orderBook.bids[0].price)
              )}
            </span>
            <span className="text-sm text-muted-foreground">
              {formatNumber(
                ((Number(orderBook.asks[0].price) -
                  Number(orderBook.bids[0].price)) /
                  Number(orderBook.bids[0].price)) *
                  100,
                3
              )}
              %
            </span>
          </div>
        )}

        {/* Bids (Buy Orders) */}
        <div className="space-y-1">
          {orderBook.bids?.map((bid: any, index: number) => (
            <OrderBookRow key={`bid-${index}`} level={bid} isBid={true} />
          ))}
        </div>
      </div>
    </div>
  );
}
