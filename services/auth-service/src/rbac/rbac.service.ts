import {
  Injectable,
  UnauthorizedException,
  ForbiddenException,
} from "@nestjs/common";
import { InjectRepository } from "@nestjs/typeorm";
import { Repository } from "typeorm";
import { RedisService } from "../redis/redis.service";
import { User } from "../auth/entities/user.entity";
import { Role } from "./entities/role.entity";
import { Permission } from "./entities/permission.entity";
import { UserRole } from "./entities/user-role.entity";
import { RolePermission } from "./entities/role-permission.entity";
import { Resource } from "./entities/resource.entity";
import { ResourcePermission } from "./entities/resource-permission.entity";

/**
 * Advanced Role-Based Access Control (RBAC) Service
 *
 * Provides:
 * - Hierarchical role management
 * - Fine-grained permissions
 * - Resource-based access control
 * - Dynamic permission evaluation
 * - Context-aware authorization
 * - Audit logging for access control
 */
@Injectable()
export class RBACService {
  constructor(
    @InjectRepository(User)
    private userRepository: Repository<User>,
    @InjectRepository(Role)
    private roleRepository: Repository<Role>,
    @InjectRepository(Permission)
    private permissionRepository: Repository<Permission>,
    @InjectRepository(UserRole)
    private userRoleRepository: Repository<UserRole>,
    @InjectRepository(RolePermission)
    private rolePermissionRepository: Repository<RolePermission>,
    @InjectRepository(Resource)
    private resourceRepository: Repository<Resource>,
    @InjectRepository(ResourcePermission)
    private resourcePermissionRepository: Repository<ResourcePermission>,
    private redisService: RedisService
  ) {}

  /**
   * Check if user has permission to access resource
   */
  async checkPermission(
    userId: string,
    resource: string,
    action: string,
    context?: AccessContext
  ): Promise<PermissionResult> {
    try {
      // Get user roles
      const userRoles = await this.getUserRoles(userId);
      if (userRoles.length === 0) {
        return {
          allowed: false,
          reason: "User has no roles assigned",
          roles: [],
          permissions: [],
        };
      }

      // Get permissions for user roles
      const permissions = await this.getRolePermissions(userRoles);

      // Check direct permissions
      const directPermission = permissions.find(
        (p) => p.resource === resource && p.action === action
      );

      if (directPermission) {
        // Check context-based restrictions
        const contextCheck = await this.checkContextualPermission(
          directPermission,
          context
        );

        if (contextCheck.allowed) {
          await this.logAccessAttempt(
            userId,
            resource,
            action,
            true,
            "Direct permission"
          );
          return {
            allowed: true,
            reason: "Direct permission granted",
            roles: userRoles,
            permissions: [directPermission],
          };
        }
      }

      // Check inherited permissions
      const inheritedPermissions = await this.getInheritedPermissions(
        userRoles,
        resource,
        action
      );
      if (inheritedPermissions.length > 0) {
        const contextCheck = await this.checkContextualPermission(
          inheritedPermissions[0],
          context
        );

        if (contextCheck.allowed) {
          await this.logAccessAttempt(
            userId,
            resource,
            action,
            true,
            "Inherited permission"
          );
          return {
            allowed: true,
            reason: "Inherited permission granted",
            roles: userRoles,
            permissions: inheritedPermissions,
          };
        }
      }

      // Check resource-specific permissions
      const resourcePermissions = await this.getResourcePermissions(
        userId,
        resource,
        action
      );
      if (resourcePermissions.length > 0) {
        const contextCheck = await this.checkContextualPermission(
          resourcePermissions[0],
          context
        );

        if (contextCheck.allowed) {
          await this.logAccessAttempt(
            userId,
            resource,
            action,
            true,
            "Resource-specific permission"
          );
          return {
            allowed: true,
            reason: "Resource-specific permission granted",
            roles: userRoles,
            permissions: resourcePermissions,
          };
        }
      }

      await this.logAccessAttempt(
        userId,
        resource,
        action,
        false,
        "No matching permissions"
      );
      return {
        allowed: false,
        reason: "No matching permissions found",
        roles: userRoles,
        permissions: [],
      };
    } catch (error) {
      await this.logAccessAttempt(
        userId,
        resource,
        action,
        false,
        `Error: ${error.message}`
      );
      throw new UnauthorizedException(
        `Permission check failed: ${error.message}`
      );
    }
  }

