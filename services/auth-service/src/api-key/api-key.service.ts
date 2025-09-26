import {
  Injectable,
  UnauthorizedException,
  BadRequestException,
} from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { JwtService } from "@nestjs/jwt";
import { RedisService } from "../redis/redis.service";
import { ApiKey } from "./entities/api-key.entity";
import { ApiKeyPermission } from "./entities/api-key-permission.entity";
import { CreateApiKeyDto } from "./dto/create-api-key.dto";
import { UpdateApiKeyDto } from "./dto/update-api-key.dto";
import { ApiKeyUsage } from "./entities/api-key-usage.entity";
import * as crypto from "crypto";
import * as bcrypt from "bcrypt";

/**
 * Advanced API Key Management Service
 *
 * Provides:
 * - API key generation and validation
 * - Permission-based access control
 * - Rate limiting per API key
 * - Usage tracking and analytics
 * - Key rotation and expiration
 * - Security monitoring
 */
@Injectable()
export class ApiKeyService {
  constructor(
    @InjectRepository(ApiKey)
    private apiKeyRepository: Repository<ApiKey>,
    @InjectRepository(ApiKeyPermission)
    private permissionRepository: Repository<ApiKeyPermission>,
    @InjectRepository(ApiKeyUsage)
    private usageRepository: Repository<ApiKeyUsage>,
    private jwtService: JwtService,
    private redisService: RedisService
  ) {}

  /**
   * Create a new API key
   */
  async createApiKey(
    userId: string,
    createApiKeyDto: CreateApiKeyDto
  ): Promise<ApiKeyResponse> {
    try {
      // Validate permissions
      await this.validatePermissions(createApiKeyDto.permissions);

      // Generate API key
      const apiKey = this.generateApiKey();
      const hashedKey = await this.hashApiKey(apiKey);

      // Create API key entity
      const apiKeyEntity = new ApiKey();
      apiKeyEntity.userId = userId;
      apiKeyEntity.name = createApiKeyDto.name;
      apiKeyEntity.description = createApiKeyDto.description;
      apiKeyEntity.keyHash = hashedKey;
      apiKeyEntity.permissions = createApiKeyDto.permissions;
      apiKeyEntity.rateLimit = createApiKeyDto.rateLimit || 1000; // Default 1000 requests per hour
      apiKeyEntity.expiresAt = createApiKeyDto.expiresAt;
      apiKeyEntity.isActive = true;
      apiKeyEntity.createdAt = new Date();
      apiKeyEntity.lastUsedAt = null;

      // Save to database
      const savedApiKey = await this.apiKeyRepository.save(apiKeyEntity);

      // Create permissions
      await this.createPermissions(savedApiKey.id, createApiKeyDto.permissions);

      // Cache API key info
      await this.cacheApiKeyInfo(savedApiKey.id, savedApiKey);

      // Return response (only return the key once)
      return {
        id: savedApiKey.id,
        name: savedApiKey.name,
        key: apiKey, // Only returned once
        permissions: createApiKeyDto.permissions,
        rateLimit: savedApiKey.rateLimit,
        expiresAt: savedApiKey.expiresAt,
        createdAt: savedApiKey.createdAt,
      };
    } catch (error) {
      throw new BadRequestException(
        `Failed to create API key: ${error.message}`
      );
    }
  }

