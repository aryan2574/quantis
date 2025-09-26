/**
 * Security Manager for Trader Dashboard
 *
 * Integrates with backend security services:
 * - API key management
 * - End-to-end encryption
 * - Advanced RBAC
 * - Real-time risk monitoring
 * - Circuit breaker status
 */

import { ApolloClient } from "@apollo/client";
import {
  CREATE_API_KEY,
  ENCRYPT_DATA,
  DECRYPT_DATA,
  LOG_SECURITY_EVENT,
  RISK_MONITORING_SUBSCRIPTION,
  CIRCUIT_BREAKER_UPDATES_SUBSCRIPTION,
  GET_API_KEY_USAGE,
  GET_SECURITY_DASHBOARD,
  VALIDATE_API_KEY,
} from "../graphql/security";

export class SecurityManager {
  private apolloClient: ApolloClient<any>;
  // private _apiKey: string | null = null;
  // private _encryptionKey: string | null = null;
  private userPermissions: string[] = [];
  private riskLevel: "MINIMAL" | "LOW" | "MEDIUM" | "HIGH" | "CRITICAL" =
    "MINIMAL";

  constructor(apolloClient: ApolloClient<any>) {
    this.apolloClient = apolloClient;
    this.initializeSecurity();
  }

  /**
   * Initialize security features
   */
  private async initializeSecurity() {
    try {
      // Load stored API key
      // this._apiKey = localStorage.getItem("quantis_api_key");

      // Load encryption key
      // this._encryptionKey = localStorage.getItem("quantis_encryption_key");

      // Load user permissions
      const permissions = localStorage.getItem("quantis_permissions");
      this.userPermissions = permissions ? JSON.parse(permissions) : [];

      // Initialize risk monitoring
      await this.initializeRiskMonitoring();

      // Initialize circuit breaker monitoring
      await this.initializeCircuitBreakerMonitoring();
    } catch (error) {
      console.error("Security initialization failed:", error);
    }
  }

  /**
   * Generate and store API key
   */
  async generateApiKey(name: string, permissions: string[]): Promise<string> {
    try {
      const mutation = CREATE_API_KEY;

      const result = await this.apolloClient.mutate({
        mutation,
        variables: {
          input: {
            name,
            permissions,
            rateLimit: 1000,
          },
        },
      });

      const apiKey = result.data.createApiKey.key;

      // Store API key securely
      localStorage.setItem("quantis_api_key", apiKey);
      // this._apiKey = apiKey;

      return apiKey;
    } catch (error) {
      throw new Error(`API key generation failed: ${(error as Error).message}`);
    }
  }

  /**
   * Validate API key
   */
  async validateApiKey(apiKey: string): Promise<boolean> {
    try {
      const query = VALIDATE_API_KEY;

      const result = await this.apolloClient.query({
        query,
        variables: { apiKey },
        fetchPolicy: "network-only",
      });

      const validation = result.data.validateApiKey;

      if (validation.valid) {
        this.userPermissions = validation.permissions;
        localStorage.setItem(
          "quantis_permissions",
          JSON.stringify(validation.permissions)
        );
        return true;
      }

      return false;
    } catch (error) {
      console.error("API key validation failed:", error);
      return false;
    }
  }

  /**
   * Check user permission
   */
  hasPermission(resource: string, action: string): boolean {
    const requiredPermission = `${action}:${resource}`;
    return this.userPermissions.includes(requiredPermission);
  }

  /**
   * Encrypt sensitive data before sending
   */
  async encryptData(data: any): Promise<string> {
    try {
      const mutation = ENCRYPT_DATA;

      const result = await this.apolloClient.mutate({
        mutation,
        variables: { data: JSON.stringify(data) },
      });

      return JSON.stringify(result.data.encryptData);
    } catch (error) {
      throw new Error(`Data encryption failed: ${(error as Error).message}`);
    }
  }

  /**
   * Decrypt sensitive data after receiving
   */
  async decryptData(encryptedData: string): Promise<any> {
    try {
      const mutation = DECRYPT_DATA;

      const result = await this.apolloClient.mutate({
        mutation,
        variables: { encryptedData },
      });

      return JSON.parse(result.data.decryptData.decryptedData);
    } catch (error) {
      throw new Error(`Data decryption failed: ${(error as Error).message}`);
    }
  }

  /**
   * Initialize risk monitoring
   */
  private async initializeRiskMonitoring() {
    try {
      // Subscribe to real-time risk updates
      const subscription = RISK_MONITORING_SUBSCRIPTION;

      this.apolloClient
        .subscribe({
          query: subscription,
        })
        .subscribe({
          next: (result) => {
            const riskUpdate = result.data.riskUpdates;
            this.handleRiskUpdate(riskUpdate);
          },
          error: (error) => {
            console.error("Risk monitoring subscription error:", error);
          },
        });
    } catch (error) {
      console.error("Risk monitoring initialization failed:", error);
    }
  }

