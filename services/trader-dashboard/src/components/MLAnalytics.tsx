import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { useTradingStore } from "@/stores/tradingStore";
import { formatTimestamp, formatCurrency } from "@/lib/utils";
import {
  useWasmAnalytics,
  useRealTimeAnalytics,
} from "@/lib/wasm/useWasmAnalytics";
import {
  Brain,
  TrendingUp,
  AlertTriangle,
  Zap,
  BarChart3,
  Target,
  Lightbulb,
  Cpu,
  RefreshCw,
  Shield,
  CheckCircle,
  XCircle,
  Loader2,
} from "lucide-react";

interface MLModel {
  id: string;
  name: string;
  type:
    | "Sentiment Analysis"
    | "Price Prediction"
    | "Risk Assessment"
    | "Pattern Recognition";
  status: "Active" | "Training" | "Inactive" | "Error";
  accuracy: number;
  lastUpdate: string;
  description: string;
}

interface AnalyticsData {
  sentimentScore: number;
  pricePrediction: {
    symbol: string;
    currentPrice: number;
    predictedPrice: number;
    confidence: number;
    timeframe: string;
  };
  riskMetrics: {
    portfolioRisk: number;
    marketRisk: number;
    liquidityRisk: number;
    overallRisk: "Low" | "Medium" | "High";
  };
  patterns: {
    bullish: number;
    bearish: number;
    neutral: number;
  };
}

