package com.quantis.order_ingress.model;

/**
 * Data model representing a trading order in the Quantis trading platform.
 * This class defines the structure of trading orders that can be placed by users.
 * 
 * The Order object is:
 * 1. Deserialized from JSON in HTTP requests
 * 2. Validated by the controller
 * 3. Serialized back to JSON for Kafka messaging
 */
public class Order {
    
    /**
     * Unique identifier for the order.
     * Generated automatically by the system when order is created.
     * Format: UUID string (e.g., "550e8400-e29b-41d4-a716-446655440000")
     */
    private String orderId;
    
    /**
     * Identifier of the user placing the order.
     * Links the order to a specific trader/account.
     */
    private String userId;
    
    /**
     * Trading symbol (stock ticker).
     * Examples: "AAPL", "GOOGL", "MSFT", "TSLA"
     */
    private String symbol;
    
    /**
     * Order side - indicates whether this is a buy or sell order.
     * Valid values: "BUY" or "SELL"
     */
    private String side;   // BUY or SELL
    
    /**
     * Number of shares/units to trade.
     * Must be a positive integer.
     */
    private long quantity;
    
    /**
     * Price per share/unit.
     * Decimal value representing the limit price for the order.
     */
    private double price;

    // ==================== GETTERS AND SETTERS ====================
    // These methods are required for JSON serialization/deserialization
    // Jackson (the JSON library) uses these methods to convert between JSON and Java objects
    
    /**
     * Get the unique order identifier.
     * @return The order ID string
     */
    public String getOrderId() {
        return orderId;
    }

    /**
     * Set the unique order identifier.
     * @param orderId The order ID to set
     */
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * Get the user identifier.
     * @return The user ID string
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Set the user identifier.
     * @param userId The user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Get the trading symbol.
     * @return The symbol string (e.g., "AAPL")
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Set the trading symbol.
     * @param symbol The symbol to set
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Get the order side (BUY or SELL).
     * @return The side string
     */
    public String getSide() {
        return side;
    }

    /**
     * Set the order side.
     * @param side The side to set (should be "BUY" or "SELL")
     */
    public void setSide(String side) {
        this.side = side;
    }

    /**
     * Get the quantity of shares/units.
     * @return The quantity as a long integer
     */
    public long getQuantity() {
        return quantity;
    }

    /**
     * Set the quantity of shares/units.
     * @param quantity The quantity to set
     */
    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    /**
     * Get the price per share/unit.
     * @return The price as a double
     */
    public double getPrice() {
        return price;
    }

    /**
     * Set the price per share/unit.
     * @param price The price to set
     */
    public void setPrice(double price) {
        this.price = price;
    }
}
