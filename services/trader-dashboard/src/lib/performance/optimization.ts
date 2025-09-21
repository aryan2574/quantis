/**
 * Frontend Performance Optimization for 10,000+ Users
 *
 * Provides:
 * - Request batching
 * - Data pagination
 * - Virtual scrolling
 * - Memory management
 * - Connection pooling
 */

// import { ApolloClient, InMemoryCache } from "@apollo/client";

// ==================== REQUEST BATCHING ====================

export class RequestBatcher {
  private batchQueue: Map<string, any[]> = new Map();
  private batchTimeout: number = 50; // 50ms batching window
  private timers: Map<string, NodeJS.Timeout> = new Map();

  /**
   * Add request to batch queue
   */
  addToBatch<T>(batchKey: string, request: T): Promise<any> {
    return new Promise((resolve, reject) => {
      if (!this.batchQueue.has(batchKey)) {
        this.batchQueue.set(batchKey, []);
      }

      const batch = this.batchQueue.get(batchKey)!;
      batch.push({ request, resolve, reject });

      // Clear existing timer
      const existingTimer = this.timers.get(batchKey);
      if (existingTimer) {
        clearTimeout(existingTimer);
      }

      // Set new timer
      const timer = setTimeout(() => {
        this.processBatch(batchKey);
      }, this.batchTimeout);

      this.timers.set(batchKey, timer);
    });
  }

  /**
   * Process batched requests
   */
  private async processBatch(batchKey: string) {
    const batch = this.batchQueue.get(batchKey);
    if (!batch || batch.length === 0) return;

    // Clear the batch
    this.batchQueue.set(batchKey, []);
    this.timers.delete(batchKey);

    try {
      // Process all requests in the batch
      const results = await this.executeBatch(
        batchKey,
        batch.map((item) => item.request)
      );

      // Resolve all promises
      batch.forEach((item, index) => {
        item.resolve(results[index]);
      });
    } catch (error) {
      // Reject all promises
      batch.forEach((item) => {
        item.reject(error);
      });
    }
  }

  /**
   * Execute batch requests (to be implemented by specific services)
   */
  private async executeBatch(
    _batchKey: string,
    _requests: any[]
  ): Promise<any[]> {
    // This would be implemented by specific services
    // For example, GraphQL batching, REST API batching, etc.
    throw new Error("executeBatch must be implemented by subclass");
  }
}

// ==================== DATA PAGINATION ====================

export interface PaginationOptions {
  page: number;
  limit: number;
  sortBy?: string;
  sortOrder?: "asc" | "desc";
  filters?: Record<string, any>;
}

export interface PaginatedResult<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}

export class PaginationManager<T> {
  private cache: Map<string, PaginatedResult<T>> = new Map();
  private readonly cacheTTL: number = 5 * 60 * 1000; // 5 minutes
  private readonly maxCacheSize: number = 100;

  /**
   * Get paginated data with caching
   */
  async getPaginatedData(
    queryKey: string,
    fetchFunction: (options: PaginationOptions) => Promise<PaginatedResult<T>>,
    options: PaginationOptions
  ): Promise<PaginatedResult<T>> {
    const cacheKey = this.generateCacheKey(queryKey, options);

    // Check cache first
    const cached = this.cache.get(cacheKey);
    if (cached && this.isCacheValid(cached)) {
      return cached;
    }

    // Fetch from source
    const result = await fetchFunction(options);

    // Cache the result
    this.cacheResult(cacheKey, result);

    return result;
  }

  /**
   * Generate cache key for pagination options
   */
  private generateCacheKey(
    queryKey: string,
    options: PaginationOptions
  ): string {
    const filtersStr = options.filters ? JSON.stringify(options.filters) : "";
    return `${queryKey}:${options.page}:${options.limit}:${options.sortBy}:${options.sortOrder}:${filtersStr}`;
  }

  /**
   * Check if cache is still valid
   */
  private isCacheValid(result: PaginatedResult<T>): boolean {
    // Add timestamp to result for cache validation
    return Date.now() - (result as any).timestamp < this.cacheTTL;
  }

  /**
   * Cache paginated result
   */
  private cacheResult(key: string, result: PaginatedResult<T>) {
    // Add timestamp
    (result as any).timestamp = Date.now();

    // Manage cache size
    if (this.cache.size >= this.maxCacheSize) {
      const firstKey = this.cache.keys().next().value;
      this.cache.delete(firstKey!);
    }

    this.cache.set(key, result);
  }

