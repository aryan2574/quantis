-- ==================== CITUS SETUP ====================
-- Enable Citus extension for horizontal scaling
-- This script sets up distributed tables for high-performance trading

-- Enable Citus extension
CREATE EXTENSION IF NOT EXISTS citus;

-- ==================== DISTRIBUTED TABLES SETUP ====================

-- 1. Orders table - distributed by user_id for user-specific queries
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    side VARCHAR(4) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity DECIMAL(20,8) NOT NULL,
    price DECIMAL(20,8) NOT NULL,
    order_type VARCHAR(10) NOT NULL CHECK (order_type IN ('MARKET', 'LIMIT', 'STOP')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE,
    filled_quantity DECIMAL(20,8) DEFAULT 0,
    remaining_quantity DECIMAL(20,8),
    average_fill_price DECIMAL(20,8),
    total_fees DECIMAL(20,8) DEFAULT 0
);

-- 2. Trades table - distributed by symbol for market data queries
CREATE TABLE IF NOT EXISTS trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    user_id UUID NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    side VARCHAR(4) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity DECIMAL(20,8) NOT NULL,
    price DECIMAL(20,8) NOT NULL,
    total_value DECIMAL(20,8) NOT NULL,
    executed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    fees DECIMAL(20,8) DEFAULT 0,
    counterparty_order_id UUID,
    trade_status VARCHAR(20) NOT NULL DEFAULT 'EXECUTED'
);

-- 3. Portfolio table - distributed by user_id for user portfolio queries
CREATE TABLE IF NOT EXISTS portfolios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    symbol VARCHAR(20) NOT NULL,
    quantity DECIMAL(20,8) NOT NULL DEFAULT 0,
    average_cost DECIMAL(20,8) NOT NULL DEFAULT 0,
    total_cost DECIMAL(20,8) NOT NULL DEFAULT 0,
    current_value DECIMAL(20,8) NOT NULL DEFAULT 0,
    unrealized_pnl DECIMAL(20,8) NOT NULL DEFAULT 0,
    realized_pnl DECIMAL(20,8) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(user_id, symbol)
);

-- 4. Market data table - distributed by symbol for real-time data
CREATE TABLE IF NOT EXISTS market_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(20) NOT NULL,
    price DECIMAL(20,8) NOT NULL,
    volume DECIMAL(20,8) NOT NULL,
    bid DECIMAL(20,8),
    ask DECIMAL(20,8),
    high_24h DECIMAL(20,8),
    low_24h DECIMAL(20,8),
    change_24h DECIMAL(20,8),
    change_percent_24h DECIMAL(10,4),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    data_source VARCHAR(50) NOT NULL
);

-- ==================== DISTRIBUTE TABLES ====================

-- Distribute orders by user_id (co-location with portfolios)
SELECT create_distributed_table('orders', 'user_id');

-- Distribute trades by symbol (for market analysis)
SELECT create_distributed_table('trades', 'symbol');

-- Distribute portfolios by user_id (co-location with orders)
SELECT create_distributed_table('portfolios', 'user_id');

-- Distribute market_data by symbol (for real-time queries)
SELECT create_distributed_table('market_data', 'symbol');

-- ==================== INDEXES FOR PERFORMANCE ====================

-- Orders indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_user_status ON orders(user_id, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_symbol_status ON orders(symbol, status);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_orders_created_at ON orders(created_at DESC);

-- Trades indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_trades_user_executed ON trades(user_id, executed_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_trades_symbol_executed ON trades(symbol, executed_at DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_trades_executed_at ON trades(executed_at DESC);

-- Portfolio indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_portfolios_user_symbol ON portfolios(user_id, symbol);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_portfolios_user_updated ON portfolios(user_id, last_updated DESC);

-- Market data indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_market_data_symbol_timestamp ON market_data(symbol, timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_market_data_timestamp ON market_data(timestamp DESC);

-- ==================== REFERENCE TABLES (Small, replicated tables) ====================

-- Symbols reference table (small, replicated to all nodes)
CREATE TABLE IF NOT EXISTS symbols (
    symbol VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    asset_type VARCHAR(20) NOT NULL,
    exchange VARCHAR(50) NOT NULL,
    base_currency VARCHAR(10),
    quote_currency VARCHAR(10),
    min_quantity DECIMAL(20,8),
    max_quantity DECIMAL(20,8),
    price_precision INTEGER DEFAULT 8,
    quantity_precision INTEGER DEFAULT 8,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Users reference table (small, replicated to all nodes)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'TRADER',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Make reference tables replicated (small tables copied to all nodes)
SELECT create_reference_table('symbols');
SELECT create_reference_table('users');

-- ==================== PERFORMANCE OPTIMIZATIONS ====================

-- Enable query parallelization
SET citus.enable_repartition_joins = on;
SET citus.limit_clause_row_fetch_count = 10000;

-- Optimize for trading workloads
ALTER TABLE orders SET (fillfactor = 90);
ALTER TABLE trades SET (fillfactor = 90);
ALTER TABLE portfolios SET (fillfactor = 95);

-- ==================== MONITORING VIEWS ====================

-- View for distributed table statistics
CREATE OR REPLACE VIEW distributed_table_stats AS
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size,
    pg_stat_get_tuples_returned(c.oid) as tuples_returned,
    pg_stat_get_tuples_fetched(c.oid) as tuples_fetched
FROM pg_tables t
JOIN pg_class c ON c.relname = t.tablename
WHERE schemaname = 'public'
AND tablename IN ('orders', 'trades', 'portfolios', 'market_data');

-- View for cluster health
CREATE OR REPLACE VIEW cluster_health AS
SELECT 
    nodename,
    nodeport,
    CASE 
        WHEN isactive THEN 'ACTIVE'
        ELSE 'INACTIVE'
    END as status,
    noderack,
    hasmetadata
FROM pg_dist_node;

-- ==================== SAMPLE DATA ====================

-- Insert sample symbols
INSERT INTO symbols (symbol, name, asset_type, exchange, base_currency, quote_currency, min_quantity, max_quantity) VALUES
('BTCUSD', 'Bitcoin', 'CRYPTO', 'BINANCE', 'BTC', 'USD', 0.00001, 1000),
('ETHUSD', 'Ethereum', 'CRYPTO', 'BINANCE', 'ETH', 'USD', 0.0001, 10000),
('AAPL', 'Apple Inc', 'STOCK', 'NASDAQ', 'USD', 'USD', 1, 1000000),
('TSLA', 'Tesla Inc', 'STOCK', 'NASDAQ', 'USD', 'USD', 1, 1000000),
('EURUSD', 'Euro/USD', 'FOREX', 'OANDA', 'EUR', 'USD', 0.0001, 1000000)
ON CONFLICT (symbol) DO NOTHING;

-- Insert sample user
INSERT INTO users (username, email, first_name, last_name, role) VALUES
('trader1', 'trader1@quantis.com', 'John', 'Doe', 'TRADER'),
('admin1', 'admin@quantis.com', 'Admin', 'User', 'ADMIN')
ON CONFLICT (username) DO NOTHING;

COMMIT;