  /**
   * Validate API key
   */
  async validateApiKey(
    apiKey: string,
    requiredPermission?: string
  ): Promise<ApiKeyValidationResult> {
    try {
      // Get API key info from cache first
      let apiKeyInfo = await this.getCachedApiKeyInfo(apiKey);

      if (!apiKeyInfo) {
        // If not in cache, find in database
        apiKeyInfo = await this.findApiKeyByHash(apiKey);
        if (!apiKeyInfo) {
          throw new UnauthorizedException("Invalid API key");
        }

        // Cache the info
        await this.cacheApiKeyInfo(apiKeyInfo.id, apiKeyInfo);
      }

      // Check if API key is active
      if (!apiKeyInfo.isActive) {
        throw new UnauthorizedException("API key is inactive");
      }

      // Check expiration
      if (apiKeyInfo.expiresAt && apiKeyInfo.expiresAt < new Date()) {
        throw new UnauthorizedException("API key has expired");
      }

      // Check rate limit
      const rateLimitResult = await this.checkRateLimit(
        apiKeyInfo.id,
        apiKeyInfo.rateLimit
      );
      if (!rateLimitResult.allowed) {
        throw new UnauthorizedException(
          `Rate limit exceeded: ${rateLimitResult.message}`
        );
      }

      // Check permission if required
      if (requiredPermission) {
        const hasPermission = await this.checkPermission(
          apiKeyInfo.id,
          requiredPermission
        );
        if (!hasPermission) {
          throw new UnauthorizedException(
            `Insufficient permissions: ${requiredPermission} required`
          );
        }
      }

      // Update last used timestamp
      await this.updateLastUsed(apiKeyInfo.id);

      // Record usage
      await this.recordUsage(apiKeyInfo.id, apiKeyInfo.userId);

      return {
        valid: true,
        userId: apiKeyInfo.userId,
        permissions: apiKeyInfo.permissions,
        rateLimitRemaining: rateLimitResult.remaining,
        rateLimitReset: rateLimitResult.resetTime,
      };
    } catch (error) {
      return {
        valid: false,
        error: error.message,
      };
    }
  }

  /**
   * Get user's API keys
   */
  async getUserApiKeys(userId: string): Promise<ApiKeySummary[]> {
    const apiKeys = await this.apiKeyRepository.find({
      where: { userId },
      select: [
        "id",
        "name",
        "description",
        "permissions",
        "rateLimit",
        "expiresAt",
        "isActive",
        "createdAt",
        "lastUsedAt",
      ],
    });

    return apiKeys.map((key) => ({
      id: key.id,
      name: key.name,
      description: key.description,
      permissions: key.permissions,
      rateLimit: key.rateLimit,
      expiresAt: key.expiresAt,
      isActive: key.isActive,
      createdAt: key.createdAt,
      lastUsedAt: key.lastUsedAt,
    }));
  }

  /**
   * Update API key
   */
  async updateApiKey(
    userId: string,
    keyId: string,
    updateApiKeyDto: UpdateApiKeyDto
  ): Promise<void> {
    const apiKey = await this.apiKeyRepository.findOne({
      where: { id: keyId, userId },
    });

    if (!apiKey) {
      throw new BadRequestException("API key not found");
    }

    // Update fields
    if (updateApiKeyDto.name) apiKey.name = updateApiKeyDto.name;
    if (updateApiKeyDto.description)
      apiKey.description = updateApiKeyDto.description;
    if (updateApiKeyDto.rateLimit) apiKey.rateLimit = updateApiKeyDto.rateLimit;
    if (updateApiKeyDto.expiresAt) apiKey.expiresAt = updateApiKeyDto.expiresAt;
    if (updateApiKeyDto.isActive !== undefined)
      apiKey.isActive = updateApiKeyDto.isActive;

    // Update permissions if provided
    if (updateApiKeyDto.permissions) {
      await this.validatePermissions(updateApiKeyDto.permissions);
      apiKey.permissions = updateApiKeyDto.permissions;

      // Update permissions in database
      await this.permissionRepository.delete({ apiKeyId: keyId });
      await this.createPermissions(keyId, updateApiKeyDto.permissions);
    }

    await this.apiKeyRepository.save(apiKey);

    // Update cache
    await this.cacheApiKeyInfo(keyId, apiKey);
  }

  /**
   * Delete API key
   */
  async deleteApiKey(userId: string, keyId: string): Promise<void> {
    const apiKey = await this.apiKeyRepository.findOne({
      where: { id: keyId, userId },
    });

    if (!apiKey) {
      throw new BadRequestException("API key not found");
    }

    // Delete from database
    await this.apiKeyRepository.delete(keyId);
    await this.permissionRepository.delete({ apiKeyId: keyId });

    // Remove from cache
    await this.redisService.del(`api_key:${keyId}`);
  }