  /**
   * Assign role to user
   */
  async assignRoleToUser(
    userId: string,
    roleId: string,
    assignedBy: string
  ): Promise<void> {
    try {
      // Check if user exists
      const user = await this.userRepository.findOne({ where: { id: userId } });
      if (!user) {
        throw new ForbiddenException("User not found");
      }

      // Check if role exists
      const role = await this.roleRepository.findOne({ where: { id: roleId } });
      if (!role) {
        throw new ForbiddenException("Role not found");
      }

      // Check if assignment already exists
      const existingAssignment = await this.userRoleRepository.findOne({
        where: { userId, roleId },
      });

      if (existingAssignment) {
        throw new ForbiddenException("Role already assigned to user");
      }

      // Create user role assignment
      const userRole = new UserRole();
      userRole.userId = userId;
      userRole.roleId = roleId;
      userRole.assignedBy = assignedBy;
      userRole.assignedAt = new Date();
      userRole.isActive = true;

      await this.userRoleRepository.save(userRole);

      // Clear user permissions cache
      await this.clearUserPermissionsCache(userId);

      // Log role assignment
      await this.logRoleAssignment(userId, roleId, assignedBy, "ASSIGNED");
    } catch (error) {
      throw new ForbiddenException(`Role assignment failed: ${error.message}`);
    }
  }

  /**
   * Remove role from user
   */
  async removeRoleFromUser(
    userId: string,
    roleId: string,
    removedBy: string
  ): Promise<void> {
    try {
      const userRole = await this.userRoleRepository.findOne({
        where: { userId, roleId },
      });

      if (!userRole) {
        throw new ForbiddenException("Role assignment not found");
      }

      // Soft delete the assignment
      userRole.isActive = false;
      userRole.removedBy = removedBy;
      userRole.removedAt = new Date();

      await this.userRoleRepository.save(userRole);

      // Clear user permissions cache
      await this.clearUserPermissionsCache(userId);

      // Log role removal
      await this.logRoleAssignment(userId, roleId, removedBy, "REMOVED");
    } catch (error) {
      throw new ForbiddenException(`Role removal failed: ${error.message}`);
    }
  }

  /**
   * Create new role
   */
  async createRole(createRoleDto: CreateRoleDto): Promise<Role> {
    try {
      // Check if role name already exists
      const existingRole = await this.roleRepository.findOne({
        where: { name: createRoleDto.name },
      });

      if (existingRole) {
        throw new ForbiddenException("Role name already exists");
      }

      // Create role
      const role = new Role();
      role.name = createRoleDto.name;
      role.description = createRoleDto.description;
      role.isSystemRole = createRoleDto.isSystemRole || false;
      role.parentRoleId = createRoleDto.parentRoleId;
      role.createdAt = new Date();
      role.createdBy = createRoleDto.createdBy;

      const savedRole = await this.roleRepository.save(role);

      // Assign permissions to role
      if (createRoleDto.permissions && createRoleDto.permissions.length > 0) {
        await this.assignPermissionsToRole(
          savedRole.id,
          createRoleDto.permissions
        );
      }

      return savedRole;
    } catch (error) {
      throw new ForbiddenException(`Role creation failed: ${error.message}`);
    }
  }

  /**
   * Assign permissions to role
   */
  async assignPermissionsToRole(
    roleId: string,
    permissionIds: string[]
  ): Promise<void> {
    try {
      for (const permissionId of permissionIds) {
        // Check if permission exists
        const permission = await this.permissionRepository.findOne({
          where: { id: permissionId },
        });

        if (!permission) {
          throw new ForbiddenException(`Permission not found: ${permissionId}`);
        }

        // Check if assignment already exists
        const existingAssignment = await this.rolePermissionRepository.findOne({
          where: { roleId, permissionId },
        });

        if (!existingAssignment) {
          const rolePermission = new RolePermission();
          rolePermission.roleId = roleId;
          rolePermission.permissionId = permissionId;
          rolePermission.assignedAt = new Date();

          await this.rolePermissionRepository.save(rolePermission);
        }
      }

      // Clear role permissions cache
      await this.clearRolePermissionsCache(roleId);
    } catch (error) {
      throw new ForbiddenException(
        `Permission assignment failed: ${error.message}`
      );
    }
  }

