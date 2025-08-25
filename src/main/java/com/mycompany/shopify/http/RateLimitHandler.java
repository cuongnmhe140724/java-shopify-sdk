package com.mycompany.shopify.http;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Handles Shopify API rate limiting with sophisticated tracking and adaptive delays.
 * Implements leaky bucket algorithm and provides rate limit prediction.
 */
@Component
public class RateLimitHandler {
    
    private final ConcurrentHashMap<String, BucketInfo> buckets;
    private final AtomicInteger totalRequests;
    private final AtomicLong lastResetTime;
    
    // Shopify rate limits (requests per minute)
    private static final int REST_API_LIMIT = 40;
    private static final int GRAPHQL_API_LIMIT = 1000;
    private static final int WEBHOOK_LIMIT = 100;
    
    public RateLimitHandler() {
        this.buckets = new ConcurrentHashMap<>();
        this.totalRequests = new AtomicInteger(0);
        this.lastResetTime = new AtomicLong(System.currentTimeMillis());
    }
    
    /**
     * Checks if a request can be made without hitting rate limits.
     * @param endpoint The API endpoint
     * @param requestType The type of request (REST, GraphQL, WEBHOOK)
     * @return true if request can proceed, false if should wait
     */
    public boolean canMakeRequest(String endpoint, RequestType requestType) {
        String bucketKey = getBucketKey(endpoint, requestType);
        BucketInfo bucket = buckets.computeIfAbsent(bucketKey, k -> new BucketInfo(requestType));
        
        return bucket.canMakeRequest();
    }
    
    /**
     * Records a successful request and updates rate limit tracking.
     * @param endpoint The API endpoint
     * @param requestType The type of request
     * @param responseHeaders Response headers containing rate limit info
     */
    public void recordRequest(String endpoint, RequestType requestType, HttpHeaders responseHeaders) {
        String bucketKey = getBucketKey(endpoint, requestType);
        BucketInfo bucket = buckets.computeIfAbsent(bucketKey, k -> new BucketInfo(requestType));
        
        bucket.recordRequest(responseHeaders);
        totalRequests.incrementAndGet();
    }
    
    /**
     * Records a rate limit hit and calculates optimal wait time.
     * @param endpoint The API endpoint
     * @param requestType The type of request
     * @param responseHeaders Response headers containing rate limit info
     * @return Duration to wait before next request
     */
    public Duration handleRateLimit(String endpoint, RequestType requestType, HttpHeaders responseHeaders) {
        String bucketKey = getBucketKey(endpoint, requestType);
        BucketInfo bucket = buckets.computeIfAbsent(bucketKey, k -> new BucketInfo(requestType));
        
        return bucket.handleRateLimit(responseHeaders);
    }
    
    /**
     * Gets the optimal delay before making the next request.
     * @param endpoint The API endpoint
     * @param requestType The type of request
     * @return Duration to wait, or Duration.ZERO if no delay needed
     */
    public Duration getOptimalDelay(String endpoint, RequestType requestType) {
        String bucketKey = getBucketKey(endpoint, requestType);
        BucketInfo bucket = buckets.get(bucketKey);
        
        if (bucket == null) {
            return Duration.ZERO;
        }
        
        return bucket.getOptimalDelay();
    }
    
    /**
     * Gets rate limit statistics for monitoring.
     * @return Rate limit statistics
     */
    public RateLimitStats getStats() {
        RateLimitStats stats = new RateLimitStats();
        stats.setTotalRequests(totalRequests.get());
        stats.setLastResetTime(Instant.ofEpochMilli(lastResetTime.get()));
        stats.setActiveBuckets(buckets.size());
        
        // Calculate current usage across all buckets
        int totalUsage = buckets.values().stream()
                .mapToInt(BucketInfo::getCurrentUsage)
                .sum();
        stats.setCurrentUsage(totalUsage);
        
        return stats;
    }
    
    /**
     * Resets all rate limit tracking (useful for testing or after long periods).
     */
    public void resetAll() {
        buckets.clear();
        totalRequests.set(0);
        lastResetTime.set(System.currentTimeMillis());
    }
    
    /**
     * Gets the bucket key for rate limit tracking.
     * @param endpoint The API endpoint
     * @param requestType The type of request
     * @return The bucket key
     */
    private String getBucketKey(String endpoint, RequestType requestType) {
        return requestType.name() + ":" + endpoint;
    }
    
    /**
     * Represents different types of API requests with different rate limits.
     */
    public enum RequestType {
        REST(REST_API_LIMIT),
        GRAPHQL(GRAPHQL_API_LIMIT),
        WEBHOOK(WEBHOOK_LIMIT);
        
