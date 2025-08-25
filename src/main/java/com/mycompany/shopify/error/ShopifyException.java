package com.mycompany.shopify.error;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Comprehensive exception class for Shopify API errors.
 * Provides detailed error information and context for debugging and handling.
 */
public class ShopifyException extends RuntimeException {
    
    private final ErrorType errorType;
    private final int statusCode;
    private final String errorCode;
    private final String endpoint;
    private final Map<String, Object> errorDetails;
    private final Instant timestamp;
    private final int retryCount;
    private final Duration retryAfter;
    
    public ShopifyException(String message, ErrorType errorType, int statusCode, String errorCode, 
                           String endpoint, Map<String, Object> errorDetails, int retryCount, 
                           Duration retryAfter, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
        this.statusCode = statusCode;
        this.errorCode = errorCode;
        this.endpoint = endpoint;
        this.errorDetails = errorDetails;
        this.timestamp = Instant.now();
        this.retryCount = retryCount;
        this.retryAfter = retryAfter;
    }
    
    // Factory methods for different error types
    
    public static ShopifyException rateLimitExceeded(String endpoint, int retryCount, Duration retryAfter) {
        return new ShopifyException(
            "Rate limit exceeded for endpoint: " + endpoint,
            ErrorType.RATE_LIMIT_EXCEEDED,
            429,
            "RATE_LIMIT_EXCEEDED",
            endpoint,
            null,
            retryCount,
            retryAfter,
            null
        );
    }
    
    public static ShopifyException authenticationFailed(String endpoint, String details) {
        return new ShopifyException(
            "Authentication failed: " + details,
            ErrorType.AUTHENTICATION_FAILED,
            401,
            "AUTHENTICATION_FAILED",
            endpoint,
            Map.of("details", details),
            0,
            null,
            null
        );
    }
    
    public static ShopifyException authorizationFailed(String endpoint, String details) {
        return new ShopifyException(
            "Authorization failed: " + details,
            ErrorType.AUTHORIZATION_FAILED,
            403,
            "AUTHORIZATION_FAILED",
            endpoint,
            Map.of("details", details),
            0,
            null,
            null
        );
    }
    
    public static ShopifyException resourceNotFound(String endpoint, String resourceId) {
        return new ShopifyException(
            "Resource not found: " + resourceId,
            ErrorType.RESOURCE_NOT_FOUND,
            404,
            "RESOURCE_NOT_FOUND",
            endpoint,
            Map.of("resourceId", resourceId),
            0,
            null,
            null
        );
    }
    
    public static ShopifyException validationError(String endpoint, Map<String, Object> validationErrors) {
        return new ShopifyException(
            "Validation error occurred",
            ErrorType.VALIDATION_ERROR,
            422,
            "VALIDATION_ERROR",
            endpoint,
            validationErrors,
            0,
            null,
            null
        );
    }
    
    public static ShopifyException serverError(String endpoint, int statusCode, String details) {
        return new ShopifyException(
            "Server error: " + details,
            ErrorType.SERVER_ERROR,
            statusCode,
            "SERVER_ERROR",
            endpoint,
            Map.of("details", details),
            0,
            null,
            null
        );
    }
    
    public static ShopifyException networkError(String endpoint, Throwable cause) {
        return new ShopifyException(
            "Network error occurred",
            ErrorType.NETWORK_ERROR,
            0,
            "NETWORK_ERROR",
            endpoint,
            null,
            0,
            null,
            cause
        );
    }
    
    public static ShopifyException timeoutError(String endpoint, Duration timeout) {
        return new ShopifyException(
            "Request timeout after " + timeout.toMillis() + "ms",
            ErrorType.TIMEOUT_ERROR,
            0,
            "TIMEOUT_ERROR",
            endpoint,
            Map.of("timeout", timeout.toMillis()),
            0,
            null,
            null
        );
    }
    
    // Getters
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public Map<String, Object> getErrorDetails() {
        return errorDetails;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public Duration getRetryAfter() {
        return retryAfter;
    }
    
    /**
     * Checks if this error is retryable.
     * @return true if the error can be retried
     */
    public boolean isRetryable() {
        return errorType.isRetryable();
    }
    
    /**
     * Checks if this error is related to rate limiting.
     * @return true if it's a rate limit error
     */
    public boolean isRateLimitError() {
        return errorType == ErrorType.RATE_LIMIT_EXCEEDED;
    }
    
    /**
     * Checks if this error is related to authentication.
     * @return true if it's an authentication error
     */
    public boolean isAuthenticationError() {
        return errorType == ErrorType.AUTHENTICATION_FAILED;
    }
    
    /**
     * Checks if this error is related to authorization.
     * @return true if it's an authorization error
     */
    public boolean isAuthorizationError() {
        return errorType == ErrorType.AUTHORIZATION_FAILED;
    }
    
    /**
     * Checks if this error is related to validation.
     * @return true if it's a validation error
     */
    public boolean isValidationError() {
        return errorType == ErrorType.VALIDATION_ERROR;
    }
    
    /**
     * Gets a user-friendly error message.
     * @return Human-readable error message
     */
    public String getUserMessage() {
        switch (errorType) {
            case RATE_LIMIT_EXCEEDED:
                return "Too many requests. Please wait before trying again.";
            case AUTHENTICATION_FAILED:
                return "Authentication failed. Please check your credentials.";
            case AUTHORIZATION_FAILED:
                return "You don't have permission to perform this action.";
            case RESOURCE_NOT_FOUND:
                return "The requested resource was not found.";
            case VALIDATION_ERROR:
                return "The request contains invalid data. Please check your input.";
            case SERVER_ERROR:
                return "A server error occurred. Please try again later.";
            case NETWORK_ERROR:
                return "A network error occurred. Please check your connection.";
            case TIMEOUT_ERROR:
                return "The request timed out. Please try again.";
            default:
                return getMessage();
        }
    }
    
    @Override
    public String toString() {
        return String.format("ShopifyException{errorType=%s, statusCode=%d, errorCode='%s', endpoint='%s', retryCount=%d, retryAfter=%s}",
                errorType, statusCode, errorCode, endpoint, retryCount, retryAfter);
    }
    
    /**
     * Enum representing different types of Shopify API errors.
     */
    public enum ErrorType {
        RATE_LIMIT_EXCEEDED(true),
        AUTHENTICATION_FAILED(false),
        AUTHORIZATION_FAILED(false),
        RESOURCE_NOT_FOUND(false),
        VALIDATION_ERROR(false),
        SERVER_ERROR(true),
        NETWORK_ERROR(true),
        TIMEOUT_ERROR(true);
        
        private final boolean retryable;
        
        ErrorType(boolean retryable) {
            this.retryable = retryable;
        }
        
        public boolean isRetryable() {
            return retryable;
        }
    }
}