  /**
   * Get user's effective permissions
   */
  async getUserEffectivePermissions(
    userId: string
  ): Promise<EffectivePermissions> {
    try {
      // Get user roles
      const userRoles = await this.getUserRoles(userId);

      // Get permissions for each role
      const rolePermissions = await this.getRolePermissions(userRoles);

      // Get inherited permissions
      const inheritedPermissions =
        await this.getInheritedPermissions(userRoles);

      // Get resource-specific permissions
      const resourcePermissions = await this.getUserResourcePermissions(userId);

      // Combine all permissions
      const allPermissions = [
        ...rolePermissions,
        ...inheritedPermissions,
        ...resourcePermissions,
      ];

      // Remove duplicates
      const uniquePermissions = this.removeDuplicatePermissions(allPermissions);

      return {
        userId,
        roles: userRoles,
        permissions: uniquePermissions,
        totalPermissions: uniquePermissions.length,
        lastUpdated: new Date(),
      };
    } catch (error) {
      throw new UnauthorizedException(
        `Failed to get user permissions: ${error.message}`
      );
    }
  }

  /**
   * Check contextual permission
   */
  private async checkContextualPermission(
    permission: Permission,
    context?: AccessContext
  ): Promise<ContextualPermissionResult> {
    if (!context) {
      return { allowed: true, reason: "No context restrictions" };
    }

    // Check time-based restrictions
    if (permission.timeRestrictions) {
      const timeCheck = this.checkTimeRestrictions(
        permission.timeRestrictions,
        context.timestamp
      );
      if (!timeCheck.allowed) {
        return { allowed: false, reason: timeCheck.reason };
      }
    }

    // Check IP-based restrictions
    if (permission.ipRestrictions && context.ipAddress) {
      const ipCheck = this.checkIPRestrictions(
        permission.ipRestrictions,
        context.ipAddress
      );
      if (!ipCheck.allowed) {
        return { allowed: false, reason: ipCheck.reason };
      }
    }

    // Check location-based restrictions
    if (permission.locationRestrictions && context.location) {
      const locationCheck = this.checkLocationRestrictions(
        permission.locationRestrictions,
        context.location
      );
      if (!locationCheck.allowed) {
        return { allowed: false, reason: locationCheck.reason };
      }
    }

    // Check device-based restrictions
    if (permission.deviceRestrictions && context.deviceInfo) {
      const deviceCheck = this.checkDeviceRestrictions(
        permission.deviceRestrictions,
        context.deviceInfo
      );
      if (!deviceCheck.allowed) {
        return { allowed: false, reason: deviceCheck.reason };
      }
    }

    return { allowed: true, reason: "All context checks passed" };
  }

  /**
   * Get user roles
   */
  private async getUserRoles(userId: string): Promise<Role[]> {
    const cacheKey = `user_roles:${userId}`;
    const cachedRoles = await this.redisService.get(cacheKey);

    if (cachedRoles) {
      return JSON.parse(cachedRoles);
    }

    const userRoles = await this.userRoleRepository
      .createQueryBuilder("ur")
      .leftJoinAndSelect("ur.role", "role")
      .where("ur.userId = :userId", { userId })
      .andWhere("ur.isActive = :isActive", { isActive: true })
      .getMany();

    const roles = userRoles.map((ur) => ur.role);

    // Cache for 5 minutes
    await this.redisService.setex(cacheKey, 300, JSON.stringify(roles));

    return roles;
  }

  /**
   * Get role permissions
   */
  private async getRolePermissions(roles: Role[]): Promise<Permission[]> {
    const roleIds = roles.map((role) => role.id);
    const cacheKey = `role_permissions:${roleIds.join(",")}`;
    const cachedPermissions = await this.redisService.get(cacheKey);

    if (cachedPermissions) {
      return JSON.parse(cachedPermissions);
    }

    const rolePermissions = await this.rolePermissionRepository
      .createQueryBuilder("rp")
      .leftJoinAndSelect("rp.permission", "permission")
      .where("rp.roleId IN (:...roleIds)", { roleIds })
      .getMany();

    const permissions = rolePermissions.map((rp) => rp.permission);

    // Cache for 5 minutes
    await this.redisService.setex(cacheKey, 300, JSON.stringify(permissions));

    return permissions;
  }

