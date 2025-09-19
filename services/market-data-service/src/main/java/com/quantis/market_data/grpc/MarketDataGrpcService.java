package com.quantis.market_data.grpc;

import com.quantis.market_data.grpc.MarketDataProto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.redis.core.RedisTemplate;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * gRPC service implementation for market data streaming
 * Provides high-performance streaming for algorithmic trading clients
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class MarketDataGrpcService extends MarketDataServiceGrpc.MarketDataServiceImplBase {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    // Track active subscriptions
    private final ConcurrentMap<String, Boolean> activeSubscriptions = new ConcurrentHashMap<>();
    
    @Override
    public void streamMarketData(StreamMarketDataRequest request, 
                               io.grpc.stub.StreamObserver<MarketDataSnapshot> responseObserver) {
        
        log.info("Starting market data stream for client: {}, symbols: {}", 
                request.getClientId(), request.getSymbolsList());
        
        String subscriptionKey = request.getClientId() + "_market_data";
        activeSubscriptions.put(subscriptionKey, true);
        
        Flux.interval(Duration.ofMillis(request.getUpdateFrequencyMs()))
            .takeWhile(tick -> activeSubscriptions.containsKey(subscriptionKey))
            .map(tick -> {
                List<MarketDataSnapshot> snapshots = request.getSymbolsList().stream()
                    .map(symbol -> getMarketDataSnapshot(symbol))
                    .filter(snapshot -> snapshot != null)
                    .toList();
                
                return snapshots;
            })
            .filter(snapshots -> !snapshots.isEmpty())
            .subscribe(
                snapshots -> {
                    for (MarketDataSnapshot snapshot : snapshots) {
                        try {
                            responseObserver.onNext(snapshot);
                        } catch (Exception e) {
                            log.error("Error sending market data snapshot", e);
                        }
                    }
                },
                error -> {
                    log.error("Error in market data stream for client: {}", request.getClientId(), error);
                    responseObserver.onError(error);
                },
                () -> {
                    log.info("Market data stream completed for client: {}", request.getClientId());
                    responseObserver.onCompleted();
                }
            );
    }
    
    @Override
    public void streamOrderBook(StreamOrderBookRequest request, 
                              io.grpc.stub.StreamObserver<OrderBookSnapshot> responseObserver) {
        
        log.info("Starting order book stream for client: {}, symbols: {}", 
                request.getClientId(), request.getSymbolsList());
        
        String subscriptionKey = request.getClientId() + "_order_book";
        activeSubscriptions.put(subscriptionKey, true);
        
        Flux.interval(Duration.ofMillis(200)) // 5 updates per second
            .takeWhile(tick -> activeSubscriptions.containsKey(subscriptionKey))
            .map(tick -> {
                List<OrderBookSnapshot> snapshots = request.getSymbolsList().stream()
                    .map(symbol -> getOrderBookSnapshot(symbol, request.getDepth()))
                    .filter(snapshot -> snapshot != null)
                    .toList();
                
                return snapshots;
            })
            .filter(snapshots -> !snapshots.isEmpty())
            .subscribe(
                snapshots -> {
                    for (OrderBookSnapshot snapshot : snapshots) {
                        try {
                            responseObserver.onNext(snapshot);
                        } catch (Exception e) {
                            log.error("Error sending order book snapshot", e);
                        }
                    }
                },
                error -> {
                    log.error("Error in order book stream for client: {}", request.getClientId(), error);
                    responseObserver.onError(error);
                },
                () -> {
                    log.info("Order book stream completed for client: {}", request.getClientId());
                    responseObserver.onCompleted();
                }
            );
    }
    
    @Override
    public void streamTrades(StreamTradesRequest request, 
                          io.grpc.stub.StreamObserver<TradeUpdate> responseObserver) {
        
        log.info("Starting trade stream for client: {}, symbols: {}", 
                request.getClientId(), request.getSymbolsList());
        
        String subscriptionKey = request.getClientId() + "_trades";
        activeSubscriptions.put(subscriptionKey, true);
        
        Flux.interval(Duration.ofMillis(500)) // 2 updates per second
            .takeWhile(tick -> activeSubscriptions.containsKey(subscriptionKey))
            .map(tick -> {
                List<TradeUpdate> updates = request.getSymbolsList().stream()
                    .map(symbol -> getTradeUpdate(symbol))
                    .filter(update -> update != null)
                    .toList();
                
                return updates;
            })
            .filter(updates -> !updates.isEmpty())
            .subscribe(
                updates -> {
                    for (TradeUpdate update : updates) {
                        try {
                            responseObserver.onNext(update);
                        } catch (Exception e) {
                            log.error("Error sending trade update", e);
                        }
                    }
                },
                error -> {
                    log.error("Error in trade stream for client: {}", request.getClientId(), error);
                    responseObserver.onError(error);
                },
                () -> {
                    log.info("Trade stream completed for client: {}", request.getClientId());
                    responseObserver.onCompleted();
                }
            );
    }
    
    @Override
    public void getHistoricalData(GetHistoricalDataRequest request, 
                                io.grpc.stub.StreamObserver<GetHistoricalDataResponse> responseObserver) {
        
        log.info("Getting historical data for symbol: {}, interval: {}", 
                request.getSymbol(), request.getInterval());
        
        try {
            GetHistoricalDataResponse response = generateHistoricalDataResponse(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting historical data", e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void subscribeToSymbols(SubscribeRequest request, 
                                 io.grpc.stub.StreamObserver<SubscribeResponse> responseObserver) {
        
        log.info("Subscription request from client: {}, symbols: {}", 
                request.getClientId(), request.getSymbolsList());
        
        try {
            String subscriptionId = "sub_" + System.currentTimeMillis();
            
            // Store subscription details
            for (String symbol : request.getSymbolsList()) {
                String key = request.getClientId() + "_" + symbol;
                activeSubscriptions.put(key, true);
            }
            
            SubscribeResponse response = SubscribeResponse.newBuilder()
                .setSubscriptionId(subscriptionId)
                .setStatus("ACTIVE")
                .setMessage("Subscription created successfully")
                .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            responseObserver.onError(e);
        }
    }
    
    // Helper methods
    private MarketDataSnapshot getMarketDataSnapshot(String symbol) {
        try {
            String data = redisTemplate.opsForValue().get("marketdata:" + symbol);
            if (data != null) {
                // Parse Redis data and create MarketDataSnapshot
                return MarketDataSnapshot.newBuilder()
                    .setSymbol(symbol)
                    .setBestBid(150.0) // Mock data - replace with real parsing
                    .setBestAsk(150.1)
                    .setLastPrice(150.05)
                    .setSpread(0.1)
                    .setVolume(1000000L)
                    .setTimestamp(System.currentTimeMillis())
                    .setMarketSession("REGULAR")
                    .setHighPrice(151.0)
                    .setLowPrice(149.0)
                    .setOpenPrice(150.0)
                    .build();
            }
        } catch (Exception e) {
            log.error("Error getting market data snapshot for symbol: {}", symbol, e);
        }
        return null;
    }
    
    private OrderBookSnapshot getOrderBookSnapshot(String symbol, int depth) {
        try {
            String data = redisTemplate.opsForValue().get("orderbook:" + symbol);
            if (data != null) {
                // Generate mock order book data
                OrderBookSnapshot.Builder builder = OrderBookSnapshot.newBuilder()
                    .setSymbol(symbol)
                    .setTimestamp(System.currentTimeMillis())
                    .setSequenceNumber(System.currentTimeMillis());
                
                // Add bids
                for (int i = 0; i < depth; i++) {
                    builder.addBids(PriceLevel.newBuilder()
                        .setPrice(150.0 - (i * 0.1))
                        .setQuantity(1000L + (i * 100))
                        .setOrderCount(5 + i)
                        .build());
                }
                
                // Add asks
                for (int i = 0; i < depth; i++) {
                    builder.addAsks(PriceLevel.newBuilder()
                        .setPrice(150.1 + (i * 0.1))
                        .setQuantity(1000L + (i * 100))
                        .setOrderCount(5 + i)
                        .build());
                }
                
                return builder.build();
            }
        } catch (Exception e) {
            log.error("Error getting order book snapshot for symbol: {}", symbol, e);
        }
        return null;
    }
    
    private TradeUpdate getTradeUpdate(String symbol) {
        try {
            String data = redisTemplate.opsForValue().get("trades:" + symbol);
            if (data != null) {
                // Generate mock trade data
                return TradeUpdate.newBuilder()
                    .setTradeId("trade_" + System.currentTimeMillis())
                    .setSymbol(symbol)
                    .setSide("BUY")
                    .setQuantity(100L)
                    .setPrice(150.05)
                    .setTotalValue(15005.0)
                    .setTimestamp(System.currentTimeMillis())
                    .setBuyerId("buyer_123")
                    .setSellerId("seller_456")
                    .build();
            }
        } catch (Exception e) {
            log.error("Error getting trade update for symbol: {}", symbol, e);
        }
        return null;
    }
    
    private GetHistoricalDataResponse generateHistoricalDataResponse(GetHistoricalDataRequest request) {
        GetHistoricalDataResponse.Builder responseBuilder = GetHistoricalDataResponse.newBuilder()
            .setSymbol(request.getSymbol())
            .setInterval(request.getInterval());
        
        // Generate mock historical data
        long intervalMs = getIntervalMs(request.getInterval());
        long currentTime = request.getStartTime();
        long endTime = request.getEndTime();
        int limit = request.getLimit();
        
        double basePrice = 150.0;
        int count = 0;
        
        while (currentTime < endTime && count < limit) {
            double open = basePrice + (Math.random() - 0.5) * 2;
            double high = open + Math.random() * 1;
            double low = open - Math.random() * 1;
            double close = low + Math.random() * (high - low);
            long volume = 1000000L + (long)(Math.random() * 500000);
            
            responseBuilder.addDataPoints(HistoricalDataPoint.newBuilder()
                .setTimestamp(currentTime)
                .setOpen(open)
                .setHigh(high)
                .setLow(low)
                .setClose(close)
                .setVolume(volume)
                .build());
            
            currentTime += intervalMs;
            basePrice = close;
            count++;
        }
        
        return responseBuilder.build();
    }
    
    private long getIntervalMs(String interval) {
        switch (interval.toLowerCase()) {
            case "1m": return 60 * 1000;
            case "5m": return 5 * 60 * 1000;
            case "15m": return 15 * 60 * 1000;
            case "1h": return 60 * 60 * 1000;
            case "4h": return 4 * 60 * 60 * 1000;
            case "1d": return 24 * 60 * 60 * 1000;
            default: return 60 * 1000; // Default to 1 minute
        }
    }
}
