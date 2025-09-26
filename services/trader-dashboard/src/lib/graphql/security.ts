import { gql } from "@apollo/client";

// ==================== SECURITY QUERIES ====================

export const GET_SECURITY_DASHBOARD = gql`
  query GetSecurityDashboard {
    getSecurityDashboard {
      riskLevel
      riskScore
      permissions
      apiKeyStatus
      encryptionStatus
      lastLogin
      securityAlerts
      complianceStatus
    }
  }
`;

export const GET_RISK_DASHBOARD = gql`
  query GetRiskDashboard {
    getRiskDashboard {
      systemRiskStatus {
        totalUsers
        highRiskUsers
        systemRiskScore
        marketVolatility
        riskLevel
        lastUpdated
      }
      marketRiskMetrics {
        symbol
        riskLevel
        volatility
        volume
        priceChange
        lastUpdated
      }
      highRiskUsers {
        userId
        riskLevel
        overallRiskScore
        fraudRiskScore
        portfolioRiskScore
        lastTradeTime
      }
      recentAlerts {
        userId
        alertType
        severity
        message
        timestamp
      }
    }
  }
`;

export const GET_CIRCUIT_BREAKER_STATUS = gql`
  query GetCircuitBreakerStatus {
    getCircuitBreakerStatus {
      systemCircuitBreaker {
        state
        triggerTime
        triggerReason
        failureCount
      }
      marketCircuitBreaker {
        state
        triggerTime
        triggerReason
        failureCount
      }
      userCircuitBreakers {
        identifier
        state
        triggerTime
        triggerReason
      }
      symbolCircuitBreakers {
        identifier
        state
        triggerTime
        triggerReason
      }
    }
  }
`;

export const GET_API_KEY_USAGE = gql`
  query GetApiKeyUsage($keyId: String!) {
    getApiKeyUsage(keyId: $keyId) {
      keyId
      usageCount
      lastUsed
      rateLimitStatus
      permissions
      expiresAt
    }
  }
`;

export const GET_ENCRYPTION_STATUS = gql`
  query GetEncryptionStatus {
    getEncryptionStatus {
      isEnabled
      algorithm
      keyStrength
      lastRotated
      status
    }
  }
`;

export const VALIDATE_API_KEY = gql`
  query ValidateApiKey($apiKey: String!) {
    validateApiKey(apiKey: $apiKey) {
      valid
      userId
      permissions
      rateLimitRemaining
      rateLimitReset
    }
  }
`;

// ==================== SECURITY MUTATIONS ====================

export const CREATE_API_KEY = gql`
  mutation CreateApiKey($input: CreateApiKeyInput!) {
    createApiKey(input: $input) {
      id
      name
      key
      permissions
      rateLimit
      expiresAt
      createdAt
    }
  }
`;

export const DELETE_API_KEY = gql`
  mutation DeleteApiKey($keyId: String!) {
    deleteApiKey(keyId: $keyId) {
      success
      message
    }
  }
`;

export const ENCRYPT_DATA = gql`
  mutation EncryptData($data: String!) {
    encryptData(data: $data) {
      encryptedData
      success
      message
    }
  }
`;

export const DECRYPT_DATA = gql`
  mutation DecryptData($encryptedData: String!) {
    decryptData(encryptedData: $encryptedData) {
      decryptedData
      success
      message
    }
  }
`;

export const UPDATE_PERMISSIONS = gql`
  mutation UpdatePermissions($userId: String!, $permissions: [String!]!) {
    updatePermissions(userId: $userId, permissions: $permissions) {
      success
      message
      permissions
    }
  }
`;

export const LOG_SECURITY_EVENT = gql`
  mutation LogSecurityEvent($eventType: String!, $details: String!) {
    logSecurityEvent(eventType: $eventType, details: $details) {
      success
      eventId
      timestamp
    }
  }
`;

// ==================== SECURITY SUBSCRIPTIONS ====================

export const SECURITY_ALERTS_SUBSCRIPTION = gql`
  subscription SecurityAlerts {
    securityAlerts {
      id
      type
      severity
      message
      timestamp
      userId
      resolved
    }
  }
`;

export const RISK_MONITORING_SUBSCRIPTION = gql`
  subscription RiskMonitoring {
    riskMonitoring {
      userId
      riskLevel
      riskScore
      alerts
      timestamp
    }
  }
`;

export const CIRCUIT_BREAKER_UPDATES_SUBSCRIPTION = gql`
  subscription CircuitBreakerUpdates {
    circuitBreakerUpdates {
      type
      identifier
      state
      reason
      timestamp
    }
  }
`;
