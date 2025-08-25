package com.mycompany.shopify.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * HTTP client wrapper with retry logic and error handling.
 * Handles rate limiting (429), network errors, and provides configurable retry strategies.
 */
@Component
public class HttpClientWrapper {
    
    private final RestTemplate restTemplate;
    private final ScheduledExecutorService scheduler;
    private final RetryConfig retryConfig;
    
    public HttpClientWrapper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.scheduler = Executors.newScheduledThreadPool(2);
        this.retryConfig = new RetryConfig();
    }
    
    /**
     * Executes an HTTP request with retry logic.
     * @param request The HTTP request to execute
     * @return The HTTP response
     */
    public <T> ResponseEntity<T> executeWithRetry(HttpRequest<T> request) {
        return executeWithRetry(request, retryConfig);
    }
    
    /**
     * Executes an HTTP request with custom retry configuration.
     * @param request The HTTP request to execute
     * @param config Custom retry configuration
     * @return The HTTP response
     */
    public <T> ResponseEntity<T> executeWithRetry(HttpRequest<T> request, RetryConfig config) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < config.getMaxRetries()) {
            try {
                return executeRequest(request);
            } catch (HttpClientErrorException e) {
                lastException = e;
                
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    // Handle rate limiting
                    Duration retryAfter = parseRetryAfter(e.getResponseHeaders());
                    if (retryAfter != null && attempt < config.getMaxRetries() - 1) {
                        waitForDuration(retryAfter);
                        attempt++;
                        continue;
                    }
                }
                
                // Don't retry on client errors (4xx) except rate limiting
                throw e;
                
            } catch (HttpServerErrorException e) {
                lastException = e;
                
                if (shouldRetryOnServerError(e.getStatusCode().value()) && attempt < config.getMaxRetries() - 1) {
                    Duration delay = calculateBackoffDelay(attempt, config, null);
                    waitForDuration(delay);
                    attempt++;
                    continue;
                }
                
                throw e;
                
            } catch (ResourceAccessException e) {
                lastException = e;
                
                if (attempt < config.getMaxRetries() - 1) {
                    Duration delay = calculateBackoffDelay(attempt, config, null);
                    waitForDuration(delay);
                    attempt++;
                    continue;
                }
                
                throw e;
            }
        }
        
        throw new RuntimeException("Max retries exceeded", lastException);
    }
    
    /**
     * Executes an HTTP request asynchronously with retry logic.
     * @param request The HTTP request to execute
     * @return CompletableFuture with the response
     */
    public <T> CompletableFuture<ResponseEntity<T>> executeAsyncWithRetry(HttpRequest<T> request) {
        return CompletableFuture.supplyAsync(() -> executeWithRetry(request));
    }
    
    /**
     * Executes the actual HTTP request.
     * @param request The HTTP request
     * @return The HTTP response
     */
    private <T> ResponseEntity<T> executeRequest(HttpRequest<T> request) {
        HttpHeaders headers = new HttpHeaders();
        if (request.getHeaders() != null) {
            headers.putAll(request.getHeaders());
        }
        
        // Add authentication header if token is provided
        if (request.getAccessToken() != null) {
            headers.setBearerAuth(request.getAccessToken());
        }
        
        // Add Shopify-specific headers
        headers.set("X-Shopify-Access-Token", request.getAccessToken());
        headers.set("Content-Type", "application/json");
        
        return restTemplate.exchange(
            request.getUrl(),
            request.getMethod(),
            request.getEntity(),
            request.getResponseType()
        );
    }
    
    /**
     * Parses the Retry-After header to determine wait duration.
     * @param headers Response headers
     * @return Duration to wait, or null if header not present
     */
    private Duration parseRetryAfter(HttpHeaders headers) {
        String retryAfter = headers.getFirst("Retry-After");
        if (retryAfter == null) {
            return null;
        }
        
        try {
            // Try to parse as seconds
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
    
    /**
     * Determines if a server error should trigger a retry.
     * @param statusCode The HTTP status code value
     * @return true if retry should be attempted
     */
    private boolean shouldRetryOnServerError(int statusCode) {
        return statusCode == 500 || // INTERNAL_SERVER_ERROR
               statusCode == 502 || // BAD_GATEWAY
               statusCode == 503 || // SERVICE_UNAVAILABLE
               statusCode == 504;   // GATEWAY_TIMEOUT
    }
    
    /**
     * Calculates the backoff delay for retry attempts with jitter and adaptive retry.
     * @param attempt Current attempt number (0-based)
     * @param config Retry configuration
     * @param lastResponseTime Last response time in milliseconds (for adaptive retry)
     * @return Duration to wait
     */
    private Duration calculateBackoffDelay(int attempt, RetryConfig config, Long lastResponseTime) {
        long delayMs = config.getBaseDelayMs() * (long) Math.pow(config.getBackoffMultiplier(), attempt);
        
        // Apply adaptive retry if enabled and we have response time data
        if (config.isAdaptiveRetryEnabled() && lastResponseTime != null && attempt >= config.getAdaptiveRetryThreshold()) {
            // Adjust delay based on response time - slower responses get longer delays
            double responseTimeFactor = Math.min(lastResponseTime / 1000.0, 2.0); // Cap at 2x
            delayMs = (long) (delayMs * responseTimeFactor);
        }
        
        // Apply jitter if enabled
        if (config.isJitterEnabled()) {
            double jitter = 1.0 + (Math.random() - 0.5) * 2 * config.getJitterFactor();
            delayMs = (long) (delayMs * jitter);
        }
        
        delayMs = Math.min(delayMs, config.getMaxDelayMs());
        return Duration.ofMillis(delayMs);
    }
    
    /**
     * Waits for the specified duration.
     * @param duration Duration to wait
     */
    private void waitForDuration(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry wait interrupted", e);
        }
    }
    
    /**
     * Shuts down the scheduler.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Configuration class for retry behavior.
     */
    public static class RetryConfig {
        private int maxRetries = 3;
        private long baseDelayMs = 1000; // 1 second
        private double backoffMultiplier = 2.0;
        private long maxDelayMs = 30000; // 30 seconds
        private boolean jitterEnabled = true;
        private double jitterFactor = 0.1; // 10% jitter
        private boolean adaptiveRetryEnabled = true;
        private int adaptiveRetryThreshold = 2;
        
        // Getters and Setters
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
        
        public long getBaseDelayMs() {
            return baseDelayMs;
        }
        
        public void setBaseDelayMs(long baseDelayMs) {
            this.baseDelayMs = baseDelayMs;
        }
        
        public double getBackoffMultiplier() {
            return backoffMultiplier;
        }
        
        public void setBackoffMultiplier(double backoffMultiplier) {
            this.backoffMultiplier = backoffMultiplier;
        }
        
        public long getMaxDelayMs() {
            return maxDelayMs;
        }
        
        public void setMaxDelayMs(long maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
        }
        
        public boolean isJitterEnabled() {
            return jitterEnabled;
        }
        
        public void setJitterEnabled(boolean jitterEnabled) {
            this.jitterEnabled = jitterEnabled;
        }
        
        public double getJitterFactor() {
            return jitterFactor;
        }
        
        public void setJitterFactor(double jitterFactor) {
            this.jitterFactor = jitterFactor;
        }
        
        public boolean isAdaptiveRetryEnabled() {
            return adaptiveRetryEnabled;
        }
        
        public void setAdaptiveRetryEnabled(boolean adaptiveRetryEnabled) {
            this.adaptiveRetryEnabled = adaptiveRetryEnabled;
        }
        
        public int getAdaptiveRetryThreshold() {
            return adaptiveRetryThreshold;
        }
        
        public void setAdaptiveRetryThreshold(int adaptiveRetryThreshold) {
            this.adaptiveRetryThreshold = adaptiveRetryThreshold;
        }
    }
}
