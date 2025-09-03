# Configuration Templates

This directory contains template configuration files for Spring Boot applications in the Quantis trading platform.

## Available Templates

### 1. `application-template.yml`

- **Format**: YAML configuration
- **Use Case**: Preferred format for complex configurations
- **Features**:
  - Hierarchical structure
  - Profile-specific configurations
  - Better readability for complex nested properties

### 2. `application-template.properties`

- **Format**: Properties file
- **Use Case**: Simple configurations or when YAML is not preferred
- **Features**:
  - Flat structure
  - Environment variable friendly
  - Traditional Spring Boot format

## How to Use These Templates

### Step 1: Choose Your Format

Decide whether you want to use YAML or Properties format:

- **YAML**: Better for complex configurations, profiles, and readability
- **Properties**: Simpler, flatter structure, easier for environment variables

### Step 2: Copy and Rename

```bash
# For YAML format
cp application-template.yml application.yml

# For Properties format
cp application-template.properties application.properties
```

### Step 3: Customize for Your Service

1. **Change the service name**:

   ```yaml
   spring:
     application:
       name: your-service-name
   ```

2. **Update the server port**:

   ```yaml
   server:
     port: 8081 # Change for each service
   ```

3. **Configure database connections** (if needed):

   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/your-database
       username: your-username
       password: your-password
   ```

4. **Remove unused sections**:
   - Comment out or delete sections you don't need
   - Keep only the configurations relevant to your service

## Configuration Sections

### Core Sections (Required)

- **Server Configuration**: Port, context path, compression
- **Spring Application**: Application name, profiles
- **Logging**: Log levels, patterns, file configuration

### Optional Sections (Remove if not needed)

- **Kafka**: Producer/Consumer configurations
- **Database**: PostgreSQL, JPA/Hibernate settings
- **Redis**: Cache configuration
- **Elasticsearch**: Search configuration
- **Cassandra**: NoSQL database configuration
- **Security**: JWT, authentication settings
- **Trading Platform**: Order processing, risk management
- **Monitoring**: Actuator, metrics, Prometheus

## Environment-Specific Configuration

### Development Profile

```yaml
spring:
  config:
    activate:
      on-profile: dev
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop
```

### Production Profile

```yaml
spring:
  config:
    activate:
      on-profile: prod
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate
```

## Environment Variables

You can override any configuration using environment variables:

```bash
# Override server port
export SERVER_PORT=8081

# Override database URL
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/quantis

# Activate production profile
export SPRING_PROFILES_ACTIVE=prod

# Override Kafka bootstrap servers
export SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka1:9092,kafka2:9092
```

## Docker Integration

For Docker containers, use environment variables in your `docker-compose.yml`:

```yaml
services:
  your-service:
    image: your-service:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SERVER_PORT=8080
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/quantis
      - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

## Security Considerations

### Sensitive Information

- **Never commit** real passwords, API keys, or secrets
- Use environment variables for sensitive data
- Consider using Spring Cloud Config Server for centralized configuration

### JWT Secrets

- Change the default JWT secret in production
- Use strong, randomly generated secrets
- Rotate secrets regularly

## Best Practices

1. **Keep it Simple**: Only include configurations you actually need
2. **Use Profiles**: Separate dev, test, and prod configurations
3. **Environment Variables**: Use them for deployment-specific values
4. **Documentation**: Add comments to explain non-obvious configurations
5. **Validation**: Test your configuration with different profiles
6. **Version Control**: Commit templates, not actual configuration files with secrets

## Troubleshooting

### Common Issues

1. **Port Already in Use**:

   ```bash
   # Check what's using the port
   netstat -an | grep 8080

   # Change the port in your configuration
   server.port=8081
   ```

2. **Database Connection Issues**:

   - Verify database is running
   - Check connection URL, username, password
   - Ensure database exists

3. **Kafka Connection Issues**:

   - Verify Kafka is running
   - Check bootstrap servers
   - Ensure topics exist

4. **Profile Not Active**:

   ```bash
   # Set active profile
   export SPRING_PROFILES_ACTIVE=dev

   # Or use command line argument
   java -jar your-app.jar --spring.profiles.active=dev
   ```

## Examples

### Minimal Configuration (YAML)

```yaml
server:
  port: 8080

spring:
  application:
    name: simple-service

logging:
  level:
    com.quantis: DEBUG
```

### Minimal Configuration (Properties)

```properties
server.port=8080
spring.application.name=simple-service
logging.level.com.quantis=DEBUG
```

### Full Trading Service Configuration

See the template files for complete examples with all trading platform features.