  /**
   * Clear cache for specific query
   */
  clearCache(queryKey: string) {
    const keysToDelete = Array.from(this.cache.keys()).filter((key) =>
      key.startsWith(queryKey)
    );
    keysToDelete.forEach((key) => this.cache.delete(key));
  }

  /**
   * Clear all cache
   */
  clearAllCache() {
    this.cache.clear();
  }
}

// ==================== VIRTUAL SCROLLING ====================

export interface VirtualScrollOptions {
  itemHeight: number;
  containerHeight: number;
  overscan?: number; // Extra items to render outside viewport
}

export interface VirtualScrollResult {
  startIndex: number;
  endIndex: number;
  totalHeight: number;
  offsetY: number;
}

export class VirtualScrollManager {
  /**
   * Calculate visible range for virtual scrolling
   */
  calculateVisibleRange(
    scrollTop: number,
    totalItems: number,
    options: VirtualScrollOptions
  ): VirtualScrollResult {
    const { itemHeight, containerHeight, overscan = 5 } = options;

    const startIndex = Math.max(
      0,
      Math.floor(scrollTop / itemHeight) - overscan
    );
    const endIndex = Math.min(
      totalItems - 1,
      Math.ceil((scrollTop + containerHeight) / itemHeight) + overscan
    );

    const totalHeight = totalItems * itemHeight;
    const offsetY = startIndex * itemHeight;

    return {
      startIndex,
      endIndex,
      totalHeight,
      offsetY,
    };
  }

  /**
   * Get visible items for rendering
   */
  getVisibleItems<T>(
    items: T[],
    scrollTop: number,
    options: VirtualScrollOptions
  ): { items: T[]; offsetY: number } {
    const { startIndex, endIndex, offsetY } = this.calculateVisibleRange(
      scrollTop,
      items.length,
      options
    );

    const visibleItems = items.slice(startIndex, endIndex + 1);

    return {
      items: visibleItems,
      offsetY,
    };
  }
}

// ==================== MEMORY MANAGEMENT ====================

export class MemoryManager {
  private static instance: MemoryManager;
  private memoryUsage: Map<string, number> = new Map();
  private readonly maxMemoryUsage: number = 100 * 1024 * 1024; // 100MB

  static getInstance(): MemoryManager {
    if (!MemoryManager.instance) {
      MemoryManager.instance = new MemoryManager();
    }
    return MemoryManager.instance;
  }

  /**
   * Track memory usage for a component
   */
  trackMemoryUsage(componentId: string, usage: number) {
    this.memoryUsage.set(componentId, usage);

    // Check if we need to clean up
    if (this.getTotalMemoryUsage() > this.maxMemoryUsage) {
      this.cleanupMemory();
    }
  }

  /**
   * Get total memory usage
   */
  getTotalMemoryUsage(): number {
    return Array.from(this.memoryUsage.values()).reduce(
      (sum, usage) => sum + usage,
      0
    );
  }

  /**
   * Clean up memory by removing least recently used components
   */
  private cleanupMemory() {
    // Sort by memory usage (descending)
    const sortedUsage = Array.from(this.memoryUsage.entries()).sort(
      ([, a], [, b]) => b - a
    );

    // Remove top 20% of memory consumers
    const toRemove = Math.ceil(sortedUsage.length * 0.2);
    for (let i = 0; i < toRemove; i++) {
      const [componentId] = sortedUsage[i];
      this.memoryUsage.delete(componentId);

      // Trigger cleanup event
      this.dispatchCleanupEvent(componentId);
    }
  }

  /**
   * Dispatch cleanup event for component
   */
  private dispatchCleanupEvent(componentId: string) {
    window.dispatchEvent(
      new CustomEvent("memory-cleanup", {
        detail: { componentId },
      })
    );
  }

  /**
   * Get memory usage report
   */
  getMemoryReport(): Record<string, any> {
    return {
      totalUsage: this.getTotalMemoryUsage(),
      maxUsage: this.maxMemoryUsage,
      usagePercentage: (this.getTotalMemoryUsage() / this.maxMemoryUsage) * 100,
      components: Object.fromEntries(this.memoryUsage),
    };
  }
}

