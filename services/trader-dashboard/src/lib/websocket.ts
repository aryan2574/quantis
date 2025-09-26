import { io, Socket } from "socket.io-client";

class WebSocketService {
  private socket: Socket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;

  connect(token?: string): Socket {
    if (this.socket?.connected) {
      return this.socket;
    }

    this.socket = io("ws://localhost:8081", {
      auth: {
        token: token || localStorage.getItem("auth_token"),
      },
      transports: ["websocket"],
    });

    this.socket.on("connect", () => {
      console.log("WebSocket connected");
      this.reconnectAttempts = 0;
    });

    this.socket.on("disconnect", () => {
      console.log("WebSocket disconnected");
      this.handleReconnect();
    });

    this.socket.on("connect_error", (error) => {
      console.error("WebSocket connection error:", error);
      this.handleReconnect();
    });

    return this.socket;
  }

  private handleReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      setTimeout(() => {
        console.log(
          `Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`
        );
        this.connect();
      }, this.reconnectDelay * this.reconnectAttempts);
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.disconnect();
      this.socket = null;
    }
  }

  subscribeToOrderBook(symbol: string, callback: (data: any) => void) {
    if (this.socket) {
      this.socket.emit("subscribe", { channel: "orderbook", symbol });
      this.socket.on(`orderbook:${symbol}`, callback);
    }
  }

  subscribeToTrades(symbol: string, callback: (data: any) => void) {
    if (this.socket) {
      this.socket.emit("subscribe", { channel: "trades", symbol });
      this.socket.on(`trades:${symbol}`, callback);
    }
  }

  subscribeToMarketData(symbol: string, callback: (data: any) => void) {
    if (this.socket) {
      this.socket.emit("subscribe", { channel: "marketdata", symbol });
      this.socket.on(`marketdata:${symbol}`, callback);
    }
  }

  unsubscribe(channel: string, symbol: string) {
    if (this.socket) {
      this.socket.emit("unsubscribe", { channel, symbol });
      this.socket.off(`${channel}:${symbol}`);
    }
  }

  getSocket(): Socket | null {
    return this.socket;
  }
}

export const websocketService = new WebSocketService();
