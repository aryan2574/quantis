import React, { useState, useEffect } from "react";
import { useQuery, useMutation } from "@apollo/client";
import { securityManager } from "../lib/security/security-manager";
import {
  GET_SECURITY_DASHBOARD,
  GET_RISK_DASHBOARD,
  GET_CIRCUIT_BREAKER_STATUS,
  CREATE_API_KEY,
  // DELETE_API_KEY,
} from "../lib/graphql/security";

/**
 * Security Dashboard Component
 *
 * Displays:
 * - Real-time risk monitoring
 * - API key management
 * - Permission status
 * - Security alerts
 * - Compliance status
 * - Circuit breaker status
 */

const SecurityDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<
    "overview" | "api-keys" | "permissions" | "risk"
  >("overview");
  const [securityData, setSecurityData] = useState<any>(null);
  const [riskData, setRiskData] = useState<any>(null);
  const [apiKeyUsage, setApiKeyUsage] = useState<any>(null);

  // Queries
  const { data: securityDashboardData, loading: securityLoading } = useQuery(
    GET_SECURITY_DASHBOARD,
    {
      pollInterval: 5000, // Poll every 5 seconds
    }
  );

  const { data: riskDashboardData, loading: riskLoading } = useQuery(
    GET_RISK_DASHBOARD,
    {
      pollInterval: 2000, // Poll every 2 seconds for real-time updates
    }
  );

  const { data: circuitBreakerData, loading: circuitBreakerLoading } = useQuery(
    GET_CIRCUIT_BREAKER_STATUS,
    {
      pollInterval: 1000, // Poll every second for circuit breaker status
    }
  );

  // Mutations
  const [createApiKey] = useMutation(CREATE_API_KEY);
  // const [deleteApiKey] = useMutation(DELETE_API_KEY);

  useEffect(() => {
    if (securityDashboardData) {
      setSecurityData(securityDashboardData.getSecurityDashboard);
    }
  }, [securityDashboardData]);

  useEffect(() => {
    if (riskDashboardData) {
      setRiskData(riskDashboardData.getRiskDashboard);
    }
  }, [riskDashboardData]);

  useEffect(() => {
    // Load API key usage data
    const loadApiKeyUsage = async () => {
      try {
        const usage = await securityManager.getApiKeyUsage("24h");
        setApiKeyUsage(usage);
      } catch (error) {
        console.error("Failed to load API key usage:", error);
      }
    };

    loadApiKeyUsage();
  }, []);

  const handleCreateApiKey = async () => {
    try {
      const name = prompt("Enter API key name:");
      if (!name) return;

      const permissions = [
        "read:portfolio",
        "read:orders",
        "read:trades",
        "read:market_data",
      ];

      const result = await createApiKey({
        variables: {
          input: {
            name,
            permissions,
            rateLimit: 1000,
          },
        },
      });

      const apiKey = result.data.createApiKey.key;
      alert(
        `API Key created successfully!\n\nKey: ${apiKey}\n\nPlease save this key securely. It will not be shown again.`
      );
    } catch (error) {
      alert(`Failed to create API key: ${(error as Error).message}`);
    }
  };

  // const _handleDeleteApiKey = async (keyId: string) => {
  //   if (!confirm("Are you sure you want to delete this API key?")) return;

  //   try {
  //     await deleteApiKey({
  //       variables: { keyId },
  //     });
  //     alert("API key deleted successfully");
  //   } catch (error) {
  //     alert(`Failed to delete API key: ${(error as Error).message}`);
  //   }
  // };

  const getRiskLevelColor = (riskLevel: string) => {
    switch (riskLevel) {
      case "MINIMAL":
        return "text-green-600 bg-green-100";
      case "LOW":
        return "text-blue-600 bg-blue-100";
      case "MEDIUM":
        return "text-yellow-600 bg-yellow-100";
      case "HIGH":
        return "text-orange-600 bg-orange-100";
      case "CRITICAL":
        return "text-red-600 bg-red-100";
      default:
        return "text-gray-600 bg-gray-100";
    }
  };

  const getCircuitBreakerColor = (state: string) => {
    switch (state) {
      case "CLOSED":
        return "text-green-600 bg-green-100";
      case "OPEN":
        return "text-red-600 bg-red-100";
      case "HALF_OPEN":
        return "text-yellow-600 bg-yellow-100";
      default:
        return "text-gray-600 bg-gray-100";
    }
  };

  if (securityLoading || riskLoading || circuitBreakerLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Security Dashboard</h1>
        <p className="text-gray-600 mt-2">
          Monitor security, risk, and access control
        </p>
      </div>

      {/* Tab Navigation */}
      <div className="border-b border-gray-200 mb-6">
        <nav className="-mb-px flex space-x-8">
          {[
            { id: "overview", name: "Overview" },
            { id: "api-keys", name: "API Keys" },
            { id: "permissions", name: "Permissions" },
            { id: "risk", name: "Risk Monitoring" },
          ].map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === tab.id
                  ? "border-blue-500 text-blue-600"
                  : "border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300"
              }`}
            >
              {tab.name}
            </button>
          ))}
        </nav>
      </div>

      {/* Overview Tab */}
      {activeTab === "overview" && (
        <div className="space-y-6">
          {/* Security Status Cards */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="bg-white p-6 rounded-lg shadow">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div
                    className={`w-8 h-8 rounded-full flex items-center justify-center ${getRiskLevelColor(
                      securityData?.riskLevel || "MINIMAL"
                    )}`}
                  >
                    <span className="text-xs font-bold">
                      {securityData?.riskLevel || "MINIMAL"}
                    </span>
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">
                    Risk Level
                  </p>
                  <p className="text-2xl font-semibold text-gray-900">
                    {securityData?.riskScore || 0}%
                  </p>
                </div>
              </div>
            </div>

            <div className="bg-white p-6 rounded-lg shadow">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 rounded-full bg-green-100 flex items-center justify-center">
                    <span className="text-green-600 text-xs font-bold">✓</span>
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">
                    API Key Status
                  </p>
                  <p className="text-2xl font-semibold text-gray-900">
                    {securityData?.apiKeyStatus || "Active"}
                  </p>
                </div>
              </div>
            </div>

            <div className="bg-white p-6 rounded-lg shadow">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center">
                    <span className="text-blue-600 text-xs font-bold">🔒</span>
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">
                    Encryption
                  </p>
                  <p className="text-2xl font-semibold text-gray-900">
                    {securityData?.encryptionStatus || "Enabled"}
                  </p>
                </div>
              </div>
            </div>

            <div className="bg-white p-6 rounded-lg shadow">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center">
                    <span className="text-purple-600 text-xs font-bold">
                      👥
                    </span>
                  </div>
                </div>
                <div className="ml-4">
                  <p className="text-sm font-medium text-gray-500">
                    Permissions
                  </p>
                  <p className="text-2xl font-semibold text-gray-900">
                    {securityData?.permissions?.length || 0}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Circuit Breaker Status */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">
              Circuit Breaker Status
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <div className="text-center">
                <div
                  className={`inline-flex px-3 py-1 rounded-full text-sm font-medium ${getCircuitBreakerColor(
                    circuitBreakerData?.getCircuitBreakerStatus
                      ?.systemCircuitBreaker?.state || "CLOSED"
                  )}`}
                >
                  System:{" "}
                  {circuitBreakerData?.getCircuitBreakerStatus
                    ?.systemCircuitBreaker?.state || "CLOSED"}
                </div>
              </div>
              <div className="text-center">
                <div
                  className={`inline-flex px-3 py-1 rounded-full text-sm font-medium ${getCircuitBreakerColor(
                    circuitBreakerData?.getCircuitBreakerStatus
                      ?.marketCircuitBreaker?.state || "CLOSED"
                  )}`}
                >
                  Market:{" "}
                  {circuitBreakerData?.getCircuitBreakerStatus
                    ?.marketCircuitBreaker?.state || "CLOSED"}
                </div>
              </div>
              <div className="text-center">
                <div className="text-sm text-gray-500">
                  User Breakers:{" "}
                  {
                    Object.keys(
                      circuitBreakerData?.getCircuitBreakerStatus
                        ?.userCircuitBreakers || {}
                    ).length
                  }
                </div>
              </div>
              <div className="text-center">
                <div className="text-sm text-gray-500">
                  Symbol Breakers:{" "}
                  {
                    Object.keys(
                      circuitBreakerData?.getCircuitBreakerStatus
                        ?.symbolCircuitBreakers || {}
                    ).length
                  }
                </div>
              </div>
            </div>
          </div>

          {/* Security Alerts */}
          <div className="bg-white p-6 rounded-lg shadow">
            <h3 className="text-lg font-medium text-gray-900 mb-4">
              Recent Security Alerts
            </h3>
            <div className="space-y-3">
              {securityData?.securityAlerts?.map(
                (alert: any, index: number) => (
                  <div
                    key={index}
                    className="flex items-center justify-between p-3 bg-red-50 border border-red-200 rounded-lg"
                  >
                    <div>
                      <p className="text-sm font-medium text-red-800">
                        {alert.type}
                      </p>
                      <p className="text-sm text-red-600">{alert.message}</p>
                    </div>
                    <span className="text-xs text-red-500">
                      {new Date(alert.timestamp).toLocaleString()}
                    </span>
                  </div>
                )
              ) || (
                <p className="text-gray-500 text-center py-4">
                  No recent security alerts
                </p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* API Keys Tab */}
      {activeTab === "api-keys" && (
        <div className="space-y-6">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-medium text-gray-900">
              API Key Management
            </h3>
            <button
              onClick={handleCreateApiKey}
              className="bg-blue-600 text-white px-4 py-2 rounded-lg hover:bg-blue-700"
            >
              Create API Key
            </button>
          </div>

          {/* API Key Usage Statistics */}
          {apiKeyUsage && (
            <div className="bg-white p-6 rounded-lg shadow">
              <h4 className="text-md font-medium text-gray-900 mb-4">
                Usage Statistics (24h)
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="text-center">
                  <p className="text-2xl font-semibold text-gray-900">
                    {apiKeyUsage.totalRequests}
                  </p>
                  <p className="text-sm text-gray-500">Total Requests</p>
                </div>
                <div className="text-center">
                  <p className="text-2xl font-semibold text-green-600">
                    {apiKeyUsage.successfulRequests}
                  </p>
                  <p className="text-sm text-gray-500">Successful</p>
                </div>
                <div className="text-center">
                  <p className="text-2xl font-semibold text-red-600">
                    {apiKeyUsage.failedRequests}
                  </p>
                  <p className="text-sm text-gray-500">Failed</p>
                </div>
              </div>
              <div className="mt-4">
                <p className="text-sm text-gray-500">
                  Error Rate: {apiKeyUsage.errorRate?.toFixed(2)}%
                </p>
                <p className="text-sm text-gray-500">
                  Average Response Time:{" "}
                  {apiKeyUsage.averageResponseTime?.toFixed(2)}ms
                </p>
              </div>
            </div>
          )}

          {/* API Keys List */}
          <div className="bg-white rounded-lg shadow">
            <div className="px-6 py-4 border-b border-gray-200">
              <h4 className="text-md font-medium text-gray-900">
                Your API Keys
              </h4>
            </div>
            <div className="p-6">
              <p className="text-gray-500 text-center py-4">
                API keys will be displayed here
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Permissions Tab */}
      {activeTab === "permissions" && (
        <div className="space-y-6">
          <h3 className="text-lg font-medium text-gray-900">
            Your Permissions
          </h3>

          <div className="bg-white p-6 rounded-lg shadow">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {securityData?.permissions?.map(
                (permission: string, index: number) => (
                  <div
                    key={index}
                    className="flex items-center p-3 bg-green-50 border border-green-200 rounded-lg"
                  >
                    <div className="flex-shrink-0">
                      <div className="w-6 h-6 rounded-full bg-green-100 flex items-center justify-center">
                        <span className="text-green-600 text-xs font-bold">
                          ✓
                        </span>
                      </div>
                    </div>
                    <div className="ml-3">
                      <p className="text-sm font-medium text-green-800">
                        {permission}
                      </p>
                    </div>
                  </div>
                )
              ) || (
                <p className="text-gray-500 col-span-2 text-center py-4">
                  No permissions found
                </p>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Risk Monitoring Tab */}
      {activeTab === "risk" && (
        <div className="space-y-6">
          <h3 className="text-lg font-medium text-gray-900">
            Real-time Risk Monitoring
          </h3>

          {/* System Risk Status */}
          {riskData?.systemRiskStatus && (
            <div className="bg-white p-6 rounded-lg shadow">
              <h4 className="text-md font-medium text-gray-900 mb-4">
                System Risk Status
              </h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="text-center">
                  <p className="text-2xl font-semibold text-gray-900">
                    {riskData.systemRiskStatus.totalUsers}
                  </p>
                  <p className="text-sm text-gray-500">Total Users</p>
                </div>
                <div className="text-center">
                  <p className="text-2xl font-semibold text-red-600">
                    {riskData.systemRiskStatus.highRiskUsers}
                  </p>
                  <p className="text-sm text-gray-500">High Risk Users</p>
                </div>
                <div className="text-center">
                  <p className="text-2xl font-semibold text-gray-900">
                    {(riskData.systemRiskStatus.systemRiskScore * 100).toFixed(
                      1
                    )}
                    %
                  </p>
                  <p className="text-sm text-gray-500">System Risk Score</p>
                </div>
              </div>
            </div>
          )}

          {/* High Risk Users */}
          {riskData?.highRiskUsers && riskData.highRiskUsers.length > 0 && (
            <div className="bg-white p-6 rounded-lg shadow">
              <h4 className="text-md font-medium text-gray-900 mb-4">
                High Risk Users
              </h4>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        User ID
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Risk Level
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Overall Score
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Fraud Score
                      </th>
                      <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                        Last Trade
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {riskData.highRiskUsers.map((user: any, index: number) => (
                      <tr key={index}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {user.userId}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span
                            className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getRiskLevelColor(
                              user.riskLevel
                            )}`}
                          >
                            {user.riskLevel}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {(user.overallRiskScore * 100).toFixed(1)}%
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {(user.fraudRiskScore * 100).toFixed(1)}%
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {new Date(user.lastTradeTime).toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}

          {/* Recent Alerts */}
          {riskData?.recentAlerts && riskData.recentAlerts.length > 0 && (
            <div className="bg-white p-6 rounded-lg shadow">
              <h4 className="text-md font-medium text-gray-900 mb-4">
                Recent Risk Alerts
              </h4>
              <div className="space-y-3">
                {riskData.recentAlerts.map((alert: any, index: number) => (
                  <div
                    key={index}
                    className="flex items-center justify-between p-3 bg-yellow-50 border border-yellow-200 rounded-lg"
                  >
                    <div>
                      <p className="text-sm font-medium text-yellow-800">
                        {alert.alertType}
                      </p>
                      <p className="text-sm text-yellow-600">{alert.message}</p>
                    </div>
                    <span className="text-xs text-yellow-500">
                      {new Date(alert.timestamp).toLocaleString()}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default SecurityDashboard;
