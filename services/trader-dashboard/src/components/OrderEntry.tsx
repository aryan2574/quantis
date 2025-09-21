import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { usePlaceOrder } from "@/lib/graphql/hooks";
import { useMarketData } from "@/lib/graphql/hooks";
import { formatCurrency } from "@/lib/utils";
import { useToast } from "@/hooks/use-toast";
import { ArrowUp, ArrowDown } from "lucide-react";
import { useAuthStore } from "@/stores/authStore";

interface OrderEntryProps {
  symbol: string;
}

export function OrderEntry({ symbol }: OrderEntryProps) {
  const [side, setSide] = useState<"BUY" | "SELL">("BUY");
  const [quantity, setQuantity] = useState("");
  const [price, setPrice] = useState("");
  const [orderType, setOrderType] = useState<"MARKET" | "LIMIT">("LIMIT");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { user } = useAuthStore();
  const [placeOrderMutation] = usePlaceOrder();
  const { data: marketDataQuery } = useMarketData(symbol);
  const { toast } = useToast();

  // Use GraphQL market data if available, otherwise fallback to default price
  const currentPrice = marketDataQuery?.marketData?.lastPrice
    ? Number(marketDataQuery.marketData.lastPrice)
    : 150.0;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!quantity || (orderType === "LIMIT" && !price)) {
      toast({
        title: "Invalid Order",
        description: "Please fill in all required fields",
        variant: "destructive",
      });
      return;
    }

    setIsSubmitting(true);

    try {
      // Use GraphQL mutation to place order
      const result = await placeOrderMutation({
        variables: {
          input: {
            userId: user?.id || "demo-user",
            symbol,
            side,
            quantity: parseFloat(quantity),
            price: orderType === "MARKET" ? undefined : parseFloat(price),
            orderType,
            timeInForce: "GTC", // Good Till Cancelled
          },
        },
      });

      if (result.data?.placeOrder?.success) {
        toast({
          title: "Order Placed",
          description: `${side} ${quantity} ${symbol} at ${formatCurrency(
            orderType === "MARKET" ? currentPrice : parseFloat(price)
          )}`,
        });

        // Reset form
        setQuantity("");
        setPrice("");
      } else {
        const errors = result.data?.placeOrder?.errors || ["Unknown error"];
        toast({
          title: "Order Failed",
          description: errors.join(", "),
          variant: "destructive",
        });
      }
    } catch (error: any) {
      console.error("Order placement error:", error);
      toast({
        title: "Error",
        description: error.message || "Failed to place order",
        variant: "destructive",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleQuickPrice = (multiplier: number) => {
    const newPrice = (currentPrice * multiplier).toFixed(2);
    setPrice(newPrice);
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Symbol Display */}
      <div className="text-center">
        <span className="text-lg font-semibold">{symbol}</span>
        <div className="text-sm text-muted-foreground">
          Current: {formatCurrency(currentPrice)}
        </div>
      </div>

      {/* Side Selection */}
      <div className="space-y-2">
        <Label>Side</Label>
        <div className="flex space-x-2">
          <Button
            type="button"
            variant={side === "BUY" ? "success" : "outline"}
            className="flex-1"
            onClick={() => setSide("BUY")}
          >
            <ArrowUp className="h-4 w-4 mr-2" />
            BUY
          </Button>
          <Button
            type="button"
            variant={side === "SELL" ? "danger" : "outline"}
            className="flex-1"
            onClick={() => setSide("SELL")}
          >
            <ArrowDown className="h-4 w-4 mr-2" />
            SELL
          </Button>
        </div>
      </div>

      {/* Order Type */}
      <div className="space-y-2">
        <Label>Order Type</Label>
        <div className="flex space-x-2">
          <Button
            type="button"
            variant={orderType === "LIMIT" ? "default" : "outline"}
            className="flex-1"
            onClick={() => setOrderType("LIMIT")}
          >
            LIMIT
          </Button>
          <Button
            type="button"
            variant={orderType === "MARKET" ? "default" : "outline"}
            className="flex-1"
            onClick={() => setOrderType("MARKET")}
          >
            MARKET
          </Button>
        </div>
      </div>

      {/* Quantity */}
      <div className="space-y-2">
        <Label htmlFor="quantity">Quantity</Label>
        <Input
          id="quantity"
          type="number"
          placeholder="Enter quantity"
          value={quantity}
          onChange={(e) => setQuantity(e.target.value)}
          min="1"
          step="1"
          required
        />
      </div>

      {/* Price (only for LIMIT orders) */}
      {orderType === "LIMIT" && (
        <div className="space-y-2">
          <Label htmlFor="price">Price</Label>
          <Input
            id="price"
            type="number"
            placeholder="Enter price"
            value={price}
            onChange={(e) => setPrice(e.target.value)}
            min="0"
            step="0.01"
            required
          />

          {/* Quick Price Buttons */}
          <div className="flex space-x-1">
            <Button
              type="button"
              variant="outline"
              size="sm"
              className="flex-1"
              onClick={() => handleQuickPrice(0.99)}
            >
              -1%
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              className="flex-1"
              onClick={() => handleQuickPrice(1.01)}
            >
              +1%
            </Button>
            <Button
              type="button"
              variant="outline"
              size="sm"
              className="flex-1"
              onClick={() => setPrice(currentPrice.toFixed(2))}
            >
              Market
            </Button>
          </div>
        </div>
      )}

      {/* Order Summary */}
      <div className="p-3 bg-muted rounded-lg space-y-1">
        <div className="text-sm font-semibold">Order Summary</div>
        <div className="text-sm text-muted-foreground">
          {side} {quantity || "0"} {symbol}
        </div>
        {orderType === "LIMIT" && price && (
          <div className="text-sm text-muted-foreground">
            @ {formatCurrency(parseFloat(price))}
          </div>
        )}
        {orderType === "MARKET" && (
          <div className="text-sm text-muted-foreground">
            @ Market Price (~{formatCurrency(currentPrice)})
          </div>
        )}
        {quantity && (
          <div className="text-sm font-semibold">
            Total:{" "}
            {formatCurrency(
              (parseFloat(quantity) || 0) *
                (orderType === "MARKET" ? currentPrice : parseFloat(price) || 0)
            )}
          </div>
        )}
      </div>

      {/* Submit Button */}
      <Button
        type="submit"
        className="w-full"
        variant={side === "BUY" ? "success" : "danger"}
        disabled={
          isSubmitting || !quantity || (orderType === "LIMIT" && !price)
        }
      >
        {isSubmitting ? "Placing Order..." : `Place ${side} Order`}
      </Button>
    </form>
  );
}