  /**
   * Get inherited permissions
   */
  private async getInheritedPermissions(
    roles: Role[],
    resource?: string,
    action?: string
  ): Promise<Permission[]> {
    const inheritedPermissions: Permission[] = [];

    for (const role of roles) {
      if (role.parentRoleId) {
        const parentRole = await this.roleRepository.findOne({
          where: { id: role.parentRoleId },
        });

        if (parentRole) {
          const parentPermissions = await this.getRolePermissions([parentRole]);
          inheritedPermissions.push(...parentPermissions);
        }
      }
    }

    // Filter by resource and action if specified
    if (resource && action) {
      return inheritedPermissions.filter(
        (p) => p.resource === resource && p.action === action
      );
    }

    return inheritedPermissions;
  }

  /**
   * Get resource permissions
   */
  private async getResourcePermissions(
    userId: string,
    resource: string,
    action: string
  ): Promise<Permission[]> {
    const resourcePermissions = await this.resourcePermissionRepository
      .createQueryBuilder("rp")
      .leftJoinAndSelect("rp.permission", "permission")
      .where("rp.userId = :userId", { userId })
      .andWhere("permission.resource = :resource", { resource })
      .andWhere("permission.action = :action", { action })
      .getMany();

    return resourcePermissions.map((rp) => rp.permission);
  }

  /**
   * Get user resource permissions
   */
  private async getUserResourcePermissions(
    userId: string
  ): Promise<Permission[]> {
    const resourcePermissions = await this.resourcePermissionRepository
      .createQueryBuilder("rp")
      .leftJoinAndSelect("rp.permission", "permission")
      .where("rp.userId = :userId", { userId })
      .getMany();

    return resourcePermissions.map((rp) => rp.permission);
  }

  /**
   * Remove duplicate permissions
   */
  private removeDuplicatePermissions(permissions: Permission[]): Permission[] {
    const uniquePermissions = new Map<string, Permission>();

    for (const permission of permissions) {
      const key = `${permission.resource}:${permission.action}`;
      if (!uniquePermissions.has(key)) {
        uniquePermissions.set(key, permission);
      }
    }

    return Array.from(uniquePermissions.values());
  }

  /**
   * Check time restrictions
   */
  private checkTimeRestrictions(
    restrictions: TimeRestrictions,
    timestamp: Date
  ): RestrictionCheckResult {
    const hour = timestamp.getHours();
    const dayOfWeek = timestamp.getDay();

    // Check allowed hours
    if (restrictions.allowedHours && restrictions.allowedHours.length > 0) {
      if (!restrictions.allowedHours.includes(hour)) {
        return {
          allowed: false,
          reason: `Access not allowed at hour ${hour}`,
        };
      }
    }

    // Check allowed days
    if (restrictions.allowedDays && restrictions.allowedDays.length > 0) {
      if (!restrictions.allowedDays.includes(dayOfWeek)) {
        return {
          allowed: false,
          reason: `Access not allowed on day ${dayOfWeek}`,
        };
      }
    }

    return { allowed: true, reason: "Time restrictions passed" };
  }

  /**
   * Check IP restrictions
   */
  private checkIPRestrictions(
    restrictions: IPRestrictions,
    ipAddress: string
  ): RestrictionCheckResult {
    // Check allowed IPs
    if (restrictions.allowedIPs && restrictions.allowedIPs.length > 0) {
      if (!restrictions.allowedIPs.includes(ipAddress)) {
        return {
          allowed: false,
          reason: `IP address ${ipAddress} not allowed`,
        };
      }
    }

    // Check blocked IPs
    if (
      restrictions.blockedIPs &&
      restrictions.blockedIPs.includes(ipAddress)
    ) {
      return {
        allowed: false,
        reason: `IP address ${ipAddress} is blocked`,
      };
    }

    return { allowed: true, reason: "IP restrictions passed" };
  }

  /**
   * Check location restrictions
   */
  private checkLocationRestrictions(
    restrictions: LocationRestrictions,
    location: LocationInfo
  ): RestrictionCheckResult {
    // Check allowed countries
    if (
      restrictions.allowedCountries &&
      restrictions.allowedCountries.length > 0
    ) {
      if (!restrictions.allowedCountries.includes(location.country)) {
        return {
          allowed: false,
          reason: `Country ${location.country} not allowed`,
        };
      }
    }

    // Check blocked countries
    if (
      restrictions.blockedCountries &&
      restrictions.blockedCountries.includes(location.country)
    ) {
      return {
        allowed: false,
        reason: `Country ${location.country} is blocked`,
      };
    }

    return { allowed: true, reason: "Location restrictions passed" };
  }