  /**
   * Rotate API key
   */
  async rotateApiKey(userId: string, keyId: string): Promise<ApiKeyResponse> {
    const apiKey = await this.apiKeyRepository.findOne({
      where: { id: keyId, userId },
    });

    if (!apiKey) {
      throw new BadRequestException("API key not found");
    }

    // Generate new key
    const newApiKey = this.generateApiKey();
    const hashedKey = await this.hashApiKey(newApiKey);

    // Update the key hash
    apiKey.keyHash = hashedKey;
    apiKey.lastRotatedAt = new Date();
    await this.apiKeyRepository.save(apiKey);

    // Update cache
    await this.cacheApiKeyInfo(keyId, apiKey);

    return {
      id: apiKey.id,
      name: apiKey.name,
      key: newApiKey, // Only returned once
      permissions: apiKey.permissions,
      rateLimit: apiKey.rateLimit,
      expiresAt: apiKey.expiresAt,
      createdAt: apiKey.createdAt,
    };
  }

  /**
   * Get API key usage statistics
   */
  async getApiKeyUsage(
    keyId: string,
    userId: string,
    period: string = "24h"
  ): Promise<ApiKeyUsageStats> {
    const apiKey = await this.apiKeyRepository.findOne({
      where: { id: keyId, userId },
    });

    if (!apiKey) {
      throw new BadRequestException("API key not found");
    }

    const startDate = this.getStartDate(period);
    const usage = await this.usageRepository
      .createQueryBuilder("usage")
      .where("usage.apiKeyId = :keyId", { keyId })
      .andWhere("usage.timestamp >= :startDate", { startDate })
      .getMany();

    const stats = this.calculateUsageStats(usage);

    return {
      apiKeyId: keyId,
      period,
      totalRequests: stats.totalRequests,
      successfulRequests: stats.successfulRequests,
      failedRequests: stats.failedRequests,
      averageResponseTime: stats.averageResponseTime,
      peakRequestsPerHour: stats.peakRequestsPerHour,
      topEndpoints: stats.topEndpoints,
      errorRate: stats.errorRate,
    };
  }

  /**
   * Get API key analytics
   */
  async getApiKeyAnalytics(
    userId: string,
    period: string = "7d"
  ): Promise<ApiKeyAnalytics> {
    const startDate = this.getStartDate(period);

    const apiKeys = await this.apiKeyRepository.find({
      where: { userId },
    });

    const analytics = {
      totalApiKeys: apiKeys.length,
      activeApiKeys: apiKeys.filter((key) => key.isActive).length,
      expiredApiKeys: apiKeys.filter(
        (key) => key.expiresAt && key.expiresAt < new Date()
      ).length,
      totalRequests: 0,
      totalErrors: 0,
      averageResponseTime: 0,
      topApiKeys: [],
      usageTrend: [],
      errorTrend: [],
    };

    // Calculate analytics for each API key
    for (const apiKey of apiKeys) {
      const usage = await this.usageRepository
        .createQueryBuilder("usage")
        .where("usage.apiKeyId = :keyId", { keyId: apiKey.id })
        .andWhere("usage.timestamp >= :startDate", { startDate })
        .getMany();

      const stats = this.calculateUsageStats(usage);

      analytics.totalRequests += stats.totalRequests;
      analytics.totalErrors += stats.failedRequests;
      analytics.averageResponseTime += stats.averageResponseTime;

      analytics.topApiKeys.push({
        id: apiKey.id,
        name: apiKey.name,
        requests: stats.totalRequests,
        errors: stats.failedRequests,
        errorRate: stats.errorRate,
      });
    }

    // Sort top API keys by requests
    analytics.topApiKeys.sort((a, b) => b.requests - a.requests);
    analytics.topApiKeys = analytics.topApiKeys.slice(0, 10);

    // Calculate averages
    if (apiKeys.length > 0) {
      analytics.averageResponseTime /= apiKeys.length;
    }

    return analytics;
  }

  /**
   * Generate API key
   */
  private generateApiKey(): string {
    const prefix = "qk_"; // Quantis Key prefix
    const randomBytes = crypto.randomBytes(32);
    const key = randomBytes.toString("hex");
    return `${prefix}${key}`;
  }

