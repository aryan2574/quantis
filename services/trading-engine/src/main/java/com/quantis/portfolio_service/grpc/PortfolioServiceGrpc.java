package com.quantis.portfolio_service.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Portfolio Service Definition
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.58.0)",
    comments = "Source: portfolio.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PortfolioServiceGrpc {

  private PortfolioServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "com.quantis.portfolio.PortfolioService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetCashBalanceRequest,
      com.quantis.portfolio_service.grpc.GetCashBalanceResponse> getGetCashBalanceMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCashBalance",
      requestType = com.quantis.portfolio_service.grpc.GetCashBalanceRequest.class,
      responseType = com.quantis.portfolio_service.grpc.GetCashBalanceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetCashBalanceRequest,
      com.quantis.portfolio_service.grpc.GetCashBalanceResponse> getGetCashBalanceMethod() {
    io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetCashBalanceRequest, com.quantis.portfolio_service.grpc.GetCashBalanceResponse> getGetCashBalanceMethod;
    if ((getGetCashBalanceMethod = PortfolioServiceGrpc.getGetCashBalanceMethod) == null) {
      synchronized (PortfolioServiceGrpc.class) {
        if ((getGetCashBalanceMethod = PortfolioServiceGrpc.getGetCashBalanceMethod) == null) {
          PortfolioServiceGrpc.getGetCashBalanceMethod = getGetCashBalanceMethod =
              io.grpc.MethodDescriptor.<com.quantis.portfolio_service.grpc.GetCashBalanceRequest, com.quantis.portfolio_service.grpc.GetCashBalanceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCashBalance"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetCashBalanceRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetCashBalanceResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PortfolioServiceMethodDescriptorSupplier("GetCashBalance"))
              .build();
        }
      }
    }
    return getGetCashBalanceMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPositionValueRequest,
      com.quantis.portfolio_service.grpc.GetPositionValueResponse> getGetPositionValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPositionValue",
      requestType = com.quantis.portfolio_service.grpc.GetPositionValueRequest.class,
      responseType = com.quantis.portfolio_service.grpc.GetPositionValueResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPositionValueRequest,
      com.quantis.portfolio_service.grpc.GetPositionValueResponse> getGetPositionValueMethod() {
    io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPositionValueRequest, com.quantis.portfolio_service.grpc.GetPositionValueResponse> getGetPositionValueMethod;
    if ((getGetPositionValueMethod = PortfolioServiceGrpc.getGetPositionValueMethod) == null) {
      synchronized (PortfolioServiceGrpc.class) {
        if ((getGetPositionValueMethod = PortfolioServiceGrpc.getGetPositionValueMethod) == null) {
          PortfolioServiceGrpc.getGetPositionValueMethod = getGetPositionValueMethod =
              io.grpc.MethodDescriptor.<com.quantis.portfolio_service.grpc.GetPositionValueRequest, com.quantis.portfolio_service.grpc.GetPositionValueResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPositionValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetPositionValueRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetPositionValueResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PortfolioServiceMethodDescriptorSupplier("GetPositionValue"))
              .build();
        }
      }
    }
    return getGetPositionValueMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPortfolioValueRequest,
      com.quantis.portfolio_service.grpc.GetPortfolioValueResponse> getGetPortfolioValueMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPortfolioValue",
      requestType = com.quantis.portfolio_service.grpc.GetPortfolioValueRequest.class,
      responseType = com.quantis.portfolio_service.grpc.GetPortfolioValueResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPortfolioValueRequest,
      com.quantis.portfolio_service.grpc.GetPortfolioValueResponse> getGetPortfolioValueMethod() {
    io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPortfolioValueRequest, com.quantis.portfolio_service.grpc.GetPortfolioValueResponse> getGetPortfolioValueMethod;
    if ((getGetPortfolioValueMethod = PortfolioServiceGrpc.getGetPortfolioValueMethod) == null) {
      synchronized (PortfolioServiceGrpc.class) {
        if ((getGetPortfolioValueMethod = PortfolioServiceGrpc.getGetPortfolioValueMethod) == null) {
          PortfolioServiceGrpc.getGetPortfolioValueMethod = getGetPortfolioValueMethod =
              io.grpc.MethodDescriptor.<com.quantis.portfolio_service.grpc.GetPortfolioValueRequest, com.quantis.portfolio_service.grpc.GetPortfolioValueResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPortfolioValue"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetPortfolioValueRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetPortfolioValueResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PortfolioServiceMethodDescriptorSupplier("GetPortfolioValue"))
              .build();
        }
      }
    }
    return getGetPortfolioValueMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPositionRequest,
      com.quantis.portfolio_service.grpc.GetPositionResponse> getGetPositionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPosition",
      requestType = com.quantis.portfolio_service.grpc.GetPositionRequest.class,
      responseType = com.quantis.portfolio_service.grpc.GetPositionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPositionRequest,
      com.quantis.portfolio_service.grpc.GetPositionResponse> getGetPositionMethod() {
    io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetPositionRequest, com.quantis.portfolio_service.grpc.GetPositionResponse> getGetPositionMethod;
    if ((getGetPositionMethod = PortfolioServiceGrpc.getGetPositionMethod) == null) {
      synchronized (PortfolioServiceGrpc.class) {
        if ((getGetPositionMethod = PortfolioServiceGrpc.getGetPositionMethod) == null) {
          PortfolioServiceGrpc.getGetPositionMethod = getGetPositionMethod =
              io.grpc.MethodDescriptor.<com.quantis.portfolio_service.grpc.GetPositionRequest, com.quantis.portfolio_service.grpc.GetPositionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPosition"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetPositionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetPositionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PortfolioServiceMethodDescriptorSupplier("GetPosition"))
              .build();
        }
      }
    }
    return getGetPositionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetAllPositionsRequest,
      com.quantis.portfolio_service.grpc.GetAllPositionsResponse> getGetAllPositionsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAllPositions",
      requestType = com.quantis.portfolio_service.grpc.GetAllPositionsRequest.class,
      responseType = com.quantis.portfolio_service.grpc.GetAllPositionsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetAllPositionsRequest,
      com.quantis.portfolio_service.grpc.GetAllPositionsResponse> getGetAllPositionsMethod() {
    io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetAllPositionsRequest, com.quantis.portfolio_service.grpc.GetAllPositionsResponse> getGetAllPositionsMethod;
    if ((getGetAllPositionsMethod = PortfolioServiceGrpc.getGetAllPositionsMethod) == null) {
      synchronized (PortfolioServiceGrpc.class) {
        if ((getGetAllPositionsMethod = PortfolioServiceGrpc.getGetAllPositionsMethod) == null) {
          PortfolioServiceGrpc.getGetAllPositionsMethod = getGetAllPositionsMethod =
              io.grpc.MethodDescriptor.<com.quantis.portfolio_service.grpc.GetAllPositionsRequest, com.quantis.portfolio_service.grpc.GetAllPositionsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAllPositions"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetAllPositionsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetAllPositionsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PortfolioServiceMethodDescriptorSupplier("GetAllPositions"))
              .build();
        }
      }
    }
    return getGetAllPositionsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.UpdatePositionRequest,
      com.quantis.portfolio_service.grpc.UpdatePositionResponse> getUpdatePositionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "UpdatePosition",
      requestType = com.quantis.portfolio_service.grpc.UpdatePositionRequest.class,
      responseType = com.quantis.portfolio_service.grpc.UpdatePositionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.UpdatePositionRequest,
      com.quantis.portfolio_service.grpc.UpdatePositionResponse> getUpdatePositionMethod() {
    io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.UpdatePositionRequest, com.quantis.portfolio_service.grpc.UpdatePositionResponse> getUpdatePositionMethod;
    if ((getUpdatePositionMethod = PortfolioServiceGrpc.getUpdatePositionMethod) == null) {
      synchronized (PortfolioServiceGrpc.class) {
        if ((getUpdatePositionMethod = PortfolioServiceGrpc.getUpdatePositionMethod) == null) {
          PortfolioServiceGrpc.getUpdatePositionMethod = getUpdatePositionMethod =
              io.grpc.MethodDescriptor.<com.quantis.portfolio_service.grpc.UpdatePositionRequest, com.quantis.portfolio_service.grpc.UpdatePositionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "UpdatePosition"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.UpdatePositionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.UpdatePositionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PortfolioServiceMethodDescriptorSupplier("UpdatePosition"))
              .build();
        }
      }
    }
    return getUpdatePositionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetTradingHistoryRequest,
      com.quantis.portfolio_service.grpc.GetTradingHistoryResponse> getGetTradingHistoryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTradingHistory",
      requestType = com.quantis.portfolio_service.grpc.GetTradingHistoryRequest.class,
      responseType = com.quantis.portfolio_service.grpc.GetTradingHistoryResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetTradingHistoryRequest,
      com.quantis.portfolio_service.grpc.GetTradingHistoryResponse> getGetTradingHistoryMethod() {
    io.grpc.MethodDescriptor<com.quantis.portfolio_service.grpc.GetTradingHistoryRequest, com.quantis.portfolio_service.grpc.GetTradingHistoryResponse> getGetTradingHistoryMethod;
    if ((getGetTradingHistoryMethod = PortfolioServiceGrpc.getGetTradingHistoryMethod) == null) {
      synchronized (PortfolioServiceGrpc.class) {
        if ((getGetTradingHistoryMethod = PortfolioServiceGrpc.getGetTradingHistoryMethod) == null) {
          PortfolioServiceGrpc.getGetTradingHistoryMethod = getGetTradingHistoryMethod =
              io.grpc.MethodDescriptor.<com.quantis.portfolio_service.grpc.GetTradingHistoryRequest, com.quantis.portfolio_service.grpc.GetTradingHistoryResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTradingHistory"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetTradingHistoryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.quantis.portfolio_service.grpc.GetTradingHistoryResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PortfolioServiceMethodDescriptorSupplier("GetTradingHistory"))
              .build();
        }
      }
    }
    return getGetTradingHistoryMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PortfolioServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PortfolioServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PortfolioServiceStub>() {
        @java.lang.Override
        public PortfolioServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PortfolioServiceStub(channel, callOptions);
        }
      };
    return PortfolioServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PortfolioServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PortfolioServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PortfolioServiceBlockingStub>() {
        @java.lang.Override
        public PortfolioServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PortfolioServiceBlockingStub(channel, callOptions);
        }
      };
    return PortfolioServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PortfolioServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PortfolioServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PortfolioServiceFutureStub>() {
        @java.lang.Override
        public PortfolioServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PortfolioServiceFutureStub(channel, callOptions);
        }
      };
    return PortfolioServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Portfolio Service Definition
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Get user's cash balance
     * </pre>
     */
    default void getCashBalance(com.quantis.portfolio_service.grpc.GetCashBalanceRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetCashBalanceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetCashBalanceMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get user's position value for a specific symbol
     * </pre>
     */
    default void getPositionValue(com.quantis.portfolio_service.grpc.GetPositionValueRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPositionValueResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPositionValueMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get user's total portfolio value
     * </pre>
     */
    default void getPortfolioValue(com.quantis.portfolio_service.grpc.GetPortfolioValueRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPortfolioValueResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPortfolioValueMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get user's position details
     * </pre>
     */
    default void getPosition(com.quantis.portfolio_service.grpc.GetPositionRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPositionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPositionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get all positions for a user
     * </pre>
     */
    default void getAllPositions(com.quantis.portfolio_service.grpc.GetAllPositionsRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetAllPositionsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAllPositionsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Update position after trade execution
     * </pre>
     */
    default void updatePosition(com.quantis.portfolio_service.grpc.UpdatePositionRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.UpdatePositionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getUpdatePositionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get user's trading history
     * </pre>
     */
    default void getTradingHistory(com.quantis.portfolio_service.grpc.GetTradingHistoryRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetTradingHistoryResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTradingHistoryMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service PortfolioService.
   * <pre>
   * Portfolio Service Definition
   * </pre>
   */
  public static abstract class PortfolioServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PortfolioServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service PortfolioService.
   * <pre>
   * Portfolio Service Definition
   * </pre>
   */
  public static final class PortfolioServiceStub
      extends io.grpc.stub.AbstractAsyncStub<PortfolioServiceStub> {
    private PortfolioServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PortfolioServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PortfolioServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Get user's cash balance
     * </pre>
     */
    public void getCashBalance(com.quantis.portfolio_service.grpc.GetCashBalanceRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetCashBalanceResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetCashBalanceMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get user's position value for a specific symbol
     * </pre>
     */
    public void getPositionValue(com.quantis.portfolio_service.grpc.GetPositionValueRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPositionValueResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPositionValueMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get user's total portfolio value
     * </pre>
     */
    public void getPortfolioValue(com.quantis.portfolio_service.grpc.GetPortfolioValueRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPortfolioValueResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPortfolioValueMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get user's position details
     * </pre>
     */
    public void getPosition(com.quantis.portfolio_service.grpc.GetPositionRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPositionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPositionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get all positions for a user
     * </pre>
     */
    public void getAllPositions(com.quantis.portfolio_service.grpc.GetAllPositionsRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetAllPositionsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAllPositionsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Update position after trade execution
     * </pre>
     */
    public void updatePosition(com.quantis.portfolio_service.grpc.UpdatePositionRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.UpdatePositionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getUpdatePositionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get user's trading history
     * </pre>
     */
    public void getTradingHistory(com.quantis.portfolio_service.grpc.GetTradingHistoryRequest request,
        io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetTradingHistoryResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTradingHistoryMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service PortfolioService.
   * <pre>
   * Portfolio Service Definition
   * </pre>
   */
  public static final class PortfolioServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PortfolioServiceBlockingStub> {
    private PortfolioServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PortfolioServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PortfolioServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Get user's cash balance
     * </pre>
     */
    public com.quantis.portfolio_service.grpc.GetCashBalanceResponse getCashBalance(com.quantis.portfolio_service.grpc.GetCashBalanceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetCashBalanceMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get user's position value for a specific symbol
     * </pre>
     */
    public com.quantis.portfolio_service.grpc.GetPositionValueResponse getPositionValue(com.quantis.portfolio_service.grpc.GetPositionValueRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPositionValueMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get user's total portfolio value
     * </pre>
     */
    public com.quantis.portfolio_service.grpc.GetPortfolioValueResponse getPortfolioValue(com.quantis.portfolio_service.grpc.GetPortfolioValueRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPortfolioValueMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get user's position details
     * </pre>
     */
    public com.quantis.portfolio_service.grpc.GetPositionResponse getPosition(com.quantis.portfolio_service.grpc.GetPositionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPositionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get all positions for a user
     * </pre>
     */
    public com.quantis.portfolio_service.grpc.GetAllPositionsResponse getAllPositions(com.quantis.portfolio_service.grpc.GetAllPositionsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAllPositionsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Update position after trade execution
     * </pre>
     */
    public com.quantis.portfolio_service.grpc.UpdatePositionResponse updatePosition(com.quantis.portfolio_service.grpc.UpdatePositionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getUpdatePositionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get user's trading history
     * </pre>
     */
    public com.quantis.portfolio_service.grpc.GetTradingHistoryResponse getTradingHistory(com.quantis.portfolio_service.grpc.GetTradingHistoryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTradingHistoryMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service PortfolioService.
   * <pre>
   * Portfolio Service Definition
   * </pre>
   */
  public static final class PortfolioServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<PortfolioServiceFutureStub> {
    private PortfolioServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PortfolioServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PortfolioServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Get user's cash balance
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.quantis.portfolio_service.grpc.GetCashBalanceResponse> getCashBalance(
        com.quantis.portfolio_service.grpc.GetCashBalanceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetCashBalanceMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get user's position value for a specific symbol
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.quantis.portfolio_service.grpc.GetPositionValueResponse> getPositionValue(
        com.quantis.portfolio_service.grpc.GetPositionValueRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPositionValueMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get user's total portfolio value
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.quantis.portfolio_service.grpc.GetPortfolioValueResponse> getPortfolioValue(
        com.quantis.portfolio_service.grpc.GetPortfolioValueRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPortfolioValueMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get user's position details
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.quantis.portfolio_service.grpc.GetPositionResponse> getPosition(
        com.quantis.portfolio_service.grpc.GetPositionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPositionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get all positions for a user
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.quantis.portfolio_service.grpc.GetAllPositionsResponse> getAllPositions(
        com.quantis.portfolio_service.grpc.GetAllPositionsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAllPositionsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Update position after trade execution
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.quantis.portfolio_service.grpc.UpdatePositionResponse> updatePosition(
        com.quantis.portfolio_service.grpc.UpdatePositionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getUpdatePositionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get user's trading history
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.quantis.portfolio_service.grpc.GetTradingHistoryResponse> getTradingHistory(
        com.quantis.portfolio_service.grpc.GetTradingHistoryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTradingHistoryMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_CASH_BALANCE = 0;
  private static final int METHODID_GET_POSITION_VALUE = 1;
  private static final int METHODID_GET_PORTFOLIO_VALUE = 2;
  private static final int METHODID_GET_POSITION = 3;
  private static final int METHODID_GET_ALL_POSITIONS = 4;
  private static final int METHODID_UPDATE_POSITION = 5;
  private static final int METHODID_GET_TRADING_HISTORY = 6;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_CASH_BALANCE:
          serviceImpl.getCashBalance((com.quantis.portfolio_service.grpc.GetCashBalanceRequest) request,
              (io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetCashBalanceResponse>) responseObserver);
          break;
        case METHODID_GET_POSITION_VALUE:
          serviceImpl.getPositionValue((com.quantis.portfolio_service.grpc.GetPositionValueRequest) request,
              (io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPositionValueResponse>) responseObserver);
          break;
        case METHODID_GET_PORTFOLIO_VALUE:
          serviceImpl.getPortfolioValue((com.quantis.portfolio_service.grpc.GetPortfolioValueRequest) request,
              (io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPortfolioValueResponse>) responseObserver);
          break;
        case METHODID_GET_POSITION:
          serviceImpl.getPosition((com.quantis.portfolio_service.grpc.GetPositionRequest) request,
              (io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetPositionResponse>) responseObserver);
          break;
        case METHODID_GET_ALL_POSITIONS:
          serviceImpl.getAllPositions((com.quantis.portfolio_service.grpc.GetAllPositionsRequest) request,
              (io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetAllPositionsResponse>) responseObserver);
          break;
        case METHODID_UPDATE_POSITION:
          serviceImpl.updatePosition((com.quantis.portfolio_service.grpc.UpdatePositionRequest) request,
              (io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.UpdatePositionResponse>) responseObserver);
          break;
        case METHODID_GET_TRADING_HISTORY:
          serviceImpl.getTradingHistory((com.quantis.portfolio_service.grpc.GetTradingHistoryRequest) request,
              (io.grpc.stub.StreamObserver<com.quantis.portfolio_service.grpc.GetTradingHistoryResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetCashBalanceMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.quantis.portfolio_service.grpc.GetCashBalanceRequest,
              com.quantis.portfolio_service.grpc.GetCashBalanceResponse>(
                service, METHODID_GET_CASH_BALANCE)))
        .addMethod(
          getGetPositionValueMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.quantis.portfolio_service.grpc.GetPositionValueRequest,
              com.quantis.portfolio_service.grpc.GetPositionValueResponse>(
                service, METHODID_GET_POSITION_VALUE)))
        .addMethod(
          getGetPortfolioValueMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.quantis.portfolio_service.grpc.GetPortfolioValueRequest,
              com.quantis.portfolio_service.grpc.GetPortfolioValueResponse>(
                service, METHODID_GET_PORTFOLIO_VALUE)))
        .addMethod(
          getGetPositionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.quantis.portfolio_service.grpc.GetPositionRequest,
              com.quantis.portfolio_service.grpc.GetPositionResponse>(
                service, METHODID_GET_POSITION)))
        .addMethod(
          getGetAllPositionsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.quantis.portfolio_service.grpc.GetAllPositionsRequest,
              com.quantis.portfolio_service.grpc.GetAllPositionsResponse>(
                service, METHODID_GET_ALL_POSITIONS)))
        .addMethod(
          getUpdatePositionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.quantis.portfolio_service.grpc.UpdatePositionRequest,
              com.quantis.portfolio_service.grpc.UpdatePositionResponse>(
                service, METHODID_UPDATE_POSITION)))
        .addMethod(
          getGetTradingHistoryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.quantis.portfolio_service.grpc.GetTradingHistoryRequest,
              com.quantis.portfolio_service.grpc.GetTradingHistoryResponse>(
                service, METHODID_GET_TRADING_HISTORY)))
        .build();
  }

  private static abstract class PortfolioServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PortfolioServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.quantis.portfolio_service.grpc.PortfolioProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PortfolioService");
    }
  }

  private static final class PortfolioServiceFileDescriptorSupplier
      extends PortfolioServiceBaseDescriptorSupplier {
    PortfolioServiceFileDescriptorSupplier() {}
  }

  private static final class PortfolioServiceMethodDescriptorSupplier
      extends PortfolioServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    PortfolioServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PortfolioServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PortfolioServiceFileDescriptorSupplier())
              .addMethod(getGetCashBalanceMethod())
              .addMethod(getGetPositionValueMethod())
              .addMethod(getGetPortfolioValueMethod())
              .addMethod(getGetPositionMethod())
              .addMethod(getGetAllPositionsMethod())
              .addMethod(getUpdatePositionMethod())
              .addMethod(getGetTradingHistoryMethod())
              .build();
        }
      }
    }
    return result;
  }
}
