import { gql } from "@apollo/client";

// ==================== ORDER MUTATIONS ====================

export const PLACE_ORDER = gql`
  mutation PlaceOrder($input: PlaceOrderInput!) {
    placeOrder(input: $input) {
      success
      orderId
      message
      order {
        orderId
        userId
        symbol
        side
        quantity
        price
        orderType
        timeInForce
        status
        filledQuantity
        averagePrice
        createdAt
        updatedAt
        executedAt
        commission
        metadata
      }
      errors
    }
  }
`;

export const CANCEL_ORDER = gql`
  mutation CancelOrder($orderId: String!) {
    cancelOrder(orderId: $orderId) {
      success
      message
      orderId
      errors
    }
  }
`;

export const MODIFY_ORDER = gql`
  mutation ModifyOrder($input: ModifyOrderInput!) {
    modifyOrder(input: $input) {
      success
      orderId
      message
      order {
        orderId
        userId
        symbol
        side
        quantity
        price
        orderType
        timeInForce
        status
        filledQuantity
        averagePrice
        createdAt
        updatedAt
        executedAt
        commission
        metadata
      }
      errors
    }
  }
`;

// ==================== PORTFOLIO MUTATIONS ====================

export const UPDATE_WATCHLIST = gql`
  mutation UpdateWatchlist($userId: String!, $symbols: [String!]!) {
    updateWatchlist(userId: $userId, symbols: $symbols) {
      success
      message
      symbols
      errors
    }
  }
`;