  /**
   * Hash API key for storage
   */
  private async hashApiKey(apiKey: string): Promise<string> {
    return await bcrypt.hash(apiKey, 12);
  }

  /**
   * Find API key by hash
   */
  private async findApiKeyByHash(apiKey: string): Promise<ApiKey | null> {
    const apiKeys = await this.apiKeyRepository.find();

    for (const key of apiKeys) {
      const isValid = await bcrypt.compare(apiKey, key.keyHash);
      if (isValid) {
        return key;
      }
    }

    return null;
  }

  /**
   * Validate permissions
   */
  private async validatePermissions(permissions: string[]): Promise<void> {
    const validPermissions = [
      "read:portfolio",
      "write:portfolio",
      "read:orders",
      "write:orders",
      "read:trades",
      "read:market_data",
      "read:analytics",
      "write:analytics",
      "admin:users",
      "admin:system",
    ];

    for (const permission of permissions) {
      if (!validPermissions.includes(permission)) {
        throw new BadRequestException(`Invalid permission: ${permission}`);
      }
    }
  }

  /**
   * Create permissions
   */
  private async createPermissions(
    apiKeyId: string,
    permissions: string[]
  ): Promise<void> {
    const permissionEntities = permissions.map((permission) => {
      const entity = new ApiKeyPermission();
      entity.apiKeyId = apiKeyId;
      entity.permission = permission;
      return entity;
    });

    await this.permissionRepository.save(permissionEntities);
  }

  /**
   * Check permission
   */
  private async checkPermission(
    apiKeyId: string,
    requiredPermission: string
  ): Promise<boolean> {
    const permission = await this.permissionRepository.findOne({
      where: { apiKeyId, permission: requiredPermission },
    });

    return !!permission;
  }

  /**
   * Check rate limit
   */
  private async checkRateLimit(
    apiKeyId: string,
    rateLimit: number
  ): Promise<RateLimitResult> {
    const key = `rate_limit:${apiKeyId}`;
    const now = Date.now();
    const window = 3600000; // 1 hour in milliseconds

    // Get current count
    const current = (await this.redisService.get(key)) || "0";
    const count = parseInt(current);

    if (count >= rateLimit) {
      return {
        allowed: false,
        remaining: 0,
        resetTime: new Date(now + window),
        message: `Rate limit exceeded. Limit: ${rateLimit} requests per hour`,
      };
    }

    // Increment counter
    await this.redisService.incr(key);
    await this.redisService.expire(key, Math.ceil(window / 1000));

    return {
      allowed: true,
      remaining: rateLimit - count - 1,
      resetTime: new Date(now + window),
      message: "Rate limit OK",
    };
  }

  /**
   * Cache API key info
   */
  private async cacheApiKeyInfo(
    apiKeyId: string,
    apiKey: ApiKey
  ): Promise<void> {
    const key = `api_key:${apiKeyId}`;
    const data = {
      id: apiKey.id,
      userId: apiKey.userId,
      name: apiKey.name,
      permissions: apiKey.permissions,
      rateLimit: apiKey.rateLimit,
      expiresAt: apiKey.expiresAt,
      isActive: apiKey.isActive,
    };

    await this.redisService.setex(key, 3600, JSON.stringify(data)); // Cache for 1 hour
  }

  /**
   * Get cached API key info
   */
  private async getCachedApiKeyInfo(apiKey: string): Promise<any> {
    // This is a simplified implementation
    // In practice, you'd need to hash the API key and look it up
    return null;
  }

  /**
   * Update last used timestamp
   */
  private async updateLastUsed(apiKeyId: string): Promise<void> {
    await this.apiKeyRepository.update(apiKeyId, {
      lastUsedAt: new Date(),
    });
  }

