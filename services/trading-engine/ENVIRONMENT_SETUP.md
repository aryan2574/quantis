# Trading Engine Environment Setup

This document explains how to configure environment variables for the Trading Engine service, particularly for the Alpha Vantage API integration.

## Quick Setup

1. **Copy the environment template:**

   ```bash
   cp env.template .env
   ```

2. **Get an Alpha Vantage API key:**

   - Visit: https://www.alphavantage.co/support/#api-key
   - Sign up for a free account
   - Copy your API key

3. **Edit the .env file:**

   ```bash
   nano .env
   ```

   Update the following values:

   ```env
   ALPHA_VANTAGE_API_KEY=your_actual_api_key_here
   ALPHA_VANTAGE_UPDATE_INTERVAL=12000
   ```

4. **Start the trading engine:**
   ```bash
   ./mvnw spring-boot:run
   ```

## Environment Variables

### Required Variables

| Variable                | Description                | Example        |
| ----------------------- | -------------------------- | -------------- |
| `ALPHA_VANTAGE_API_KEY` | Your Alpha Vantage API key | `ABC123XYZ789` |

### Optional Variables

| Variable                        | Description                     | Default          | Notes                                   |
| ------------------------------- | ------------------------------- | ---------------- | --------------------------------------- |
| `ALPHA_VANTAGE_UPDATE_INTERVAL` | Update interval in milliseconds | `12000`          | Free tier: 12000ms (12s), Premium: 12ms |
| `KAFKA_BOOTSTRAP_SERVERS`       | Kafka broker addresses          | `localhost:9092` |                                         |
| `PORTFOLIO_SERVICE_HOST`        | Portfolio service hostname      | `localhost`      |                                         |
| `PORTFOLIO_SERVICE_PORT`        | Portfolio service port          | `9090`           |                                         |
| `SERVER_PORT`                   | Trading engine HTTP port        | `8083`           |                                         |

## Alpha Vantage API Tiers

### Free Tier

- **Rate Limit:** 5 calls per minute, 500 calls per day
- **Recommended Update Interval:** 12000ms (12 seconds)
- **Use Case:** Development, testing, low-frequency trading

### Premium Tiers

- **Rate Limit:** Up to 1200 calls per minute, 100,000 calls per day
- **Recommended Update Interval:** 12ms (~83 updates/second)
- **Use Case:** Production, high-frequency trading

## Configuration Methods

### Method 1: Environment File (.env)

```bash
# Create .env file
cp env.template .env

# Edit with your values
nano .env

# Start the service
./mvnw spring-boot:run
```

### Method 2: Environment Variables

```bash
export ALPHA_VANTAGE_API_KEY=your_api_key_here
export ALPHA_VANTAGE_UPDATE_INTERVAL=12000
./mvnw spring-boot:run
```

### Method 3: Docker Environment

```bash
docker run -e ALPHA_VANTAGE_API_KEY=your_api_key_here \
           -e ALPHA_VANTAGE_UPDATE_INTERVAL=12000 \
           trading-engine:latest
```

### Method 4: Application Properties Override

Create `application-local.yml`:

```yaml
market-data:
  alpha-vantage:
    api-key: your_api_key_here
    update-interval-ms: 12000
```

Then run with profile:

```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

## Security Best Practices

1. **Never commit API keys to version control**

   ```bash
   # Add to .gitignore
   echo ".env" >> .gitignore
   ```

2. **Use different keys for different environments**

   - Development: Free tier key
   - Staging: Premium tier key (limited)
   - Production: Premium tier key (full)

3. **Rotate keys regularly**

   - Set up key rotation schedule
   - Monitor usage and alerts

4. **Use environment-specific configuration**

   ```bash
   # Development
   ALPHA_VANTAGE_API_KEY=dev_key_123

   # Production
   ALPHA_VANTAGE_API_KEY=prod_key_456
   ```

## Troubleshooting

### Common Issues

1. **API Key Not Working**

   ```
   Error: Invalid API key
   ```

   - Verify the key is correct
   - Check if you've exceeded rate limits
   - Ensure the key is properly set in environment

2. **Rate Limit Exceeded**

   ```
   Error: API call frequency is 5 calls per minute
   ```

   - Increase `ALPHA_VANTAGE_UPDATE_INTERVAL` to 12000ms or higher
   - Consider upgrading to premium tier

3. **Configuration Not Loading**
   ```
   Error: API key is empty
   ```
   - Check if `.env` file exists and is readable
   - Verify environment variables are set correctly
   - Check application logs for configuration errors

### Verification

Test your configuration:

```bash
# Check if environment variables are loaded
curl http://localhost:8083/actuator/health

# Test market data endpoint
curl http://localhost:8083/api/market-data/AAPL

# Check logs for configuration
tail -f logs/trading-engine.log | grep "API Key"
```

## Production Deployment

For production deployment:

1. **Use secure secret management**

   - Kubernetes Secrets
   - AWS Secrets Manager
   - HashiCorp Vault

2. **Set appropriate rate limits**

   ```env
   ALPHA_VANTAGE_UPDATE_INTERVAL=12  # 12ms for high-frequency
   ```

3. **Monitor API usage**

   - Set up alerts for rate limit approaching
   - Monitor API response times
   - Track failed requests

4. **Health checks**
   ```bash
   # Add to health check script
   curl -f http://localhost:8083/api/market-data/health || exit 1
   ```
