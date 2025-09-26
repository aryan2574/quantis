import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { OrderBook } from "@/components/OrderBook";
import { Portfolio } from "@/components/Portfolio";
import { MarketData } from "@/components/MarketData";
import { OrderEntry } from "@/components/OrderEntry";
import { TradeBlotter } from "@/components/TradeBlotter";
// import { AIRecommendations } from "@/components/AIRecommendations";
import { UserProfile } from "@/components/UserProfile";
import { MLAnalytics } from "@/components/MLAnalytics";
import { useTradingStore } from "@/stores/tradingStore";
import { websocketService } from "@/lib/websocket";
import { useAuthStore } from "@/stores/authStore";
import { User, TrendingUp, Brain, LogOut } from "lucide-react";

type DashboardSection = "profile" | "trading" | "analytics";

export function TradingDashboard() {
  const { selectedSymbol } = useTradingStore();
  const { token, user, logout } = useAuthStore();
  const [activeSection, setActiveSection] =
    useState<DashboardSection>("trading");

  useEffect(() => {
    // Initialize WebSocket connection
    websocketService.connect(token || undefined);

    // Subscribe to real-time data for selected symbol
    const handleOrderBookUpdate = (data: any) => {
      console.log("Order book update:", data);
    };

    const handleTradeUpdate = (data: any) => {
      console.log("Trade update:", data);
    };

    const handleMarketDataUpdate = (data: any) => {
      console.log("Market data update:", data);
    };

    websocketService.subscribeToOrderBook(
      selectedSymbol,
      handleOrderBookUpdate
    );
    websocketService.subscribeToTrades(selectedSymbol, handleTradeUpdate);
    websocketService.subscribeToMarketData(
      selectedSymbol,
      handleMarketDataUpdate
    );

    return () => {
      websocketService.unsubscribe("orderbook", selectedSymbol);
      websocketService.unsubscribe("trades", selectedSymbol);
      websocketService.unsubscribe("marketdata", selectedSymbol);
    };
  }, [selectedSymbol, token]);

  const renderSection = () => {
    switch (activeSection) {
      case "profile":
        return <UserProfile />;
      case "trading":
        return (
          <div className="trading-grid">
            {/* Order Book */}
            <div className="order-book">
              <Card>
                <CardHeader>
                  <CardTitle>Order Book - {selectedSymbol}</CardTitle>
                </CardHeader>
                <CardContent>
                  <OrderBook symbol={selectedSymbol} />
                </CardContent>
              </Card>
            </div>

            {/* Portfolio */}
            <div className="portfolio">
              <Card>
                <CardHeader>
                  <CardTitle>Portfolio</CardTitle>
                </CardHeader>
                <CardContent>
                  <Portfolio />
                </CardContent>
              </Card>
            </div>

            {/* Market Data */}
            <div className="market-data">
              <Card>
                <CardHeader>
                  <CardTitle>Market Data</CardTitle>
                </CardHeader>
                <CardContent>
                  <MarketData symbol={selectedSymbol} />
                </CardContent>
              </Card>
            </div>

            {/* Order Entry */}
            <div className="order-entry">
              <Card>
                <CardHeader>
                  <CardTitle>Order Entry</CardTitle>
                </CardHeader>
                <CardContent>
                  <OrderEntry symbol={selectedSymbol} />
                </CardContent>
              </Card>
            </div>

            {/* Trade Blotter */}
            <div className="trade-blotter">
              <Card>
                <CardHeader>
                  <CardTitle>Recent Trades</CardTitle>
                </CardHeader>
                <CardContent>
                  <TradeBlotter symbol={selectedSymbol} />
                </CardContent>
              </Card>
            </div>
          </div>
        );
      case "analytics":
        return <MLAnalytics />;
      default:
        return null;
    }
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b bg-card">
        <div className="flex items-center justify-between px-6 py-4">
          <div className="flex items-center space-x-4">
            <h1 className="text-2xl font-bold text-primary">Quantis Trading</h1>
            <div className="flex space-x-1">
              <Button
                variant={activeSection === "profile" ? "default" : "ghost"}
                size="sm"
                onClick={() => setActiveSection("profile")}
                className="flex items-center space-x-2"
              >
                <User className="h-4 w-4" />
                <span>Profile</span>
              </Button>
              <Button
                variant={activeSection === "trading" ? "default" : "ghost"}
                size="sm"
                onClick={() => setActiveSection("trading")}
                className="flex items-center space-x-2"
              >
                <TrendingUp className="h-4 w-4" />
                <span>Trading</span>
              </Button>
              <Button
                variant={activeSection === "analytics" ? "default" : "ghost"}
                size="sm"
                onClick={() => setActiveSection("analytics")}
                className="flex items-center space-x-2"
              >
                <Brain className="h-4 w-4" />
                <span>Analytics</span>
              </Button>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            <div className="text-sm text-muted-foreground">
              Welcome, <span className="font-semibold">{user?.username}</span>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={logout}
              className="flex items-center space-x-2"
            >
              <LogOut className="h-4 w-4" />
              <span>Logout</span>
            </Button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="p-6">{renderSection()}</main>
    </div>
  );
}