  /**
   * Record usage
   */
  private async recordUsage(apiKeyId: string, userId: string): Promise<void> {
    const usage = new ApiKeyUsage();
    usage.apiKeyId = apiKeyId;
    usage.userId = userId;
    usage.timestamp = new Date();
    usage.endpoint = "unknown"; // Would be set by the actual endpoint
    usage.responseTime = 0; // Would be measured
    usage.statusCode = 200; // Would be set by the actual response
    usage.success = true;

    await this.usageRepository.save(usage);
  }

  /**
   * Get start date for period
   */
  private getStartDate(period: string): Date {
    const now = new Date();
    switch (period) {
      case "1h":
        return new Date(now.getTime() - 3600000);
      case "24h":
        return new Date(now.getTime() - 86400000);
      case "7d":
        return new Date(now.getTime() - 604800000);
      case "30d":
        return new Date(now.getTime() - 2592000000);
      default:
        return new Date(now.getTime() - 86400000);
    }
  }

  /**
   * Calculate usage statistics
   */
  private calculateUsageStats(usage: ApiKeyUsage[]): any {
    const totalRequests = usage.length;
    const successfulRequests = usage.filter((u) => u.success).length;
    const failedRequests = totalRequests - successfulRequests;
    const averageResponseTime =
      usage.reduce((sum, u) => sum + u.responseTime, 0) / totalRequests || 0;

    // Group by endpoint
    const endpointCounts = usage.reduce((acc, u) => {
      acc[u.endpoint] = (acc[u.endpoint] || 0) + 1;
      return acc;
    }, {});

    const topEndpoints = Object.entries(endpointCounts)
      .sort(([, a], [, b]) => (b as number) - (a as number))
      .slice(0, 10)
      .map(([endpoint, count]) => ({ endpoint, count }));

    const errorRate =
      totalRequests > 0 ? (failedRequests / totalRequests) * 100 : 0;

    // Calculate peak requests per hour
    const hourlyCounts = usage.reduce((acc, u) => {
      const hour = new Date(u.timestamp).getHours();
      acc[hour] = (acc[hour] || 0) + 1;
      return acc;
    }, {});

    const peakRequestsPerHour = Math.max(
      ...(Object.values(hourlyCounts) as number[])
    );

    return {
      totalRequests,
      successfulRequests,
      failedRequests,
      averageResponseTime,
      topEndpoints,
      errorRate,
      peakRequestsPerHour,
    };
  }
}

// Data Transfer Objects
export interface CreateApiKeyDto {
  name: string;
  description?: string;
  permissions: string[];
  rateLimit?: number;
  expiresAt?: Date;
}

export interface UpdateApiKeyDto {
  name?: string;
  description?: string;
  permissions?: string[];
  rateLimit?: number;
  expiresAt?: Date;
  isActive?: boolean;
}

export interface ApiKeyResponse {
  id: string;
  name: string;
  key: string;
  permissions: string[];
  rateLimit: number;
  expiresAt?: Date;
  createdAt: Date;
}

export interface ApiKeyValidationResult {
  valid: boolean;
  userId?: string;
  permissions?: string[];
  rateLimitRemaining?: number;
  rateLimitReset?: Date;
  error?: string;
}

export interface ApiKeySummary {
  id: string;
  name: string;
  description?: string;
  permissions: string[];
  rateLimit: number;
  expiresAt?: Date;
  isActive: boolean;
  createdAt: Date;
  lastUsedAt?: Date;
}

export interface ApiKeyUsageStats {
  apiKeyId: string;
  period: string;
  totalRequests: number;
  successfulRequests: number;
  failedRequests: number;
  averageResponseTime: number;
  peakRequestsPerHour: number;
  topEndpoints: Array<{ endpoint: string; count: number }>;
  errorRate: number;
}

export interface ApiKeyAnalytics {
  totalApiKeys: number;
  activeApiKeys: number;
  expiredApiKeys: number;
  totalRequests: number;
  totalErrors: number;
  averageResponseTime: number;
  topApiKeys: Array<{
    id: string;
    name: string;
    requests: number;
    errors: number;
    errorRate: number;
  }>;
  usageTrend: any[];
  errorTrend: any[];
}

export interface RateLimitResult {
  allowed: boolean;
  remaining: number;
  resetTime: Date;
  message: string;
}