        private final int requestsPerMinute;
        
        RequestType(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
        
        public int getRequestsPerMinute() {
            return requestsPerMinute;
        }
    }
    
    /**
     * Internal class representing a rate limit bucket.
     */
    private static class BucketInfo {
        private final RequestType requestType;
        private final AtomicInteger currentUsage;
        private final AtomicLong lastResetTime;
        private final AtomicLong lastRequestTime;
        private volatile Duration lastRateLimitDelay;
        
        public BucketInfo(RequestType requestType) {
            this.requestType = requestType;
            this.currentUsage = new AtomicInteger(0);
            this.lastResetTime = new AtomicLong(System.currentTimeMillis());
            this.lastRequestTime = new AtomicLong(System.currentTimeMillis());
        }
        
        public boolean canMakeRequest() {
            long now = System.currentTimeMillis();
            long timeSinceReset = now - lastResetTime.get();
            
            // Reset counter if a minute has passed
            if (timeSinceReset >= 60000) {
                currentUsage.set(0);
                lastResetTime.set(now);
                return true;
            }
            
            return currentUsage.get() < requestType.getRequestsPerMinute();
        }
        
        public void recordRequest(HttpHeaders responseHeaders) {
            currentUsage.incrementAndGet();
            lastRequestTime.set(System.currentTimeMillis());
            
            // Update tracking based on response headers
            String remaining = responseHeaders.getFirst("X-Shopify-Shop-Api-Call-Limit");
            if (remaining != null) {
                try {
                    String[] parts = remaining.split("/");
                    if (parts.length == 2) {
                        int used = Integer.parseInt(parts[0]);
                        int limit = Integer.parseInt(parts[1]);
                        currentUsage.set(used);
                    }
                } catch (NumberFormatException e) {
                    // Ignore parsing errors
                }
            }
        }
        
        public Duration handleRateLimit(HttpHeaders responseHeaders) {
            // Parse Retry-After header
            String retryAfter = responseHeaders.getFirst("Retry-After");
            Duration delay = parseRetryAfter(retryAfter);
            
            // If no Retry-After header, calculate based on current usage
            if (delay == null) {
                long now = System.currentTimeMillis();
                long timeUntilReset = 60000 - (now - lastResetTime.get());
                delay = Duration.ofMillis(Math.max(timeUntilReset, 1000)); // Minimum 1 second
            }
            
            lastRateLimitDelay = delay;
            return delay;
        }
        
        public Duration getOptimalDelay() {
            if (canMakeRequest()) {
                return Duration.ZERO;
            }
            
            // Calculate delay based on current usage and time until reset
            long now = System.currentTimeMillis();
            long timeUntilReset = 60000 - (now - lastResetTime.get());
            
            if (timeUntilReset <= 0) {
                return Duration.ZERO;
            }
            
            // Add some buffer to avoid hitting limits
            long bufferTime = Math.max(1000, timeUntilReset / 10);
            return Duration.ofMillis(timeUntilReset + bufferTime);
        }
        
        public int getCurrentUsage() {
            return currentUsage.get();
        }
        
        private Duration parseRetryAfter(String retryAfter) {
            if (retryAfter == null) {
                return null;
            }
            
            try {
                int seconds = Integer.parseInt(retryAfter);
                return Duration.ofSeconds(seconds);
            } catch (NumberFormatException e) {
                // Try to parse as HTTP date
                try {
                    // This is a simplified implementation
                    // In production, you'd want proper HTTP date parsing
                    return Duration.ofSeconds(60); // Default to 1 minute
                } catch (Exception ex) {
                    return Duration.ofSeconds(60); // Default fallback
                }
            }
        }
    }
    
    /**
     * Statistics class for monitoring rate limit usage.
     */
    public static class RateLimitStats {
        private int totalRequests;
        private int currentUsage;
        private int activeBuckets;
        private Instant lastResetTime;
        
        // Getters and Setters
        public int getTotalRequests() { return totalRequests; }
        public void setTotalRequests(int totalRequests) { this.totalRequests = totalRequests; }
        
        public int getCurrentUsage() { return currentUsage; }
        public void setCurrentUsage(int currentUsage) { this.currentUsage = currentUsage; }
        
        public int getActiveBuckets() { return activeBuckets; }
        public void setActiveBuckets(int activeBuckets) { this.activeBuckets = activeBuckets; }
        
        public Instant getLastResetTime() { return lastResetTime; }
        public void setLastResetTime(Instant lastResetTime) { this.lastResetTime = lastResetTime; }
    }
}
