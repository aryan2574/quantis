-- Quantis Trading Database Setup
-- This script creates the necessary tables and sample data

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    risk_tolerance VARCHAR(20) DEFAULT 'Moderate',
    trading_style VARCHAR(50) DEFAULT 'Day Trading',
    account_type VARCHAR(50) DEFAULT 'Standard',
    account_status VARCHAR(20) DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Create portfolios table
CREATE TABLE IF NOT EXISTS portfolios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    cash_balance DECIMAL(15,2) DEFAULT 0.00,
    total_value DECIMAL(15,2) DEFAULT 0.00,
    total_pnl DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create positions table
CREATE TABLE IF NOT EXISTS positions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    portfolio_id UUID REFERENCES portfolios(id) ON DELETE CASCADE,
    symbol VARCHAR(10) NOT NULL,
    quantity INTEGER NOT NULL,
    average_price DECIMAL(10,2) NOT NULL,
    current_price DECIMAL(10,2),
    unrealized_pnl DECIMAL(15,2) DEFAULT 0.00,
    realized_pnl DECIMAL(15,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    symbol VARCHAR(10) NOT NULL,
    side VARCHAR(4) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    quantity INTEGER NOT NULL,
    price DECIMAL(10,2),
    order_type VARCHAR(10) NOT NULL CHECK (order_type IN ('MARKET', 'LIMIT', 'STOP')),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'FILLED', 'CANCELLED', 'REJECTED')),
    filled_quantity INTEGER DEFAULT 0,
    average_price DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create trades table
CREATE TABLE IF NOT EXISTS trades (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID REFERENCES orders(id),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    symbol VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    side VARCHAR(4) NOT NULL CHECK (side IN ('BUY', 'SELL')),
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create market_data table
CREATE TABLE IF NOT EXISTS market_data (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    symbol VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    change DECIMAL(10,2),
    change_percent DECIMAL(5,2),
    volume BIGINT,
    high DECIMAL(10,2),
    low DECIMAL(10,2),
    open DECIMAL(10,2),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create ai_recommendations table
CREATE TABLE IF NOT EXISTS ai_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(20) NOT NULL CHECK (type IN ('ALERT', 'RECOMMENDATION', 'ANOMALY')),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    symbol VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_portfolios_user_id ON portfolios(user_id);
CREATE INDEX IF NOT EXISTS idx_positions_portfolio_id ON positions(portfolio_id);
CREATE INDEX IF NOT EXISTS idx_positions_symbol ON positions(symbol);
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_symbol ON orders(symbol);
CREATE INDEX IF NOT EXISTS idx_trades_user_id ON trades(user_id);
CREATE INDEX IF NOT EXISTS idx_trades_symbol ON trades(symbol);
CREATE INDEX IF NOT EXISTS idx_market_data_symbol ON market_data(symbol);
CREATE INDEX IF NOT EXISTS idx_market_data_timestamp ON market_data(timestamp);
CREATE INDEX IF NOT EXISTS idx_ai_recommendations_user_id ON ai_recommendations(user_id);

-- Insert sample user (password: password123)
INSERT INTO users (username, email, password_hash, first_name, last_name, risk_tolerance, trading_style, account_type) 
VALUES ('trader', 'trader@quantis.com', '$2b$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'John', 'Trader', 'Moderate', 'Day Trading', 'Professional')
ON CONFLICT (username) DO NOTHING;

-- Insert sample portfolio for the trader user
INSERT INTO portfolios (user_id, cash_balance, total_value, total_pnl)
SELECT id, 50000.00, 50000.00, 0.00 FROM users WHERE username = 'trader'
ON CONFLICT DO NOTHING;

-- Insert sample positions
INSERT INTO positions (portfolio_id, symbol, quantity, average_price, current_price, unrealized_pnl, realized_pnl)
SELECT p.id, 'AAPL', 100, 145.50, 150.25, 475.00, 250.00
FROM portfolios p 
JOIN users u ON p.user_id = u.id 
WHERE u.username = 'trader'
ON CONFLICT DO NOTHING;

INSERT INTO positions (portfolio_id, symbol, quantity, average_price, current_price, unrealized_pnl, realized_pnl)
SELECT p.id, 'GOOGL', 50, 2800.00, 2850.75, 2537.50, 0.00
FROM portfolios p 
JOIN users u ON p.user_id = u.id 
WHERE u.username = 'trader'
ON CONFLICT DO NOTHING;

INSERT INTO positions (portfolio_id, symbol, quantity, average_price, current_price, unrealized_pnl, realized_pnl)
SELECT p.id, 'TSLA', 25, 200.00, 195.50, -112.50, 500.00
FROM portfolios p 
JOIN users u ON p.user_id = u.id 
WHERE u.username = 'trader'
ON CONFLICT DO NOTHING;

INSERT INTO positions (portfolio_id, symbol, quantity, average_price, current_price, unrealized_pnl, realized_pnl)
SELECT p.id, 'MSFT', 75, 300.00, 305.25, 393.75, 150.00
FROM portfolios p 
JOIN users u ON p.user_id = u.id 
WHERE u.username = 'trader'
ON CONFLICT DO NOTHING;

-- Insert sample market data
INSERT INTO market_data (symbol, price, change, change_percent, volume, high, low, open)
VALUES 
    ('AAPL', 150.25, 2.15, 1.45, 45000000, 151.50, 148.10, 148.10),
    ('GOOGL', 2850.75, -15.25, -0.53, 1200000, 2865.00, 2840.50, 2865.00),
    ('TSLA', 195.50, -8.75, -4.28, 25000000, 204.25, 194.00, 204.25),
    ('MSFT', 305.25, 3.75, 1.24, 18000000, 306.00, 301.50, 301.50),
    ('AMZN', 3200.00, 25.50, 0.80, 8000000, 3210.00, 3174.50, 3174.50),
    ('NVDA', 450.75, 12.25, 2.80, 15000000, 455.00, 438.50, 438.50)
ON CONFLICT DO NOTHING;

-- Insert sample AI recommendations
INSERT INTO ai_recommendations (user_id, type, title, message, severity, symbol, created_at)
SELECT u.id, 'ALERT', 'High Portfolio Risk', 'Portfolio risk exceeds recommended threshold. Consider reducing position sizes.', 'HIGH', 'PORTFOLIO', NOW()
FROM users u WHERE u.username = 'trader'
ON CONFLICT DO NOTHING;

INSERT INTO ai_recommendations (user_id, type, title, message, severity, symbol, created_at)
SELECT u.id, 'RECOMMENDATION', 'Bullish Market Sentiment', 'Strong bullish momentum detected in AAPL. Consider taking long positions.', 'MEDIUM', 'AAPL', NOW()
FROM users u WHERE u.username = 'trader'
ON CONFLICT DO NOTHING;

-- Update portfolio total value based on positions
UPDATE portfolios 
SET total_value = (
    SELECT COALESCE(SUM(p.quantity * p.current_price), 0) + portfolios.cash_balance
    FROM positions p 
    WHERE p.portfolio_id = portfolios.id
)
WHERE user_id = (SELECT id FROM users WHERE username = 'trader');

-- Update portfolio total P&L
UPDATE portfolios 
SET total_pnl = (
    SELECT COALESCE(SUM(p.unrealized_pnl + p.realized_pnl), 0)
    FROM positions p 
    WHERE p.portfolio_id = portfolios.id
)
WHERE user_id = (SELECT id FROM users WHERE username = 'trader');
