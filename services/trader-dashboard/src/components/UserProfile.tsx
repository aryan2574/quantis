import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  useTradingStore,
  Portfolio as PortfolioType,
} from "@/stores/tradingStore";
import { useAuthStore } from "@/stores/authStore";
import {
  formatCurrency,
  formatPercentage,
  getPriceChangeColor,
  cn,
} from "@/lib/utils";
import {
  TrendingUp,
  TrendingDown,
  DollarSign,
  CreditCard,
  Settings,
  User,
  Activity,
  PieChart,
} from "lucide-react";

export function UserProfile() {
  const { portfolio, setPortfolio } = useTradingStore();
  const { user } = useAuthStore();
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [profileData, setProfileData] = useState({
    firstName: "John",
    lastName: "Trader",
    email: user?.email || "trader@quantis.com",
    phone: "+1 (555) 123-4567",
    address: "123 Wall Street, New York, NY 10005",
    riskTolerance: "Moderate",
    tradingStyle: "Day Trading",
    accountType: "Professional",
  });

  useEffect(() => {
    // Generate mock portfolio data
    const generateMockPortfolio = (): PortfolioType => {
      const positions = [
        {
          symbol: "AAPL",
          quantity: 100,
          averagePrice: 145.5,
          currentPrice: 150.25,
          unrealizedPnL: 475.0,
          realizedPnL: 250.0,
        },
        {
          symbol: "GOOGL",
          quantity: 50,
          averagePrice: 2800.0,
          currentPrice: 2850.75,
          unrealizedPnL: 2537.5,
          realizedPnL: 0,
        },
        {
          symbol: "TSLA",
          quantity: 25,
          averagePrice: 200.0,
          currentPrice: 195.5,
          unrealizedPnL: -112.5,
          realizedPnL: 500.0,
        },
        {
          symbol: "MSFT",
          quantity: 75,
          averagePrice: 300.0,
          currentPrice: 305.25,
          unrealizedPnL: 393.75,
          realizedPnL: 150.0,
        },
      ];

      const totalValue = positions.reduce(
        (sum, pos) => sum + pos.quantity * pos.currentPrice,
        0
      );
      const totalPnL = positions.reduce(
        (sum, pos) => sum + pos.unrealizedPnL + pos.realizedPnL,
        0
      );

      return {
        balance: 50000.0,
        positions,
        totalValue: totalValue + 50000.0,
        totalPnL,
      };
    };

    setTimeout(() => {
      setPortfolio(generateMockPortfolio());
      setIsLoading(false);
    }, 1000);
  }, [setPortfolio]);

  const handleSaveProfile = () => {
    // In a real app, this would save to the backend
    setIsEditing(false);
  };

  const handleInputChange = (field: string, value: string) => {
    setProfileData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  if (isLoading) {
    return (
      <div className="space-y-6">
        <div className="animate-pulse">
          <div className="h-8 bg-muted rounded mb-4"></div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="h-64 bg-muted rounded"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Profile Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold">User Profile</h2>
          <p className="text-muted-foreground">
            Manage your account settings and view portfolio details
          </p>
        </div>
        <Button
          variant={isEditing ? "default" : "outline"}
          onClick={() => setIsEditing(!isEditing)}
          className="flex items-center space-x-2"
        >
          <Settings className="h-4 w-4" />
          <span>{isEditing ? "Save Changes" : "Edit Profile"}</span>
        </Button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Personal Information */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <User className="h-5 w-5" />
              <span>Personal Information</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="firstName">First Name</Label>
                <Input
                  id="firstName"
                  value={profileData.firstName}
                  onChange={(e) =>
                    handleInputChange("firstName", e.target.value)
                  }
                  disabled={!isEditing}
                />
              </div>
              <div>
                <Label htmlFor="lastName">Last Name</Label>
                <Input
                  id="lastName"
                  value={profileData.lastName}
                  onChange={(e) =>
                    handleInputChange("lastName", e.target.value)
                  }
                  disabled={!isEditing}
                />
              </div>
            </div>

            <div>
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={profileData.email}
                onChange={(e) => handleInputChange("email", e.target.value)}
                disabled={!isEditing}
              />
            </div>

            <div>
              <Label htmlFor="phone">Phone</Label>
              <Input
                id="phone"
                value={profileData.phone}
                onChange={(e) => handleInputChange("phone", e.target.value)}
                disabled={!isEditing}
              />
            </div>

            <div>
              <Label htmlFor="address">Address</Label>
              <Input
                id="address"
                value={profileData.address}
                onChange={(e) => handleInputChange("address", e.target.value)}
                disabled={!isEditing}
              />
            </div>

            {isEditing && (
              <Button onClick={handleSaveProfile} className="w-full">
                Save Changes
              </Button>
            )}
          </CardContent>
        </Card>

        {/* Trading Preferences */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Activity className="h-5 w-5" />
              <span>Trading Preferences</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <Label htmlFor="riskTolerance">Risk Tolerance</Label>
              <Input
                id="riskTolerance"
                value={profileData.riskTolerance}
                onChange={(e) =>
                  handleInputChange("riskTolerance", e.target.value)
                }
                disabled={!isEditing}
              />
            </div>

            <div>
              <Label htmlFor="tradingStyle">Trading Style</Label>
              <Input
                id="tradingStyle"
                value={profileData.tradingStyle}
                onChange={(e) =>
                  handleInputChange("tradingStyle", e.target.value)
                }
                disabled={!isEditing}
              />
            </div>

            <div>
              <Label htmlFor="accountType">Account Type</Label>
              <Input
                id="accountType"
                value={profileData.accountType}
                onChange={(e) =>
                  handleInputChange("accountType", e.target.value)
                }
                disabled={!isEditing}
              />
            </div>

            <div className="pt-4 border-t">
              <div className="text-sm text-muted-foreground">
                <div className="flex items-center justify-between mb-2">
                  <span>Account Status</span>
                  <span className="text-success font-semibold">Active</span>
                </div>
                <div className="flex items-center justify-between mb-2">
                  <span>Member Since</span>
                  <span>Jan 2024</span>
                </div>
                <div className="flex items-center justify-between">
                  <span>Last Login</span>
                  <span>Today</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Account Summary */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <CreditCard className="h-5 w-5" />
              <span>Account Summary</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {portfolio && (
              <>
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center space-x-2">
                      <DollarSign className="h-4 w-4 text-muted-foreground" />
                      <span className="text-sm text-muted-foreground">
                        Cash Balance
                      </span>
                    </div>
                    <span className="font-mono text-sm font-semibold">
                      {formatCurrency(portfolio.balance)}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">
                      Total Value
                    </span>
                    <span className="font-mono text-lg font-bold">
                      {formatCurrency(portfolio.totalValue)}
                    </span>
                  </div>

                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">
                      Total P&L
                    </span>
                    <div className="flex items-center space-x-2">
                      {portfolio.totalPnL >= 0 ? (
                        <TrendingUp className="h-4 w-4 text-success" />
                      ) : (
                        <TrendingDown className="h-4 w-4 text-danger" />
                      )}
                      <span
                        className={cn(
                          "font-mono text-sm font-semibold",
                          getPriceChangeColor(portfolio.totalPnL)
                        )}
                      >
                        {formatCurrency(portfolio.totalPnL)}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t">
                  <div className="text-sm text-muted-foreground">
                    <div className="flex items-center justify-between mb-2">
                      <span>Active Positions</span>
                      <span className="font-semibold">
                        {portfolio.positions.length}
                      </span>
                    </div>
                    <div className="flex items-center justify-between mb-2">
                      <span>Total Trades</span>
                      <span className="font-semibold">1,247</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span>Win Rate</span>
                      <span className="font-semibold text-success">68.5%</span>
                    </div>
                  </div>
                </div>
              </>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Portfolio Overview */}
      {portfolio && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <PieChart className="h-5 w-5" />
              <span>Portfolio Overview</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {portfolio.positions.map((position) => {
                  const totalPnL =
                    position.unrealizedPnL + position.realizedPnL;
                  const pnlPercent =
                    (totalPnL / (position.quantity * position.averagePrice)) *
                    100;

                  return (
                    <div
                      key={position.symbol}
                      className="p-4 border rounded-lg space-y-2"
                    >
                      <div className="flex items-center justify-between">
                        <span className="font-semibold">{position.symbol}</span>
                        <span className="font-mono text-sm">
                          {formatCurrency(position.currentPrice)}
                        </span>
                      </div>

                      <div className="flex items-center justify-between text-sm text-muted-foreground">
                        <span>{position.quantity} shares</span>
                        <span>
                          Avg: {formatCurrency(position.averagePrice)}
                        </span>
                      </div>

                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">
                          P&L
                        </span>
                        <div className="flex items-center space-x-1">
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
                            {formatCurrency(totalPnL)} (
                            {formatPercentage(pnlPercent)})
                          </span>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
