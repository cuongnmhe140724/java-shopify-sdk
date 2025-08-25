package com.mycompany.shopify.utils;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Monitors API performance metrics and provides insights for optimization.
 * Tracks response times, success rates, and provides performance analytics.
 */
@Component
public class PerformanceMonitor {
    
    private final ConcurrentHashMap<String, EndpointStats> endpointStats;
    private final ConcurrentLinkedQueue<RequestMetric> recentRequests;
    private final AtomicLong totalRequests;
    private final AtomicLong totalErrors;
    private final LongAdder totalResponseTime;
    
    private static final int MAX_RECENT_REQUESTS = 1000;
    
    public PerformanceMonitor() {
        this.endpointStats = new ConcurrentHashMap<>();
        this.recentRequests = new ConcurrentLinkedQueue<>();
        this.totalRequests = new AtomicLong(0);
        this.totalErrors = new AtomicLong(0);
        this.totalResponseTime = new LongAdder();
    }
    
    /**
     * Records a successful API request.
     * @param endpoint The API endpoint
     * @param method The HTTP method
     * @param responseTime Response time in milliseconds
     * @param statusCode HTTP status code
     */
    public void recordSuccess(String endpoint, String method, long responseTime, int statusCode) {
        recordRequest(endpoint, method, responseTime, statusCode, null);
    }
    
    /**
     * Records a failed API request.
     * @param endpoint The API endpoint
     * @param method The HTTP method
     * @param responseTime Response time in milliseconds
     * @param statusCode HTTP status code
     * @param error The error that occurred
     */
    public void recordError(String endpoint, String method, long responseTime, int statusCode, Throwable error) {
        recordRequest(endpoint, method, responseTime, statusCode, error);
        totalErrors.incrementAndGet();
    }
    
    /**
     * Records an API request (internal method).
     * @param endpoint The API endpoint
     * @param method The HTTP method
     * @param responseTime Response time in milliseconds
     * @param statusCode HTTP status code
     * @param error The error that occurred (null for success)
     */
    private void recordRequest(String endpoint, String method, long responseTime, int statusCode, Throwable error) {
        String key = endpoint + ":" + method;
        
        // Update endpoint statistics
        EndpointStats stats = endpointStats.computeIfAbsent(key, k -> new EndpointStats(endpoint, method));
        stats.recordRequest(responseTime, statusCode, error != null);
        
        // Update global statistics
        totalRequests.incrementAndGet();
        totalResponseTime.add(responseTime);
        
        // Record recent request
        RequestMetric metric = new RequestMetric(endpoint, method, responseTime, statusCode, error, Instant.now());
        recentRequests.offer(metric);
        
        // Maintain queue size
        while (recentRequests.size() > MAX_RECENT_REQUESTS) {
            recentRequests.poll();
        }
    }
    
    /**
     * Gets performance statistics for a specific endpoint.
     * @param endpoint The API endpoint
     * @param method The HTTP method
     * @return Endpoint statistics
     */
    public EndpointStats getEndpointStats(String endpoint, String method) {
        String key = endpoint + ":" + method;
        return endpointStats.get(key);
    }
    
    /**
     * Gets overall performance statistics.
     * @return Overall performance statistics
     */
    public OverallStats getOverallStats() {
        OverallStats stats = new OverallStats();
        stats.setTotalRequests(totalRequests.get());
        stats.setTotalErrors(totalErrors.get());
        stats.setTotalResponseTime(Duration.ofMillis(totalResponseTime.sum()));
        
        if (totalRequests.get() > 0) {
            stats.setAverageResponseTime(Duration.ofMillis(totalResponseTime.sum() / totalRequests.get()));
            stats.setErrorRate((double) totalErrors.get() / totalRequests.get());
        }
        
        stats.setActiveEndpoints(endpointStats.size());
        return stats;
    }
    