  /**
   * Check device restrictions
   */
  private checkDeviceRestrictions(
    restrictions: DeviceRestrictions,
    deviceInfo: DeviceInfo
  ): RestrictionCheckResult {
    // Check allowed device types
    if (
      restrictions.allowedDeviceTypes &&
      restrictions.allowedDeviceTypes.length > 0
    ) {
      if (!restrictions.allowedDeviceTypes.includes(deviceInfo.type)) {
        return {
          allowed: false,
          reason: `Device type ${deviceInfo.type} not allowed`,
        };
      }
    }

    // Check allowed browsers
    if (
      restrictions.allowedBrowsers &&
      restrictions.allowedBrowsers.length > 0
    ) {
      if (!restrictions.allowedBrowsers.includes(deviceInfo.browser)) {
        return {
          allowed: false,
          reason: `Browser ${deviceInfo.browser} not allowed`,
        };
      }
    }

    return { allowed: true, reason: "Device restrictions passed" };
  }

  /**
   * Clear user permissions cache
   */
  private async clearUserPermissionsCache(userId: string): Promise<void> {
    await this.redisService.del(`user_roles:${userId}`);
  }

  /**
   * Clear role permissions cache
   */
  private async clearRolePermissionsCache(roleId: string): Promise<void> {
    // This would need to clear all role permission caches
    // Implementation depends on cache key structure
  }

  /**
   * Log access attempt
   */
  private async logAccessAttempt(
    userId: string,
    resource: string,
    action: string,
    allowed: boolean,
    reason: string
  ): Promise<void> {
    const logEntry = {
      userId,
      resource,
      action,
      allowed,
      reason,
      timestamp: new Date().toISOString(),
    };

    // Store in Redis for real-time monitoring
    await this.redisService.lpush("access_logs", JSON.stringify(logEntry));

    // Keep only last 1000 entries
    await this.redisService.ltrim("access_logs", 0, 999);
  }

  /**
   * Log role assignment
   */
  private async logRoleAssignment(
    userId: string,
    roleId: string,
    performedBy: string,
    action: "ASSIGNED" | "REMOVED"
  ): Promise<void> {
    const logEntry = {
      userId,
      roleId,
      performedBy,
      action,
      timestamp: new Date().toISOString(),
    };

    await this.redisService.lpush(
      "role_assignment_logs",
      JSON.stringify(logEntry)
    );
    await this.redisService.ltrim("role_assignment_logs", 0, 999);
  }
}

// Data Transfer Objects
export interface CreateRoleDto {
  name: string;
  description?: string;
  isSystemRole?: boolean;
  parentRoleId?: string;
  permissions?: string[];
  createdBy: string;
}

export interface AccessContext {
  timestamp?: Date;
  ipAddress?: string;
  location?: LocationInfo;
  deviceInfo?: DeviceInfo;
  sessionId?: string;
  requestId?: string;
}

export interface LocationInfo {
  country: string;
  region?: string;
  city?: string;
  coordinates?: {
    latitude: number;
    longitude: number;
  };
}

export interface DeviceInfo {
  type: "desktop" | "mobile" | "tablet";
  browser: string;
  os: string;
  userAgent?: string;
}

export interface PermissionResult {
  allowed: boolean;
  reason: string;
  roles: Role[];
  permissions: Permission[];
}

export interface ContextualPermissionResult {
  allowed: boolean;
  reason: string;
}

export interface EffectivePermissions {
  userId: string;
  roles: Role[];
  permissions: Permission[];
  totalPermissions: number;
  lastUpdated: Date;
}

export interface RestrictionCheckResult {
  allowed: boolean;
  reason: string;
}

export interface TimeRestrictions {
  allowedHours?: number[];
  allowedDays?: number[];
  timezone?: string;
}

export interface IPRestrictions {
  allowedIPs?: string[];
  blockedIPs?: string[];
  allowedRanges?: string[];
}

export interface LocationRestrictions {
  allowedCountries?: string[];
  blockedCountries?: string[];
  allowedRegions?: string[];
}

export interface DeviceRestrictions {
  allowedDeviceTypes?: string[];
  allowedBrowsers?: string[];
  allowedOS?: string[];
}
