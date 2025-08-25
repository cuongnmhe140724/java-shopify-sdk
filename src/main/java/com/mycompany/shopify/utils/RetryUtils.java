package com.mycompany.shopify.utils;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for retry operations and exponential backoff.
 * Provides methods for retrying operations with configurable strategies.
 */
public class RetryUtils {
    
    private static final ScheduledExecutorService DEFAULT_SCHEDULER = Executors.newScheduledThreadPool(2);
    
    /**
     * Executes a supplier with retry logic.
     * @param supplier The operation to retry
     * @param maxRetries Maximum number of retry attempts
     * @param baseDelay Base delay between retries
     * @param backoffMultiplier Multiplier for exponential backoff
     * @return The result of the operation
     * @throws RuntimeException if all retries are exhausted
     */
    public static <T> T executeWithRetry(Supplier<T> supplier, int maxRetries, Duration baseDelay, double backoffMultiplier) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt <= maxRetries) {
            try {
                return supplier.get();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == maxRetries) {
                    throw new RuntimeException("Max retries exceeded", lastException);
                }
                
                // Calculate delay for next attempt
                Duration delay = calculateBackoffDelay(attempt, baseDelay, backoffMultiplier);
                waitForDuration(delay);
                attempt++;
            }
        }
        
        throw new RuntimeException("Unexpected error in retry logic", lastException);
    }
    
    /**
     * Executes a supplier with retry logic using default settings.
     * @param supplier The operation to retry
     * @return The result of the operation
     */
    public static <T> T executeWithRetry(Supplier<T> supplier) {
        return executeWithRetry(supplier, 3, Duration.ofSeconds(1), 2.0);
    }
    
    /**
     * Executes a supplier with retry logic asynchronously.
     * @param supplier The operation to retry
     * @param maxRetries Maximum number of retry attempts
     * @param baseDelay Base delay between retries
     * @param backoffMultiplier Multiplier for exponential backoff
     * @return CompletableFuture with the result
     */
    public static <T> CompletableFuture<T> executeWithRetryAsync(Supplier<T> supplier, int maxRetries, 
                                                                Duration baseDelay, double backoffMultiplier) {
        return CompletableFuture.supplyAsync(() -> 
            executeWithRetry(supplier, maxRetries, baseDelay, backoffMultiplier)
        );
    }
    
    /**
     * Executes a supplier with retry logic asynchronously using default settings.
     * @param supplier The operation to retry
     * @return CompletableFuture with the result
     */
    public static <T> CompletableFuture<T> executeWithRetryAsync(Supplier<T> supplier) {
        return executeWithRetryAsync(supplier, 3, Duration.ofSeconds(1), 2.0);
    }
    
    /**
     * Executes a supplier with retry logic and custom retry condition.
     * @param supplier The operation to retry
     * @param shouldRetry Function that determines if retry should be attempted
     * @param maxRetries Maximum number of retry attempts
     * @param baseDelay Base delay between retries
     * @param backoffMultiplier Multiplier for exponential backoff
     * @return The result of the operation
     */
    public static <T> T executeWithRetryCondition(Supplier<T> supplier, RetryCondition<T> shouldRetry, 
                                                 int maxRetries, Duration baseDelay, double backoffMultiplier) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt <= maxRetries) {
            try {
                T result = supplier.get();
                
                // Check if retry is needed based on the result
                if (!shouldRetry.shouldRetry(result, attempt, lastException)) {
                    return result;
                }
                
                if (attempt == maxRetries) {
                    return result; // Return last result even if retry condition is met
                }
                
            } catch (Exception e) {
                lastException = e;
                
                // Check if retry is needed based on the exception
                if (!shouldRetry.shouldRetry(null, attempt, e)) {
                    throw new RuntimeException("Operation failed and retry not allowed", e);
                }
                
                if (attempt == maxRetries) {
                    throw new RuntimeException("Max retries exceeded", lastException);
                }
            }
            
            // Calculate delay for next attempt
            Duration delay = calculateBackoffDelay(attempt, baseDelay, backoffMultiplier);
            waitForDuration(delay);
            attempt++;
        }
        
        throw new RuntimeException("Unexpected error in retry logic", lastException);
    }
    
    /**
     * Executes a supplier with circuit breaker pattern.
     * @param supplier The operation to execute
     * @param circuitBreaker The circuit breaker instance
     * @return The result of the operation
     */
    public static <T> T executeWithCircuitBreaker(Supplier<T> supplier, CircuitBreaker circuitBreaker) {
        return circuitBreaker.execute(supplier);
    }
    
    /**
     * Executes a supplier with circuit breaker and fallback.
     * @param supplier The primary operation
     * @param fallback The fallback operation
     * @param circuitBreaker The circuit breaker instance
     * @return The result of the operation or fallback
     */
    public static <T> T executeWithCircuitBreakerAndFallback(Supplier<T> supplier, Supplier<T> fallback, 
                                                            CircuitBreaker circuitBreaker) {
        try {
            return circuitBreaker.execute(supplier);
        } catch (Exception e) {
            return fallback.get();
        }
    }
    
    /**
     * Calculates the backoff delay for a retry attempt.
     * @param attempt Current attempt number (0-based)
     * @param baseDelay Base delay
     * @param backoffMultiplier Multiplier for exponential backoff
     * @return Duration to wait
     */
    private static Duration calculateBackoffDelay(int attempt, Duration baseDelay, double backoffMultiplier) {
        long delayMs = baseDelay.toMillis() * (long) Math.pow(backoffMultiplier, attempt);
        return Duration.ofMillis(delayMs);
    }
    
    /**
     * Waits for the specified duration.
     * @param duration Duration to wait
     */
    private static void waitForDuration(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Retry wait interrupted", e);
        }
    }
    
    /**
     * Schedules a delayed execution using the default scheduler.
     * @param supplier The operation to execute
     * @param delay Delay before execution
     * @return CompletableFuture with the result
     */
    public static <T> CompletableFuture<T> scheduleDelayed(Supplier<T> supplier, Duration delay) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        DEFAULT_SCHEDULER.schedule(() -> {
            try {
                T result = supplier.get();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
        
        return future;
    }
    
    /**
     * Shuts down the default scheduler.
     */
    public static void shutdown() {
        DEFAULT_SCHEDULER.shutdown();
        try {
            if (!DEFAULT_SCHEDULER.awaitTermination(60, TimeUnit.SECONDS)) {
                DEFAULT_SCHEDULER.shutdownNow();
            }
        } catch (InterruptedException e) {
            DEFAULT_SCHEDULER.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Functional interface for determining if retry should be attempted.
     * @param <T> The type of the operation result
     */
    @FunctionalInterface
    public interface RetryCondition<T> {
        /**
         * Determines if retry should be attempted.
         * @param result The result of the operation (null if exception occurred)
         * @param attempt Current attempt number (0-based)
         * @param lastException The last exception that occurred (null if no exception)
         * @return true if retry should be attempted, false otherwise
         */
        boolean shouldRetry(T result, int attempt, Exception lastException);
    }
    
    /**
     * Circuit breaker implementation for fault tolerance.
     */
    public static class CircuitBreaker {
        private final int failureThreshold;
        private final Duration timeout;
        private final Duration recoveryTime;
        
        private volatile State state = State.CLOSED;
        private volatile int failureCount = 0;
        private volatile long lastFailureTime = 0;
        
        public CircuitBreaker(int failureThreshold, Duration timeout, Duration recoveryTime) {
            this.failureThreshold = failureThreshold;
            this.timeout = timeout;
            this.recoveryTime = recoveryTime;
        }
        
        public <T> T execute(Supplier<T> supplier) {
            if (state == State.OPEN) {
                if (System.currentTimeMillis() - lastFailureTime > recoveryTime.toMillis()) {
                    state = State.HALF_OPEN;
                } else {
                    throw new RuntimeException("Circuit breaker is OPEN");
                }
            }
            
            try {
                T result = supplier.get();
                onSuccess();
                return result;
            } catch (Exception e) {
                onFailure();
                throw e;
            }
        }
        
        private void onSuccess() {
            failureCount = 0;
            state = State.CLOSED;
        }
        
        private void onFailure() {
            failureCount++;
            lastFailureTime = System.currentTimeMillis();
            
            if (failureCount >= failureThreshold) {
                state = State.OPEN;
            }
        }
        
        public State getState() {
            return state;
        }
        
        public int getFailureCount() {
            return failureCount;
        }
        
        public enum State {
            CLOSED,    // Normal operation
            OPEN,      // Failing, reject all requests
            HALF_OPEN // Testing if service is recovered
        }
    }
}