// ==================== CONNECTION POOLING ====================

export class ConnectionPool {
  private connections: Map<string, WebSocket> = new Map();
  private connectionQueue: Map<string, Promise<WebSocket>> = new Map();
  // private readonly _maxConnections: number = 10;

  /**
   * Get or create WebSocket connection
   */
  async getConnection(url: string): Promise<WebSocket> {
    // Check if connection already exists
    const existingConnection = this.connections.get(url);
    if (
      existingConnection &&
      existingConnection.readyState === WebSocket.OPEN
    ) {
      return existingConnection;
    }

    // Check if connection is being created
    const pendingConnection = this.connectionQueue.get(url);
    if (pendingConnection) {
      return pendingConnection;
    }

    // Create new connection
    const connectionPromise = this.createConnection(url);
    this.connectionQueue.set(url, connectionPromise);

    try {
      const connection = await connectionPromise;
      this.connections.set(url, connection);
      this.connectionQueue.delete(url);
      return connection;
    } catch (error) {
      this.connectionQueue.delete(url);
      throw error;
    }
  }

  /**
   * Create new WebSocket connection
   */
  private createConnection(url: string): Promise<WebSocket> {
    return new Promise((resolve, reject) => {
      const ws = new WebSocket(url);

      ws.onopen = () => resolve(ws);
      ws.onerror = (error) => reject(error);

      // Clean up on close
      ws.onclose = () => {
        this.connections.delete(url);
      };
    });
  }

  /**
   * Close all connections
   */
  closeAllConnections() {
    this.connections.forEach((connection) => {
      if (connection.readyState === WebSocket.OPEN) {
        connection.close();
      }
    });
    this.connections.clear();
    this.connectionQueue.clear();
  }

  /**
   * Get connection status
   */
  getConnectionStatus(): Record<string, string> {
    const status: Record<string, string> = {};

    this.connections.forEach((connection, url) => {
      switch (connection.readyState) {
        case WebSocket.CONNECTING:
          status[url] = "connecting";
          break;
        case WebSocket.OPEN:
          status[url] = "open";
          break;
        case WebSocket.CLOSING:
          status[url] = "closing";
          break;
        case WebSocket.CLOSED:
          status[url] = "closed";
          break;
      }
    });

    return status;
  }
}

// ==================== PERFORMANCE MONITORING ====================

export class PerformanceMonitor {
  private metrics: Map<string, number[]> = new Map();
  private readonly maxMetricsPerKey: number = 100;

  /**
   * Record performance metric
   */
  recordMetric(key: string, value: number) {
    if (!this.metrics.has(key)) {
      this.metrics.set(key, []);
    }

    const values = this.metrics.get(key)!;
    values.push(value);

    // Keep only recent metrics
    if (values.length > this.maxMetricsPerKey) {
      values.shift();
    }
  }

  /**
   * Get performance statistics
   */
  getStats(key: string): Record<string, number> | null {
    const values = this.metrics.get(key);
    if (!values || values.length === 0) return null;

    const sorted = [...values].sort((a, b) => a - b);
    const sum = values.reduce((a, b) => a + b, 0);

    return {
      count: values.length,
      min: sorted[0],
      max: sorted[sorted.length - 1],
      avg: sum / values.length,
      median: sorted[Math.floor(sorted.length / 2)],
      p95: sorted[Math.floor(sorted.length * 0.95)],
      p99: sorted[Math.floor(sorted.length * 0.99)],
    };
  }

  /**
   * Get all performance metrics
   */
  getAllStats(): Record<string, Record<string, number>> {
    const allStats: Record<string, Record<string, number>> = {};

    this.metrics.forEach((_, key) => {
      const stats = this.getStats(key);
      if (stats) {
        allStats[key] = stats;
      }
    });

    return allStats;
  }

  /**
   * Clear metrics
   */
  clearMetrics(key?: string) {
    if (key) {
      this.metrics.delete(key);
    } else {
      this.metrics.clear();
    }
  }
}

// ==================== EXPORTS ====================

export const performanceMonitor = new PerformanceMonitor();
export const memoryManager = MemoryManager.getInstance();
export const connectionPool = new ConnectionPool();
export const virtualScrollManager = new VirtualScrollManager();