    /**
     * Gets the top performing endpoints by response time.
     * @param limit Maximum number of endpoints to return
     * @return Map of endpoint to average response time
     */
    public Map<String, Duration> getTopPerformingEndpoints(int limit) {
        return endpointStats.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e1.getValue().getAverageResponseTime(), e2.getValue().getAverageResponseTime()))
                .limit(limit)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> Duration.ofMillis(e.getValue().getAverageResponseTime())
                ));
    }
    
    /**
     * Gets the slowest endpoints by response time.
     * @param limit Maximum number of endpoints to return
     * @return Map of endpoint to average response time
     */
    public Map<String, Duration> getSlowestEndpoints(int limit) {
        return endpointStats.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().getAverageResponseTime(), e1.getValue().getAverageResponseTime()))
                .limit(limit)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> Duration.ofMillis(e.getValue().getAverageResponseTime())
                ));
    }
    
    /**
     * Gets endpoints with the highest error rates.
     * @param limit Maximum number of endpoints to return
     * @return Map of endpoint to error rate
     */
    public Map<String, Double> getEndpointsWithHighestErrorRate(int limit) {
        return endpointStats.entrySet().stream()
                .filter(e -> e.getValue().getTotalRequests() > 0)
                .sorted((e1, e2) -> Double.compare(e2.getValue().getErrorRate(), e1.getValue().getErrorRate()))
                .limit(limit)
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getErrorRate()
                ));
    }
    
    /**
     * Gets recent request metrics for analysis.
     * @param limit Maximum number of recent requests to return
     * @return List of recent request metrics
     */
    public java.util.List<RequestMetric> getRecentRequests(int limit) {
        return recentRequests.stream()
                .sorted((r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * Resets all performance monitoring data.
     */
    public void reset() {
        endpointStats.clear();
        recentRequests.clear();
        totalRequests.set(0);
        totalErrors.set(0);
        totalResponseTime.reset();
    }
    
    /**
     * Statistics for a specific endpoint.
     */
    public static class EndpointStats {
        private final String endpoint;
        private final String method;
        private final AtomicLong totalRequests;
        private final AtomicLong totalErrors;
        private final LongAdder totalResponseTime;
        private final AtomicLong minResponseTime;
        private final AtomicLong maxResponseTime;
        
        public EndpointStats(String endpoint, String method) {
            this.endpoint = endpoint;
            this.method = method;
            this.totalRequests = new AtomicLong(0);
            this.totalErrors = new AtomicLong(0);
            this.totalResponseTime = new LongAdder();
            this.minResponseTime = new AtomicLong(Long.MAX_VALUE);
            this.maxResponseTime = new AtomicLong(0);
        }
        
        public void recordRequest(long responseTime, int statusCode, boolean isError) {
            totalRequests.incrementAndGet();
            if (isError) {
                totalErrors.incrementAndGet();
            }
            
            totalResponseTime.add(responseTime);
            
            // Update min/max response times
            minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
            maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
        }
        
        // Getters
        public String getEndpoint() { return endpoint; }
        public String getMethod() { return method; }
        public long getTotalRequests() { return totalRequests.get(); }
        public long getTotalErrors() { return totalErrors.get(); }
        public long getTotalResponseTime() { return totalResponseTime.sum(); }
        public long getMinResponseTime() { return minResponseTime.get(); }
        public long getMaxResponseTime() { return maxResponseTime.get(); }
        
        public long getAverageResponseTime() {
            long total = totalRequests.get();
            return total > 0 ? totalResponseTime.sum() / total : 0;
        }
        
        public double getErrorRate() {
            long total = totalRequests.get();
            return total > 0 ? (double) totalErrors.get() / total : 0.0;
        }
        
        public double getSuccessRate() {
            return 1.0 - getErrorRate();
        }
    }
    
    /**
     * Overall performance statistics.
     */
    public static class OverallStats {
        private long totalRequests;
        private long totalErrors;
        private Duration totalResponseTime;
        private Duration averageResponseTime;
        private double errorRate;
        private int activeEndpoints;
        
        // Getters and Setters
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        
        public long getTotalErrors() { return totalErrors; }
        public void setTotalErrors(long totalErrors) { this.totalErrors = totalErrors; }
        
        public Duration getTotalResponseTime() { return totalResponseTime; }
        public void setTotalResponseTime(Duration totalResponseTime) { this.totalResponseTime = totalResponseTime; }
        
        public Duration getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(Duration averageResponseTime) { this.averageResponseTime = averageResponseTime; }
        
        public double getErrorRate() { return errorRate; }
        public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
        
        public int getActiveEndpoints() { return activeEndpoints; }
        public void setActiveEndpoints(int activeEndpoints) { this.activeEndpoints = activeEndpoints; }
        
        public double getSuccessRate() {
            return 1.0 - errorRate;
        }
    }
    
    /**
     * Individual request metric.
     */
    public static class RequestMetric {
        private final String endpoint;
        private final String method;
        private final long responseTime;
        private final int statusCode;
        private final Throwable error;
        private final Instant timestamp;
        
        public RequestMetric(String endpoint, String method, long responseTime, int statusCode, Throwable error, Instant timestamp) {
            this.endpoint = endpoint;
            this.method = method;
            this.responseTime = responseTime;
            this.statusCode = statusCode;
            this.error = error;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getEndpoint() { return endpoint; }
        public String getMethod() { return method; }
        public long getResponseTime() { return responseTime; }
        public int getStatusCode() { return statusCode; }
        public Throwable getError() { return error; }
        public Instant getTimestamp() { return timestamp; }
        public boolean isSuccess() { return error == null && statusCode < 400; }
    }
}