  /**
   * Handle risk update
   */
  private handleRiskUpdate(riskUpdate: any) {
    this.riskLevel = riskUpdate.riskLevel;

    // Show risk alerts if needed
    if (
      riskUpdate.riskLevel === "HIGH" ||
      riskUpdate.riskLevel === "CRITICAL"
    ) {
      this.showRiskAlert(riskUpdate);
    }

    // Update UI with risk information
    this.updateRiskDisplay(riskUpdate);
  }

  /**
   * Show risk alert
   */
  private showRiskAlert(riskUpdate: any) {
    const alertMessage = `
      High Risk Alert!
      
      Risk Level: ${riskUpdate.riskLevel}
      Overall Score: ${(riskUpdate.overallRiskScore * 100).toFixed(1)}%
      
      Recommendations:
      ${riskUpdate.recommendations.map((rec: string) => `• ${rec}`).join("\n")}
    `;

    // Show browser alert (in production, use a proper notification system)
    alert(alertMessage);
  }

  /**
   * Update risk display in UI
   */
  private updateRiskDisplay(riskUpdate: any) {
    // Update risk indicator in the UI
    const riskIndicator = document.getElementById("risk-indicator");
    if (riskIndicator) {
      riskIndicator.textContent = riskUpdate.riskLevel;
      riskIndicator.className = `risk-indicator risk-${riskUpdate.riskLevel.toLowerCase()}`;
    }

    // Update risk score display
    const riskScore = document.getElementById("risk-score");
    if (riskScore) {
      riskScore.textContent = `${(riskUpdate.overallRiskScore * 100).toFixed(
        1
      )}%`;
    }
  }

  /**
   * Initialize circuit breaker monitoring
   */
  private async initializeCircuitBreakerMonitoring() {
    try {
      // Subscribe to circuit breaker updates
      const subscription = CIRCUIT_BREAKER_UPDATES_SUBSCRIPTION;

      this.apolloClient
        .subscribe({
          query: subscription,
        })
        .subscribe({
          next: (result) => {
            const circuitBreakerUpdate = result.data.circuitBreakerUpdates;
            this.handleCircuitBreakerUpdate(circuitBreakerUpdate);
          },
          error: (error) => {
            console.error(
              "Circuit breaker monitoring subscription error:",
              error
            );
          },
        });
    } catch (error) {
      console.error("Circuit breaker monitoring initialization failed:", error);
    }
  }

  /**
   * Handle circuit breaker update
   */
  private handleCircuitBreakerUpdate(update: any) {
    if (update.state === "OPEN") {
      this.showCircuitBreakerAlert(update);
    }
  }

  /**
   * Show circuit breaker alert
   */
  private showCircuitBreakerAlert(update: any) {
    const alertMessage = `
      Trading Halted!
      
      Type: ${update.type}
      Reason: ${update.reason}
      
      Trading has been temporarily suspended. Please wait for further updates.
    `;

    alert(alertMessage);
  }

  /**
   * Get current risk level
   */
  getCurrentRiskLevel(): string {
    return this.riskLevel;
  }

  /**
   * Get user permissions
   */
  getUserPermissions(): string[] {
    return this.userPermissions;
  }

  /**
   * Check if user can perform action
   */
  canPerformAction(resource: string, action: string): boolean {
    return this.hasPermission(resource, action);
  }

  /**
   * Get API key usage statistics
   */
  async getApiKeyUsage(period: string = "24h"): Promise<any> {
    try {
      const query = GET_API_KEY_USAGE;

      const result = await this.apolloClient.query({
        query,
        variables: { period },
      });

      return result.data.getApiKeyUsage;
    } catch (error) {
      throw new Error(
        `Failed to get API key usage: ${(error as Error).message}`
      );
    }
  }

  /**
   * Get security dashboard data
   */
  async getSecurityDashboard(): Promise<any> {
    try {
      const query = GET_SECURITY_DASHBOARD;

      const result = await this.apolloClient.query({
        query,
        fetchPolicy: "network-only",
      });

      return result.data.getSecurityDashboard;
    } catch (error) {
      throw new Error(
        `Failed to get security dashboard: ${(error as Error).message}`
      );
    }
  }

  /**
   * Log security event
   */
  async logSecurityEvent(eventType: string, details: any): Promise<void> {
    try {
      const mutation = LOG_SECURITY_EVENT;

      await this.apolloClient.mutate({
        mutation,
        variables: {
          eventType,
          details: JSON.stringify(details),
        },
      });
    } catch (error) {
      console.error("Security event logging failed:", error);
    }
  }

  /**
   * Clear security data
   */
  clearSecurityData(): void {
    localStorage.removeItem("quantis_api_key");
    localStorage.removeItem("quantis_encryption_key");
    localStorage.removeItem("quantis_permissions");

    // this._apiKey = null;
    // this._encryptionKey = null;
    this.userPermissions = [];
    this.riskLevel = "MINIMAL";
  }
}

// Export singleton instance - will be initialized with Apollo client when available
export const securityManager = new SecurityManager(null as any);
