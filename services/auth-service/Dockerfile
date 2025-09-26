# ==================== BUILD STAGE ====================
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --only=production

# Copy source code
COPY . .

# Build the application (if needed)
RUN npm run build 2>/dev/null || echo "No build script found, skipping..."

# ==================== RUNTIME STAGE ====================
FROM node:18-alpine

# Create non-root user for security
RUN addgroup -g 1001 -S quantis && \
    adduser -u 1001 -S quantis -G quantis

# Set working directory
WORKDIR /app

# Copy package files and install production dependencies
COPY package*.json ./
RUN npm ci --only=production && npm cache clean --force

# Copy source code from build stage
COPY --from=build /app/src ./src
COPY --from=build /app/tsconfig.json ./
COPY --from=build /app/nest-cli.json ./

# Copy dist directory (NestJS build output)
COPY --from=build /app/dist ./dist

# Change ownership to non-root user
RUN chown -R quantis:quantis /app

# Switch to non-root user
USER quantis

# Expose port
EXPOSE 3001

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3001/health || exit 1

# Run the application
CMD ["npm", "run", "start:prod"]
