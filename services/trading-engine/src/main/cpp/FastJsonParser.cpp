#include "FastJsonParser.h"
#include <cstring>
#include <cstdlib>
#include <iostream>
#include <algorithm>

namespace quantis
{

    FastJsonParser::MarketData FastJsonParser::parseAlphaVantage(const std::string &symbol, const std::string &jsonResponse)
    {
        auto start = std::chrono::high_resolution_clock::now();

        totalParses_.fetch_add(1);

        MarketData data;
        data.symbol = symbol;

        try
        {
            // Alpha Vantage response format:
            // {
            //   "Global Quote": {
            //     "01. symbol": "AAPL",
            //     "02. open": "150.00",
            //     "03. high": "155.00",
            //     "04. low": "148.00",
            //     "05. price": "152.50",
            //     "06. volume": "1000000"
            //   }
            // }

            const char *json = jsonResponse.c_str();

            // Find "Global Quote" section
            const char *globalQuote = strstr(json, "\"Global Quote\"");
            if (!globalQuote)
            {
                throw std::runtime_error("Global Quote not found");
            }

            // Extract values using fast string operations
            data.open = extractDouble(globalQuote, "02. open");
            data.high = extractDouble(globalQuote, "03. high");
            data.low = extractDouble(globalQuote, "04. low");
            data.lastPrice = extractDouble(globalQuote, "05. price");
            data.volume = extractLong(globalQuote, "06. volume");

            // Calculate bid/ask from high/low (simplified)
            data.bestBid = data.low;
            data.bestAsk = data.high;

            data.timestamp = std::chrono::duration_cast<std::chrono::nanoseconds>(
                                 std::chrono::high_resolution_clock::now().time_since_epoch())
                                 .count();
            data.isValid = true;
        }
        catch (const std::exception &e)
        {
            failedParses_.fetch_add(1);
            data.isValid = false;
        }

        auto end = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start);
        totalParseTimeNs_.fetch_add(duration.count());

        return data;
    }

    FastJsonParser::MarketData FastJsonParser::parseAlphaVantageSafe(const std::string &symbol, const std::string &jsonResponse)
    {
        MarketData data = parseAlphaVantage(symbol, jsonResponse);

        // Additional validation
        if (data.isValid)
        {
            if (data.lastPrice <= 0 || data.volume < 0)
            {
                data.isValid = false;
                failedParses_.fetch_add(1);
            }
        }

        return data;
    }

    double FastJsonParser::extractDouble(const char *json, const char *key)
    {
        // Find key in JSON
        std::string keyPattern = "\"" + std::string(key) + "\"";
        const char *keyPos = strstr(json, keyPattern.c_str());
        if (!keyPos)
        {
            throw std::runtime_error("Key not found: " + std::string(key));
        }

        // Find colon after key
        const char *colonPos = strchr(keyPos, ':');
        if (!colonPos)
        {
            throw std::runtime_error("Colon not found after key: " + std::string(key));
        }

        // Skip whitespace after colon
        const char *valueStart = colonPos + 1;
        while (*valueStart == ' ' || *valueStart == '\t' || *valueStart == '\n')
        {
            valueStart++;
        }

        // Find end of value (comma, brace, or bracket)
        const char *valueEnd = valueStart;
        while (*valueEnd && *valueEnd != ',' && *valueEnd != '}' && *valueEnd != ']' && *valueEnd != '"')
        {
            valueEnd++;
        }

        // Convert to double
        std::string valueStr(valueStart, valueEnd - valueStart);

        // Remove quotes if present
        if (valueStr.front() == '"' && valueStr.back() == '"')
        {
            valueStr = valueStr.substr(1, valueStr.length() - 2);
        }

        return std::stod(valueStr);
    }

    long FastJsonParser::extractLong(const char *json, const char *key)
    {
        // Find key in JSON
        std::string keyPattern = "\"" + std::string(key) + "\"";
        const char *keyPos = strstr(json, keyPattern.c_str());
        if (!keyPos)
        {
            throw std::runtime_error("Key not found: " + std::string(key));
        }

        // Find colon after key
        const char *colonPos = strchr(keyPos, ':');
        if (!colonPos)
        {
            throw std::runtime_error("Colon not found after key: " + std::string(key));
        }

        // Skip whitespace after colon
        const char *valueStart = colonPos + 1;
        while (*valueStart == ' ' || *valueStart == '\t' || *valueStart == '\n')
        {
            valueStart++;
        }

        // Find end of value (comma, brace, or bracket)
        const char *valueEnd = valueStart;
        while (*valueEnd && *valueEnd != ',' && *valueEnd != '}' && *valueEnd != ']' && *valueEnd != '"')
        {
            valueEnd++;
        }

        // Convert to long
        std::string valueStr(valueStart, valueEnd - valueStart);

        // Remove quotes if present
        if (valueStr.front() == '"' && valueStr.back() == '"')
        {
            valueStr = valueStr.substr(1, valueStr.length() - 2);
        }

        return std::stol(valueStr);
    }

    std::string FastJsonParser::extractString(const char *json, const char *key)
    {
        // Find key in JSON
        std::string keyPattern = "\"" + std::string(key) + "\"";
        const char *keyPos = strstr(json, keyPattern.c_str());
        if (!keyPos)
        {
            throw std::runtime_error("Key not found: " + std::string(key));
        }

        // Find colon after key
        const char *colonPos = strchr(keyPos, ':');
        if (!colonPos)
        {
            throw std::runtime_error("Colon not found after key: " + std::string(key));
        }

        // Skip whitespace after colon
        const char *valueStart = colonPos + 1;
        while (*valueStart == ' ' || *valueStart == '\t' || *valueStart == '\n')
        {
            valueStart++;
        }

        // Find end of string (closing quote)
        if (*valueStart != '"')
        {
            throw std::runtime_error("String value not found for key: " + std::string(key));
        }

        valueStart++; // Skip opening quote
        const char *valueEnd = valueStart;
        while (*valueEnd && *valueEnd != '"')
        {
            valueEnd++;
        }

        return std::string(valueStart, valueEnd - valueStart);
    }

    FastJsonParser::PerformanceMetrics FastJsonParser::getPerformanceMetrics() const
    {
        uint64_t total = totalParses_.load();
        uint64_t failed = failedParses_.load();
        uint64_t parseTimeNs = totalParseTimeNs_.load();

        PerformanceMetrics metrics;
        metrics.totalParses = total;
        metrics.failedParses = failed;
        metrics.avgParseTimeMs = total > 0 ? (parseTimeNs / total) / 1e6 : 0.0;
        metrics.successRate = total > 0 ? (double)(total - failed) / total * 100.0 : 0.0;
        metrics.parsesPerSecond = total > 0 ? total / (parseTimeNs / 1e9) : 0.0;

        return metrics;
    }

    void FastJsonParser::resetMetrics()
    {
        totalParses_.store(0);
        failedParses_.store(0);
        totalParseTimeNs_.store(0);
    }

    bool FastJsonParser::isHealthy() const
    {
        return totalParses_.load() > 0;
    }

} // namespace quantis