export function MLAnalytics() {
  const { recommendations, addRecommendation } = useTradingStore();
  const [isGenerating, setIsGenerating] = useState(false);
  const [activeModels, setActiveModels] = useState<MLModel[]>([]);
  const [analyticsData, setAnalyticsData] = useState<AnalyticsData | null>(
    null
  );
  const [selectedModel, setSelectedModel] = useState<string | null>(null);

  // WebAssembly analytics integration
  const wasmAnalytics = useWasmAnalytics();

  // Mock data for demonstration (in real app, this would come from GraphQL)
  const mockReturns = Array.from(
    { length: 100 },
    () => (Math.random() - 0.5) * 0.1
  );
  const mockPortfolioValues = Array.from(
    { length: 100 },
    (_, i) => 100000 + i * 100 + Math.random() * 1000
  );
  const mockHistoricalPrices = Array.from(
    { length: 50 },
    (_, i) => 150 + Math.sin(i * 0.1) * 10 + Math.random() * 5
  );

  const realTimeAnalytics = useRealTimeAnalytics(
    mockReturns,
    mockPortfolioValues,
    mockHistoricalPrices,
    100000,
    10000 // Update every 10 seconds
  );

  useEffect(() => {
    // Initialize ML models
    const models: MLModel[] = [
      {
        id: "sentiment-1",
        name: "News Sentiment Analyzer",
        type: "Sentiment Analysis",
        status: "Active",
        accuracy: 87.3,
        lastUpdate: new Date().toISOString(),
        description:
          "Analyzes news sentiment and social media to predict market movements",
      },
      {
        id: "price-1",
        name: "Price Prediction Model",
        type: "Price Prediction",
        status: "Active",
        accuracy: 82.1,
        lastUpdate: new Date().toISOString(),
        description:
          "Uses LSTM neural networks to predict short-term price movements",
      },
      {
        id: "risk-1",
        name: "Risk Assessment Engine",
        type: "Risk Assessment",
        status: "Active",
        accuracy: 91.7,
        lastUpdate: new Date().toISOString(),
        description: "Evaluates portfolio risk using Monte Carlo simulations",
      },
      {
        id: "pattern-1",
        name: "Pattern Recognition AI",
        type: "Pattern Recognition",
        status: "Training",
        accuracy: 76.8,
        lastUpdate: new Date().toISOString(),
        description: "Identifies trading patterns using computer vision and ML",
      },
    ];
    setActiveModels(models);

    // Generate initial analytics data
    generateAnalyticsData();
  }, []);

  const generateAnalyticsData = () => {
    const data: AnalyticsData = {
      sentimentScore: (Math.random() - 0.5) * 2, // -1 to 1
      pricePrediction: {
        symbol: "AAPL",
        currentPrice: 150.25,
        predictedPrice: 150.25 + (Math.random() - 0.5) * 10,
        confidence: Math.random() * 20 + 70, // 70-90%
        timeframe: "24h",
      },
      riskMetrics: {
        portfolioRisk: Math.random() * 30 + 20, // 20-50%
        marketRisk: Math.random() * 40 + 30, // 30-70%
        liquidityRisk: Math.random() * 20 + 10, // 10-30%
        overallRisk: Math.random() > 0.5 ? "Medium" : "Low",
      },
      patterns: {
        bullish: Math.random() * 40 + 30, // 30-70%
        bearish: Math.random() * 30 + 10, // 10-40%
        neutral: Math.random() * 20 + 10, // 10-30%
      },
    };
    setAnalyticsData(data);
  };

  const generateMLRecommendation = () => {
    setIsGenerating(true);

    const recommendations = [
      {
        type: "ALERT" as const,
        title: "ML Model Alert",
        message:
          "Sentiment analysis indicates strong bullish momentum. Consider increasing position size.",
        severity: "HIGH" as const,
        symbol: "AAPL",
      },
      {
        type: "RECOMMENDATION" as const,
        title: "AI Trading Signal",
        message:
          "Pattern recognition detected a head and shoulders formation. Potential reversal signal.",
        severity: "MEDIUM" as const,
        symbol: "TSLA",
      },
      {
        type: "ANOMALY" as const,
        title: "Risk Model Alert",
        message:
          "Portfolio risk exceeds recommended threshold. Consider reducing position sizes.",
        severity: "CRITICAL" as const,
        symbol: "PORTFOLIO",
      },
    ];

    const randomRec =
      recommendations[Math.floor(Math.random() * recommendations.length)];
    addRecommendation(randomRec);

    setTimeout(() => setIsGenerating(false), 2000);
  };

  const getSentimentColor = (score: number) => {
    if (score > 0.3) return "text-success";
    if (score < -0.3) return "text-danger";
    return "text-muted-foreground";
  };

  const getSentimentLabel = (score: number) => {
    if (score > 0.3) return "Bullish";
    if (score < -0.3) return "Bearish";
    return "Neutral";
  };

  const getRiskColor = (risk: string) => {
    switch (risk) {
      case "Low":
        return "text-success";
      case "Medium":
        return "text-warning";
      case "High":
        return "text-danger";
      default:
        return "text-muted-foreground";
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-3xl font-bold">ML Analytics & AI Insights</h2>
          <p className="text-muted-foreground">
            Advanced machine learning models and real-time market analysis
          </p>
        </div>
        <div className="flex space-x-2">
          <Button
            variant="outline"
            onClick={generateAnalyticsData}
            className="flex items-center space-x-2"
          >
            <RefreshCw className="h-4 w-4" />
            <span>Refresh Data</span>
          </Button>
          <Button
            onClick={generateMLRecommendation}
            disabled={isGenerating}
            className="flex items-center space-x-2"
          >
            {isGenerating ? (
              <RefreshCw className="h-4 w-4 animate-spin" />
            ) : (
              <Brain className="h-4 w-4" />
            )}
            <span>{isGenerating ? "Generating..." : "Generate Insights"}</span>
          </Button>
        </div>
      </div>

      {/* WebAssembly Status */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Cpu className="h-5 w-5" />
            <span>WebAssembly Analytics Engine</span>
            {wasmAnalytics.isLoading ? (
              <Loader2 className="h-4 w-4 animate-spin text-blue-500" />
            ) : wasmAnalytics.isLoaded ? (
              <CheckCircle className="h-4 w-4 text-green-500" />
            ) : (
              <XCircle className="h-4 w-4 text-red-500" />
            )}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="space-y-2">
              <div className="flex items-center space-x-2">
                <span className="text-sm font-medium">Status:</span>
                <span
                  className={`text-sm px-2 py-1 rounded ${
                    wasmAnalytics.isLoaded
                      ? "bg-green-100 text-green-800"
                      : wasmAnalytics.isLoading
                      ? "bg-blue-100 text-blue-800"
                      : "bg-red-100 text-red-800"
                  }`}
                >
                  {wasmAnalytics.isLoaded
                    ? "Loaded"
                    : wasmAnalytics.isLoading
                    ? "Loading..."
                    : "Failed"}
                </span>
              </div>
              {wasmAnalytics.loadTime > 0 && (
                <div className="text-xs text-muted-foreground">
                  Load time: {wasmAnalytics.loadTime.toFixed(2)}ms
                </div>
              )}
            </div>

            <div className="space-y-2">
              <div className="text-sm font-medium">Real-time Metrics:</div>
              {realTimeAnalytics.metrics && (
                <div className="text-xs text-muted-foreground">
                  Sharpe: {realTimeAnalytics.metrics.sharpeRatio.toFixed(3)} |
                  Risk: {realTimeAnalytics.metrics.riskScore.toFixed(1)}%
                </div>
              )}
            </div>

            <div className="space-y-2">
              <div className="text-sm font-medium">Price Prediction:</div>
              {realTimeAnalytics.prediction && (
                <div className="text-xs text-muted-foreground">
                  {formatCurrency(realTimeAnalytics.prediction.predictedPrice)}(
                  {realTimeAnalytics.prediction.confidence.toFixed(1)}%
                  confidence)
                </div>
              )}
            </div>
          </div>

          {wasmAnalytics.error && (
            <div className="mt-4 p-3 bg-red-50 border border-red-200 rounded">
              <div className="text-sm text-red-800">
                <strong>WebAssembly Error:</strong> {wasmAnalytics.error}
              </div>
              <Button
                onClick={wasmAnalytics.refresh}
                variant="outline"
                size="sm"
                className="mt-2"
              >
                Retry Loading
              </Button>
            </div>
          )}
        </CardContent>
      </Card>

      {/* ML Models Status */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Cpu className="h-5 w-5" />
            <span>Active ML Models</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            {activeModels.map((model) => (
              <div
                key={model.id}
                className={`p-4 border rounded-lg space-y-2 ${
                  selectedModel === model.id ? "ring-2 ring-primary" : ""
                }`}
                onClick={() => setSelectedModel(model.id)}
              >
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-sm">{model.name}</span>
                  <div
                    className={`w-2 h-2 rounded-full ${
                      model.status === "Active"
                        ? "bg-success"
                        : model.status === "Training"
                        ? "bg-warning animate-pulse"
                        : model.status === "Error"
                        ? "bg-danger"
                        : "bg-muted"
                    }`}
                  />
                </div>

                <div className="text-xs text-muted-foreground">
                  {model.type}
                </div>

                <div className="flex items-center justify-between text-sm">
                  <span>Accuracy</span>
                  <span className="font-semibold">
                    {model.accuracy.toFixed(1)}%
                  </span>
                </div>

                <div className="text-xs text-muted-foreground">
                  Updated: {formatTimestamp(model.lastUpdate)}
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Analytics Dashboard */}
      {analyticsData && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Sentiment Analysis */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Brain className="h-5 w-5" />
                <span>Market Sentiment</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="text-center">
                <div
                  className={`text-3xl font-bold ${getSentimentColor(
                    analyticsData.sentimentScore
                  )}`}
                >
                  {analyticsData.sentimentScore.toFixed(2)}
                </div>
                <div
                  className={`text-sm font-semibold ${getSentimentColor(
                    analyticsData.sentimentScore
                  )}`}
                >
                  {getSentimentLabel(analyticsData.sentimentScore)}
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Bullish Patterns</span>
                  <span className="font-semibold">
                    {analyticsData.patterns.bullish.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Bearish Patterns</span>
                  <span className="font-semibold">
                    {analyticsData.patterns.bearish.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Neutral Patterns</span>
                  <span className="font-semibold">
                    {analyticsData.patterns.neutral.toFixed(1)}%
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Price Prediction */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Target className="h-5 w-5" />
                <span>Price Prediction</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="text-center">
                <div className="text-sm text-muted-foreground">
                  {analyticsData.pricePrediction.symbol}
                </div>
                <div className="text-2xl font-bold">
                  {formatCurrency(analyticsData.pricePrediction.currentPrice)}
                </div>
                <div className="text-sm text-muted-foreground">
                  Current Price
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Predicted Price</span>
                  <span className="font-semibold">
                    {formatCurrency(
                      analyticsData.pricePrediction.predictedPrice
                    )}
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Confidence</span>
                  <span className="font-semibold">
                    {analyticsData.pricePrediction.confidence.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Timeframe</span>
                  <span className="font-semibold">
                    {analyticsData.pricePrediction.timeframe}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Risk Assessment */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Shield className="h-5 w-5" />
                <span>Risk Assessment</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="text-center">
                <div
                  className={`text-2xl font-bold ${getRiskColor(
                    analyticsData.riskMetrics.overallRisk
                  )}`}
                >
                  {analyticsData.riskMetrics.overallRisk}
                </div>
                <div className="text-sm text-muted-foreground">
                  Overall Risk
                </div>
              </div>

              <div className="space-y-2">
                <div className="flex justify-between text-sm">
                  <span>Portfolio Risk</span>
                  <span className="font-semibold">
                    {analyticsData.riskMetrics.portfolioRisk.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Market Risk</span>
                  <span className="font-semibold">
                    {analyticsData.riskMetrics.marketRisk.toFixed(1)}%
                  </span>
                </div>
                <div className="flex justify-between text-sm">
                  <span>Liquidity Risk</span>
                  <span className="font-semibold">
                    {analyticsData.riskMetrics.liquidityRisk.toFixed(1)}%
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      {/* AI Recommendations */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Lightbulb className="h-5 w-5" />
            <span>AI-Generated Insights</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-3 max-h-96 overflow-y-auto">
            {recommendations.length === 0 ? (
              <div className="text-center text-muted-foreground py-8">
                <Brain className="h-12 w-12 mx-auto mb-4 opacity-50" />
                <p>No AI insights available</p>
                <p className="text-sm">
                  Click "Generate Insights" to create recommendations
                </p>
              </div>
            ) : (
              recommendations.map((rec) => (
                <div
                  key={rec.id}
                  className={`p-4 border rounded-lg ${
                    rec.severity === "CRITICAL"
                      ? "border-danger bg-danger/5"
                      : rec.severity === "HIGH"
                      ? "border-warning bg-warning/5"
                      : rec.severity === "MEDIUM"
                      ? "border-info bg-info/5"
                      : "border-muted bg-muted/5"
                  }`}
                >
                  <div className="flex items-start space-x-3">
                    <div className="mt-1">
                      {rec.type === "ALERT" ? (
                        <AlertTriangle className="h-4 w-4 text-warning" />
                      ) : rec.type === "RECOMMENDATION" ? (
                        <TrendingUp className="h-4 w-4 text-info" />
                      ) : (
                        <Zap className="h-4 w-4 text-danger" />
                      )}
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-1">
                        <h4 className="font-semibold">{rec.title}</h4>
                        <span className="text-xs text-muted-foreground">
                          {formatTimestamp(rec.timestamp)}
                        </span>
                      </div>

                      {rec.symbol && (
                        <div className="text-xs font-mono text-muted-foreground mb-1">
                          {rec.symbol}
                        </div>
                      )}

                      <p className="text-sm text-muted-foreground mb-2">
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
                </div>
              ))
            )}
          </div>
        </CardContent>
      </Card>

      {/* Model Performance */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <BarChart3 className="h-5 w-5" />
            <span>Model Performance</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="text-center p-4 border rounded-lg">
              <div className="text-2xl font-bold text-success">87.3%</div>
              <div className="text-sm text-muted-foreground">
                Sentiment Accuracy
              </div>
            </div>
            <div className="text-center p-4 border rounded-lg">
              <div className="text-2xl font-bold text-info">82.1%</div>
              <div className="text-sm text-muted-foreground">
                Price Prediction
              </div>
            </div>
            <div className="text-center p-4 border rounded-lg">
              <div className="text-2xl font-bold text-warning">91.7%</div>
              <div className="text-sm text-muted-foreground">
                Risk Assessment
              </div>
            </div>
            <div className="text-center p-4 border rounded-lg">
              <div className="text-2xl font-bold text-primary">76.8%</div>
              <div className="text-sm text-muted-foreground">
                Pattern Recognition
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
