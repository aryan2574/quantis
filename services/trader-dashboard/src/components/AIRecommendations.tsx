import { useEffect, useState } from "react";
import { useTradingStore } from "@/stores/tradingStore";
import { formatTimestamp } from "@/lib/utils";
import { AlertTriangle, TrendingUp, Info, Zap } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";

export function AIRecommendations() {
  const { recommendations, addRecommendation } = useTradingStore();
  const [isGenerating, setIsGenerating] = useState(false);

  useEffect(() => {
    // Generate mock AI recommendations
    const generateMockRecommendation = () => {
      // const types = ["ALERT", "RECOMMENDATION", "ANOMALY"] as const;
      // const severities = ["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const;
      const symbols = ["AAPL", "GOOGL", "TSLA", "MSFT", "AMZN"];

      const recommendations = [
        {
          type: "ALERT" as const,
          title: "High Volume Alert",
          message: `Unusual trading volume detected in ${
            symbols[Math.floor(Math.random() * symbols.length)]
          }. Volume is 3.2x above average.`,
          severity: "HIGH" as const,
          symbol: symbols[Math.floor(Math.random() * symbols.length)],
        },
        {
          type: "RECOMMENDATION" as const,
          title: "Price Momentum Signal",
          message: `Strong bullish momentum detected. Consider taking a long position with tight stop-loss.`,
          severity: "MEDIUM" as const,
          symbol: symbols[Math.floor(Math.random() * symbols.length)],
        },
        {
          type: "ANOMALY" as const,
          title: "Market Anomaly Detected",
          message: `Price action deviates significantly from historical patterns. Potential reversal signal.`,
          severity: "CRITICAL" as const,
          symbol: symbols[Math.floor(Math.random() * symbols.length)],
        },
        {
          type: "ALERT" as const,
          title: "Spread Widening",
          message: `Bid-ask spread has widened by 15% in the last 5 minutes. Reduced liquidity detected.`,
          severity: "MEDIUM" as const,
          symbol: symbols[Math.floor(Math.random() * symbols.length)],
        },
        {
          type: "RECOMMENDATION" as const,
          title: "Support Level Test",
          message: `Price approaching key support level. Watch for bounce or breakdown.`,
          severity: "LOW" as const,
          symbol: symbols[Math.floor(Math.random() * symbols.length)],
        },
      ];

      const randomRec =
        recommendations[Math.floor(Math.random() * recommendations.length)];
      addRecommendation(randomRec);
    };

    // Generate initial recommendations
    for (let i = 0; i < 3; i++) {
      generateMockRecommendation();
    }

    // Generate new recommendations periodically
    const interval = setInterval(() => {
      setIsGenerating(true);
      generateMockRecommendation();
      setTimeout(() => setIsGenerating(false), 1000);
    }, 15000 + Math.random() * 10000); // Random interval between 15-25 seconds

    return () => clearInterval(interval);
  }, [addRecommendation]);

  const getIcon = (type: string) => {
    switch (type) {
      case "ALERT":
        return <AlertTriangle className="h-4 w-4" />;
      case "RECOMMENDATION":
        return <TrendingUp className="h-4 w-4" />;
      case "ANOMALY":
        return <Zap className="h-4 w-4" />;
      default:
        return <Info className="h-4 w-4" />;
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case "CRITICAL":
        return "text-danger border-danger bg-danger/10";
      case "HIGH":
        return "text-warning border-warning bg-warning/10";
      case "MEDIUM":
        return "text-info border-info bg-info/10";
      case "LOW":
        return "text-muted-foreground border-muted bg-muted/10";
      default:
        return "text-muted-foreground border-muted bg-muted/10";
    }
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <div
            className={`w-2 h-2 rounded-full ${
              isGenerating ? "bg-warning animate-pulse" : "bg-success"
            }`}
          />
          <span className="text-sm text-muted-foreground">
            {isGenerating ? "Analyzing..." : "AI Active"}
          </span>
        </div>
        <span className="text-xs text-muted-foreground">
          {recommendations.length} insights
        </span>
      </div>

      {/* Recommendations List */}
      <div className="space-y-3 max-h-96 overflow-y-auto">
        {recommendations.length === 0 ? (
          <div className="text-center text-muted-foreground py-4">
            <Info className="h-8 w-8 mx-auto mb-2 opacity-50" />
            <p>No recommendations available</p>
            <p className="text-xs">AI is analyzing market data...</p>
          </div>
        ) : (
          recommendations.map((rec) => (
            <Card
              key={rec.id}
              className={`border-l-4 ${getSeverityColor(rec.severity)}`}
            >
              <CardContent className="p-3">
                <div className="flex items-start space-x-3">
                  <div
                    className={`mt-0.5 ${
                      getSeverityColor(rec.severity).split(" ")[0]
                    }`}
                  >
                    {getIcon(rec.type)}
                  </div>

                  <div className="flex-1 space-y-1">
                    <div className="flex items-center justify-between">
                      <h4 className="text-sm font-semibold">{rec.title}</h4>
                      <span className="text-xs text-muted-foreground">
                        {formatTimestamp(rec.timestamp)}
                      </span>
                    </div>

                    {rec.symbol && (
                      <div className="text-xs font-mono text-muted-foreground">
                        {rec.symbol}
                      </div>
                    )}

                    <p className="text-sm text-muted-foreground">
                      {rec.message}
                    </p>

                    <div className="flex items-center justify-between">
                      <span
                        className={`text-xs font-semibold px-2 py-1 rounded ${
                          rec.severity === "CRITICAL"
                            ? "bg-danger text-danger-foreground"
                            : rec.severity === "HIGH"
                            ? "bg-warning text-warning-foreground"
                            : rec.severity === "MEDIUM"
                            ? "bg-info text-info-foreground"
                            : "bg-muted text-muted-foreground"
                        }`}
                      >
                        {rec.severity}
                      </span>

                      <span className="text-xs text-muted-foreground">
                        {rec.type}
                      </span>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      {/* AI Status */}
      <div className="pt-2 border-t">
        <div className="text-xs text-muted-foreground">
          <div className="flex items-center justify-between">
            <span>AI Engine Status</span>
            <span className="text-success">Active</span>
          </div>
          <div className="flex items-center justify-between">
            <span>Analysis Coverage</span>
            <span>95.2%</span>
          </div>
          <div className="flex items-center justify-between">
            <span>Last Update</span>
            <span>{formatTimestamp(new Date().toISOString())}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
