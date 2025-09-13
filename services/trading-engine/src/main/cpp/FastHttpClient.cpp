#include "FastHttpClient.h"
#include <chrono>
#include <iostream>
#include <sstream>
#include <vector>

namespace quantis
{

    // Static member definitions
    CURLSH *FastHttpClient::shareHandle_ = nullptr;
    std::atomic<bool> FastHttpClient::shareInitialized_{false};

    FastHttpClient::FastHttpClient()
    {
        curl_ = curl_easy_init();
        if (!curl_)
        {
            throw std::runtime_error("Failed to initialize CURL");
        }

        // Initialize shared handle for connection pooling
        initializeShareHandle();

        // Configure CURL for maximum performance
        curl_easy_setopt(curl_, CURLOPT_SHARE, shareHandle_);
        curl_easy_setopt(curl_, CURLOPT_FOLLOWLOCATION, 1L);
        curl_easy_setopt(curl_, CURLOPT_MAXREDIRS, 3L);
        curl_easy_setopt(curl_, CURLOPT_TIMEOUT_MS, 100L);       // 100ms timeout
        curl_easy_setopt(curl_, CURLOPT_CONNECTTIMEOUT_MS, 50L); // 50ms connect timeout
        curl_easy_setopt(curl_, CURLOPT_TCP_KEEPALIVE, 1L);
        curl_easy_setopt(curl_, CURLOPT_TCP_KEEPIDLE, 60L);
        curl_easy_setopt(curl_, CURLOPT_TCP_KEEPINTVL, 60L);

        // Disable SSL verification for speed (use only for trusted APIs)
        curl_easy_setopt(curl_, CURLOPT_SSL_VERIFYPEER, 0L);
        curl_easy_setopt(curl_, CURLOPT_SSL_VERIFYHOST, 0L);

        // Enable compression
        curl_easy_setopt(curl_, CURLOPT_ACCEPT_ENCODING, "gzip,deflate");

        // Set user agent
        curl_easy_setopt(curl_, CURLOPT_USERAGENT, "QuantisTradingEngine/1.0");

        // Configure write callback
        curl_easy_setopt(curl_, CURLOPT_WRITEFUNCTION, writeCallback);
        curl_easy_setopt(curl_, CURLOPT_WRITEDATA, &responseBuffer_);
    }

    FastHttpClient::~FastHttpClient()
    {
        if (curl_)
        {
            curl_easy_cleanup(curl_);
        }
    }

    void FastHttpClient::initializeShareHandle()
    {
        if (!shareInitialized_.exchange(true))
        {
            shareHandle_ = curl_share_init();
            if (shareHandle_)
            {
                curl_share_setopt(shareHandle_, CURLSHOPT_SHARE, CURL_LOCK_DATA_DNS);
                curl_share_setopt(shareHandle_, CURLSHOPT_SHARE, CURL_LOCK_DATA_COOKIE);
                curl_share_setopt(shareHandle_, CURLSHOPT_SHARE, CURL_LOCK_DATA_SSL_SESSION);
            }
        }
    }

    size_t FastHttpClient::writeCallback(void *contents, size_t size, size_t nmemb, std::string *s)
    {
        size_t totalSize = size * nmemb;
        s->append(static_cast<char *>(contents), totalSize);
        return totalSize;
    }

    std::string FastHttpClient::get(const std::string &url)
    {
        std::lock_guard<std::mutex> lock(mutex_);

        auto start = std::chrono::high_resolution_clock::now();

        // Clear response buffer
        responseBuffer_.clear();

        // Set URL
        curl_easy_setopt(curl_, CURLOPT_URL, url.c_str());

        // Perform request
        CURLcode res = curl_easy_perform(curl_);

        auto end = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start);

        // Update performance counters
        totalRequests_.fetch_add(1);
        totalLatencyNs_.fetch_add(duration.count());

        if (res != CURLE_OK)
        {
            failedRequests_.fetch_add(1);
            return "";
        }

        return responseBuffer_;
    }

    std::string FastHttpClient::get(const std::string &url, const std::vector<std::string> &headers)
    {
        std::lock_guard<std::mutex> lock(mutex_);

        auto start = std::chrono::high_resolution_clock::now();

        // Clear response buffer
        responseBuffer_.clear();

        // Set URL
        curl_easy_setopt(curl_, CURLOPT_URL, url.c_str());

        // Set headers
        struct curl_slist *headerList = nullptr;
        for (const auto &header : headers)
        {
            headerList = curl_slist_append(headerList, header.c_str());
        }
        curl_easy_setopt(curl_, CURLOPT_HTTPHEADER, headerList);

        // Perform request
        CURLcode res = curl_easy_perform(curl_);

        // Clean up headers
        if (headerList)
        {
            curl_slist_free_all(headerList);
        }

        auto end = std::chrono::high_resolution_clock::now();
        auto duration = std::chrono::duration_cast<std::chrono::nanoseconds>(end - start);

        // Update performance counters
        totalRequests_.fetch_add(1);
        totalLatencyNs_.fetch_add(duration.count());

        if (res != CURLE_OK)
        {
            failedRequests_.fetch_add(1);
            return "";
        }

        return responseBuffer_;
    }

    std::string FastHttpClient::buildAlphaVantageUrl(const std::string &symbol, const std::string &apiKey)
    {
        std::ostringstream url;
        url << "https://www.alphavantage.co/query?"
            << "function=GLOBAL_QUOTE&"
            << "symbol=" << symbol << "&"
            << "apikey=" << apiKey;
        return url.str();
    }

    FastHttpClient::PerformanceMetrics FastHttpClient::getPerformanceMetrics() const
    {
        uint64_t total = totalRequests_.load();
        uint64_t failed = failedRequests_.load();
        uint64_t latencyNs = totalLatencyNs_.load();

        PerformanceMetrics metrics;
        metrics.totalRequests = total;
        metrics.failedRequests = failed;
        metrics.avgLatencyMs = total > 0 ? (latencyNs / total) / 1e6 : 0.0;
        metrics.successRate = total > 0 ? (double)(total - failed) / total * 100.0 : 0.0;
        metrics.requestsPerSecond = total > 0 ? total / (latencyNs / 1e9) : 0.0;

        return metrics;
    }

    void FastHttpClient::resetMetrics()
    {
        totalRequests_.store(0);
        failedRequests_.store(0);
        totalLatencyNs_.store(0);
    }

    bool FastHttpClient::isHealthy() const
    {
        return curl_ != nullptr && totalRequests_.load() > 0;
    }

} // namespace quantis
