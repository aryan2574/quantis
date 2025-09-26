// Order type definitions
import { AssetType } from "./asset-types";

export interface Order {
  id: string;
  symbol: string;
  assetType: AssetType;
  side: "BUY" | "SELL";
  quantity: number;
  price: number;
  orderType: "MARKET" | "LIMIT" | "STOP";
  status: "PENDING" | "FILLED" | "CANCELLED" | "REJECTED";
  timestamp: string;
  filledQuantity?: number;
  averagePrice?: number;
}

export interface PlaceOrderInput {
  symbol: string;
  side: "BUY" | "SELL";
  quantity: number;
  price?: number;
  orderType: "MARKET" | "LIMIT" | "STOP";
}

export interface ModifyOrderInput {
  orderId: string;
  quantity?: number;
  price?: number;
}

export interface OrderResponse {
  success: boolean;
  orderId?: string;
  message?: string;
}

export interface CancelResponse {
  success: boolean;
  message?: string;
}
