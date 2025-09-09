package com.quantis.portfolio_service.service;

import com.quantis.portfolio_service.entity.Account;
import com.quantis.portfolio_service.entity.Position;
import com.quantis.portfolio_service.entity.Trade;
import com.quantis.portfolio_service.grpc.*;
import com.quantis.portfolio_service.repository.AccountRepository;
import com.quantis.portfolio_service.repository.PositionRepository;
import com.quantis.portfolio_service.repository.TradeRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * gRPC service implementation for Portfolio operations.
 * Provides fast, efficient communication with Risk Service.
 */
@GrpcService
@RequiredArgsConstructor
@Slf4j
public class PortfolioService extends PortfolioServiceGrpc.PortfolioServiceImplBase {
    
    private final AccountRepository accountRepository;
    private final PositionRepository positionRepository;
    private final TradeRepository tradeRepository;
    
    @Override
    public void getCashBalance(GetCashBalanceRequest request, StreamObserver<GetCashBalanceResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            Optional<Account> accountOpt = accountRepository.findById(userId);
            
            if (accountOpt.isEmpty()) {
                // Create default account if doesn't exist
                Account account = Account.builder()
                    .userId(userId)
                    .cashBalance(BigDecimal.valueOf(100_000.0)) // Default starting balance
                    .currency("USD")
                    .status(Account.AccountStatus.ACTIVE)
                    .build();
                accountRepository.save(account);
                
                responseObserver.onNext(GetCashBalanceResponse.newBuilder()
                    .setCashBalance(100_000.0)
                    .setCurrency("USD")
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build());
            } else {
                Account account = accountOpt.get();
                responseObserver.onNext(GetCashBalanceResponse.newBuilder()
                    .setCashBalance(account.getCashBalance().doubleValue())
                    .setCurrency(account.getCurrency())
                    .setTimestamp(account.getUpdatedAt().toEpochMilli())
                    .build());
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting cash balance for user: {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void getPositionValue(GetPositionValueRequest request, StreamObserver<GetPositionValueResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            Optional<Position> positionOpt = positionRepository.findByUserIdAndSymbol(userId, request.getSymbol());
            
            if (positionOpt.isEmpty()) {
                // Return zero position if doesn't exist
                responseObserver.onNext(GetPositionValueResponse.newBuilder()
                    .setPositionValue(0.0)
                    .setQuantity(0.0)
                    .setAveragePrice(0.0)
                    .setCurrentPrice(0.0)
                    .setUnrealizedPnl(0.0)
                    .setSymbol(request.getSymbol())
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build());
            } else {
                Position position = positionOpt.get();
                responseObserver.onNext(GetPositionValueResponse.newBuilder()
                    .setPositionValue(position.getMarketValue() != null ? position.getMarketValue().doubleValue() : 0.0)
                    .setQuantity(position.getQuantity().doubleValue())
                    .setAveragePrice(position.getAveragePrice().doubleValue())
                    .setCurrentPrice(position.getCurrentPrice() != null ? position.getCurrentPrice().doubleValue() : 0.0)
                    .setUnrealizedPnl(position.getUnrealizedPnl() != null ? position.getUnrealizedPnl().doubleValue() : 0.0)
                    .setSymbol(position.getSymbol())
                    .setTimestamp(position.getUpdatedAt().toEpochMilli())
                    .build());
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting position value for user: {} symbol: {}", request.getUserId(), request.getSymbol(), e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void getPortfolioValue(GetPortfolioValueRequest request, StreamObserver<GetPortfolioValueResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            
            // Get cash balance
            Optional<Account> accountOpt = accountRepository.findById(userId);
            double cashBalance = accountOpt.map(account -> account.getCashBalance().doubleValue()).orElse(100_000.0);
            
            // Get positions value
            Double totalPositionsValue = positionRepository.getTotalPortfolioValue(userId);
            Double totalUnrealizedPnl = positionRepository.getTotalUnrealizedPnl(userId);
            
            double positionsValue = totalPositionsValue != null ? totalPositionsValue : 0.0;
            double unrealizedPnl = totalUnrealizedPnl != null ? totalUnrealizedPnl : 0.0;
            double totalValue = cashBalance + positionsValue;
            
            responseObserver.onNext(GetPortfolioValueResponse.newBuilder()
                .setTotalValue(totalValue)
                .setCashBalance(cashBalance)
                .setPositionsValue(positionsValue)
                .setUnrealizedPnl(unrealizedPnl)
                .setCurrency("USD")
                .setTimestamp(Instant.now().toEpochMilli())
                .build());
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting portfolio value for user: {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void getPosition(GetPositionRequest request, StreamObserver<GetPositionResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            Optional<Position> positionOpt = positionRepository.findByUserIdAndSymbol(userId, request.getSymbol());
            
            if (positionOpt.isEmpty()) {
                responseObserver.onNext(GetPositionResponse.newBuilder()
                    .setUserId(request.getUserId())
                    .setSymbol(request.getSymbol())
                    .setQuantity(0.0)
                    .setAveragePrice(0.0)
                    .setCurrentPrice(0.0)
                    .setMarketValue(0.0)
                    .setUnrealizedPnl(0.0)
                    .setLastUpdated(Instant.now().toEpochMilli())
                    .build());
            } else {
                Position position = positionOpt.get();
                responseObserver.onNext(GetPositionResponse.newBuilder()
                    .setUserId(position.getUserId().toString())
                    .setSymbol(position.getSymbol())
                    .setQuantity(position.getQuantity().doubleValue())
                    .setAveragePrice(position.getAveragePrice().doubleValue())
                    .setCurrentPrice(position.getCurrentPrice() != null ? position.getCurrentPrice().doubleValue() : 0.0)
                    .setMarketValue(position.getMarketValue() != null ? position.getMarketValue().doubleValue() : 0.0)
                    .setUnrealizedPnl(position.getUnrealizedPnl() != null ? position.getUnrealizedPnl().doubleValue() : 0.0)
                    .setLastUpdated(position.getUpdatedAt().toEpochMilli())
                    .build());
            }
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting position for user: {} symbol: {}", request.getUserId(), request.getSymbol(), e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void getAllPositions(GetAllPositionsRequest request, StreamObserver<GetAllPositionsResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            List<Position> positions = positionRepository.findActivePositions(userId);
            
            GetAllPositionsResponse.Builder responseBuilder = GetAllPositionsResponse.newBuilder();
            
            Double totalPositionsValue = positionRepository.getTotalPortfolioValue(userId);
            Double totalUnrealizedPnl = positionRepository.getTotalUnrealizedPnl(userId);
            
            for (Position position : positions) {
                responseBuilder.addPositions(GetPositionResponse.newBuilder()
                    .setUserId(position.getUserId().toString())
                    .setSymbol(position.getSymbol())
                    .setQuantity(position.getQuantity().doubleValue())
                    .setAveragePrice(position.getAveragePrice().doubleValue())
                    .setCurrentPrice(position.getCurrentPrice() != null ? position.getCurrentPrice().doubleValue() : 0.0)
                    .setMarketValue(position.getMarketValue() != null ? position.getMarketValue().doubleValue() : 0.0)
                    .setUnrealizedPnl(position.getUnrealizedPnl() != null ? position.getUnrealizedPnl().doubleValue() : 0.0)
                    .setLastUpdated(position.getUpdatedAt().toEpochMilli())
                    .build());
            }
            
            responseObserver.onNext(responseBuilder
                .setTotalPositionsValue(totalPositionsValue != null ? totalPositionsValue : 0.0)
                .setTotalUnrealizedPnl(totalUnrealizedPnl != null ? totalUnrealizedPnl : 0.0)
                .setTimestamp(Instant.now().toEpochMilli())
                .build());
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting all positions for user: {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void updatePosition(UpdatePositionRequest request, StreamObserver<UpdatePositionResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            BigDecimal quantityChange = BigDecimal.valueOf(request.getQuantityChange());
            BigDecimal price = BigDecimal.valueOf(request.getPrice());
            
            // Check if trade already exists
            if (tradeRepository.existsByOrderId(request.getOrderId())) {
                responseObserver.onNext(UpdatePositionResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Trade already processed")
                    .build());
                responseObserver.onCompleted();
                return;
            }
            
            // Update or create position
            Optional<Position> positionOpt = positionRepository.findByUserIdAndSymbol(userId, request.getSymbol());
            Position position;
            
            if (positionOpt.isEmpty()) {
                // Create new position
                position = Position.builder()
                    .userId(userId)
                    .symbol(request.getSymbol())
                    .quantity(quantityChange)
                    .averagePrice(price)
                    .currentPrice(price)
                    .build();
            } else {
                // Update existing position
                position = positionOpt.get();
                BigDecimal newQuantity = position.getQuantity().add(quantityChange);
                
                if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    // Position closed
                    position.setQuantity(BigDecimal.ZERO);
                } else {
                    // Update average price using weighted average
                    BigDecimal totalValue = position.getQuantity().multiply(position.getAveragePrice())
                        .add(quantityChange.multiply(price));
                    BigDecimal newAveragePrice = totalValue.divide(newQuantity, 8, BigDecimal.ROUND_HALF_UP);
                    
                    position.setQuantity(newQuantity);
                    position.setAveragePrice(newAveragePrice);
                }
            }
            
            // Update current price and calculate values
            position.setCurrentPrice(price);
            position.calculateMarketValue();
            position.calculateUnrealizedPnl();
            
            positionRepository.save(position);
            
            // Record the trade
            Trade trade = Trade.builder()
                .orderId(request.getOrderId())
                .userId(userId)
                .symbol(request.getSymbol())
                .side(Trade.TradeSide.valueOf(request.getSide()))
                .quantity(quantityChange.abs())
                .price(price)
                .totalValue(quantityChange.abs().multiply(price))
                .status(Trade.TradeStatus.EXECUTED)
                .build();
            tradeRepository.save(trade);
            
            // Update account cash balance
            Optional<Account> accountOpt = accountRepository.findById(userId);
            if (accountOpt.isPresent()) {
                Account account = accountOpt.get();
                BigDecimal cashChange = quantityChange.multiply(price);
                if ("SELL".equals(request.getSide())) {
                    account.setCashBalance(account.getCashBalance().add(cashChange.abs()));
                } else {
                    account.setCashBalance(account.getCashBalance().subtract(cashChange.abs()));
                }
                accountRepository.save(account);
            }
            
            responseObserver.onNext(UpdatePositionResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Position updated successfully")
                .setUpdatedPosition(GetPositionResponse.newBuilder()
                    .setUserId(position.getUserId().toString())
                    .setSymbol(position.getSymbol())
                    .setQuantity(position.getQuantity().doubleValue())
                    .setAveragePrice(position.getAveragePrice().doubleValue())
                    .setCurrentPrice(position.getCurrentPrice().doubleValue())
                    .setMarketValue(position.getMarketValue().doubleValue())
                    .setUnrealizedPnl(position.getUnrealizedPnl().doubleValue())
                    .setLastUpdated(position.getUpdatedAt().toEpochMilli())
                    .build())
                .build());
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error updating position for user: {} symbol: {}", request.getUserId(), request.getSymbol(), e);
            responseObserver.onError(e);
        }
    }
    
    @Override
    public void getTradingHistory(GetTradingHistoryRequest request, StreamObserver<GetTradingHistoryResponse> responseObserver) {
        try {
            UUID userId = UUID.fromString(request.getUserId());
            Pageable pageable = PageRequest.of(0, request.getLimit());
            
            Page<Trade> trades;
            if (request.getStartTimestamp() > 0 && request.getEndTimestamp() > 0) {
                Instant startTime = Instant.ofEpochMilli(request.getStartTimestamp());
                Instant endTime = Instant.ofEpochMilli(request.getEndTimestamp());
                trades = tradeRepository.findTradesByUserAndTimeRange(userId, startTime, endTime, pageable);
            } else {
                trades = tradeRepository.findByUserIdOrderByExecutedAtDesc(userId, pageable);
            }
            
            GetTradingHistoryResponse.Builder responseBuilder = GetTradingHistoryResponse.newBuilder();
            
            for (Trade trade : trades.getContent()) {
                responseBuilder.addTrades(TradingRecord.newBuilder()
                    .setOrderId(trade.getOrderId())
                    .setSymbol(trade.getSymbol())
                    .setSide(trade.getSide().name())
                    .setQuantity(trade.getQuantity().doubleValue())
                    .setPrice(trade.getPrice().doubleValue())
                    .setTotalValue(trade.getTotalValue().doubleValue())
                    .setTimestamp(trade.getExecutedAt().toEpochMilli())
                    .setStatus(trade.getStatus().name())
                    .build());
            }
            
            responseObserver.onNext(responseBuilder
                .setTotalCount((int) trades.getTotalElements())
                .setTimestamp(Instant.now().toEpochMilli())
                .build());
            
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error getting trading history for user: {}", request.getUserId(), e);
            responseObserver.onError(e);
        }
    }
}
