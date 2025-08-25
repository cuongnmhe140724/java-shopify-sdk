package com.mycompany.shopify.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for ShopifyException.
 * Tests all factory methods, error types, and utility methods.
 */
@DisplayName("ShopifyException Tests")
class ShopifyExceptionTest {
    
    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {
        
        @Test
        @DisplayName("Should create rate limit exceeded exception")
        void shouldCreateRateLimitExceededException() {
            // Arrange
            String endpoint = "/products.json";
            int retryCount = 3;
            Duration retryAfter = Duration.ofSeconds(30);
            
            // Act
            ShopifyException exception = ShopifyException.rateLimitExceeded(endpoint, retryCount, retryAfter);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.RATE_LIMIT_EXCEEDED, exception.getErrorType());
            assertEquals(429, exception.getStatusCode());
            assertEquals("RATE_LIMIT_EXCEEDED", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertEquals(retryCount, exception.getRetryCount());
            assertEquals(retryAfter, exception.getRetryAfter());
            assertTrue(exception.isRetryable());
            assertTrue(exception.isRateLimitError());
        }
        
        @Test
        @DisplayName("Should create authentication failed exception")
        void shouldCreateAuthenticationFailedException() {
            // Arrange
            String endpoint = "/products.json";
            String details = "Invalid API key";
            
            // Act
            ShopifyException exception = ShopifyException.authenticationFailed(endpoint, details);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.AUTHENTICATION_FAILED, exception.getErrorType());
            assertEquals(401, exception.getStatusCode());
            assertEquals("AUTHENTICATION_FAILED", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertEquals(0, exception.getRetryCount());
            assertNull(exception.getRetryAfter());
            assertFalse(exception.isRetryable());
            assertTrue(exception.isAuthenticationError());
            
            // Check error details
            Map<String, Object> errorDetails = exception.getErrorDetails();
            assertNotNull(errorDetails);
            assertEquals(details, errorDetails.get("details"));
        }
        
        @Test
        @DisplayName("Should create authorization failed exception")
        void shouldCreateAuthorizationFailedException() {
            // Arrange
            String endpoint = "/products.json";
            String details = "Insufficient permissions";
            
            // Act
            ShopifyException exception = ShopifyException.authorizationFailed(endpoint, details);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.AUTHORIZATION_FAILED, exception.getErrorType());
            assertEquals(403, exception.getStatusCode());
            assertEquals("AUTHORIZATION_FAILED", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertFalse(exception.isRetryable());
            assertTrue(exception.isAuthorizationError());
        }
        
        @Test
        @DisplayName("Should create resource not found exception")
        void shouldCreateResourceNotFoundException() {
            // Arrange
            String endpoint = "/products/999.json";
            String resourceId = "999";
            
            // Act
            ShopifyException exception = ShopifyException.resourceNotFound(endpoint, resourceId);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.RESOURCE_NOT_FOUND, exception.getErrorType());
            assertEquals(404, exception.getStatusCode());
            assertEquals("RESOURCE_NOT_FOUND", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertFalse(exception.isRetryable());
            
            // Check error details
            Map<String, Object> errorDetails = exception.getErrorDetails();
            assertNotNull(errorDetails);
            assertEquals(resourceId, errorDetails.get("resourceId"));
        }
        
        @Test
        @DisplayName("Should create validation error exception")
        void shouldCreateValidationErrorException() {
            // Arrange
            String endpoint = "/products.json";
            Map<String, Object> validationErrors = new HashMap<>();
            validationErrors.put("title", "Title is required");
            validationErrors.put("price", "Price must be positive");
            
            // Act
            ShopifyException exception = ShopifyException.validationError(endpoint, validationErrors);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.VALIDATION_ERROR, exception.getErrorType());
            assertEquals(422, exception.getStatusCode());
            assertEquals("VALIDATION_ERROR", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertFalse(exception.isRetryable());
            assertTrue(exception.isValidationError());
            
            // Check error details
            Map<String, Object> errorDetails = exception.getErrorDetails();
            assertNotNull(errorDetails);
            assertEquals(validationErrors, errorDetails);
        }
        
        @Test
        @DisplayName("Should create server error exception")
        void shouldCreateServerErrorException() {
            // Arrange
            String endpoint = "/products.json";
            int statusCode = 500;
            String details = "Internal server error";
            
            // Act
            ShopifyException exception = ShopifyException.serverError(endpoint, statusCode, details);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.SERVER_ERROR, exception.getErrorType());
            assertEquals(statusCode, exception.getStatusCode());
            assertEquals("SERVER_ERROR", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertTrue(exception.isRetryable());
            
            // Check error details
            Map<String, Object> errorDetails = exception.getErrorDetails();
            assertNotNull(errorDetails);
            assertEquals(details, errorDetails.get("details"));
        }
        
        @Test
        @DisplayName("Should create network error exception")
        void shouldCreateNetworkErrorException() {
            // Arrange
            String endpoint = "/products.json";
            RuntimeException cause = new RuntimeException("Connection timeout");
            
            // Act
            ShopifyException exception = ShopifyException.networkError(endpoint, cause);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.NETWORK_ERROR, exception.getErrorType());
            assertEquals(0, exception.getStatusCode());
            assertEquals("NETWORK_ERROR", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertTrue(exception.isRetryable());
            assertEquals(cause, exception.getCause());
        }
        
        @Test
        @DisplayName("Should create timeout error exception")
        void shouldCreateTimeoutErrorException() {
            // Arrange
            String endpoint = "/products.json";
            Duration timeout = Duration.ofSeconds(30);
            
            // Act
            ShopifyException exception = ShopifyException.timeoutError(endpoint, timeout);
            
            // Assert
            assertNotNull(exception);
            assertEquals(ShopifyException.ErrorType.TIMEOUT_ERROR, exception.getErrorType());
            assertEquals(0, exception.getStatusCode());
            assertEquals("TIMEOUT_ERROR", exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertTrue(exception.isRetryable());
            
            // Check error details
            Map<String, Object> errorDetails = exception.getErrorDetails();
            assertNotNull(errorDetails);
            assertEquals(30000L, errorDetails.get("timeout"));
        }
    }
    
    @Nested
    @DisplayName("Error Type Tests")
    class ErrorTypeTests {
        
        @Test
        @DisplayName("Should have correct retryable flags")
        void shouldHaveCorrectRetryableFlags() {
            // Assert
            assertTrue(ShopifyException.ErrorType.RATE_LIMIT_EXCEEDED.isRetryable());
            assertFalse(ShopifyException.ErrorType.AUTHENTICATION_FAILED.isRetryable());
            assertFalse(ShopifyException.ErrorType.AUTHORIZATION_FAILED.isRetryable());
            assertFalse(ShopifyException.ErrorType.RESOURCE_NOT_FOUND.isRetryable());
            assertFalse(ShopifyException.ErrorType.VALIDATION_ERROR.isRetryable());
            assertTrue(ShopifyException.ErrorType.SERVER_ERROR.isRetryable());
            assertTrue(ShopifyException.ErrorType.NETWORK_ERROR.isRetryable());
            assertTrue(ShopifyException.ErrorType.TIMEOUT_ERROR.isRetryable());
        }
    }
    
    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {
        
        @Test
        @DisplayName("Should provide user-friendly messages")
        void shouldProvideUserFriendlyMessages() {
            // Arrange
            ShopifyException rateLimitException = ShopifyException.rateLimitExceeded("/products.json", 1, Duration.ofSeconds(30));
            ShopifyException authException = ShopifyException.authenticationFailed("/products.json", "Invalid key");
            ShopifyException validationException = ShopifyException.validationError("/products.json", new HashMap<>());
            
            // Act & Assert
            assertEquals("Too many requests. Please wait before trying again.", rateLimitException.getUserMessage());
            assertEquals("Authentication failed. Please check your credentials.", authException.getUserMessage());
            assertEquals("The request contains invalid data. Please check your input.", validationException.getUserMessage());
        }
        
        @Test
        @DisplayName("Should check error types correctly")
        void shouldCheckErrorTypesCorrectly() {
            // Arrange
            ShopifyException rateLimitException = ShopifyException.rateLimitExceeded("/products.json", 1, Duration.ofSeconds(30));
            ShopifyException authException = ShopifyException.authenticationFailed("/products.json", "Invalid key");
            ShopifyException authzException = ShopifyException.authorizationFailed("/products.json", "No permission");
            ShopifyException validationException = ShopifyException.validationError("/products.json", new HashMap<>());
            
            // Act & Assert
            assertTrue(rateLimitException.isRateLimitError());
            assertFalse(rateLimitException.isAuthenticationError());
            assertFalse(rateLimitException.isAuthorizationError());
            assertFalse(rateLimitException.isValidationError());
            
            assertFalse(authException.isRateLimitError());
            assertTrue(authException.isAuthenticationError());
            assertFalse(authException.isAuthorizationError());
            assertFalse(authException.isValidationError());
            
            assertFalse(authzException.isRateLimitError());
            assertTrue(authzException.isAuthorizationError());
            assertTrue(authzException.isAuthorizationError());
            assertFalse(authzException.isValidationError());
            
            assertFalse(validationException.isRateLimitError());
            assertFalse(validationException.isAuthenticationError());
            assertFalse(validationException.isAuthorizationError());
            assertTrue(validationException.isValidationError());
        }
    }
    
    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        
        @Test
        @DisplayName("Should create exception with all fields")
        void shouldCreateExceptionWithAllFields() {
            // Arrange
            String message = "Test error message";
            ShopifyException.ErrorType errorType = ShopifyException.ErrorType.VALIDATION_ERROR;
            int statusCode = 422;
            String errorCode = "TEST_ERROR";
            String endpoint = "/test.json";
            Map<String, Object> errorDetails = Map.of("field", "test");
            int retryCount = 2;
            Duration retryAfter = Duration.ofSeconds(15);
            RuntimeException cause = new RuntimeException("Root cause");
            
            // Act
            ShopifyException exception = new ShopifyException(
                message, errorType, statusCode, errorCode, endpoint, 
                errorDetails, retryCount, retryAfter, cause
            );
            
            // Assert
            assertEquals(message, exception.getMessage());
            assertEquals(errorType, exception.getErrorType());
            assertEquals(statusCode, exception.getStatusCode());
            assertEquals(errorCode, exception.getErrorCode());
            assertEquals(endpoint, exception.getEndpoint());
            assertEquals(errorDetails, exception.getErrorDetails());
            assertEquals(retryCount, exception.getRetryCount());
            assertEquals(retryAfter, exception.getRetryAfter());
            assertEquals(cause, exception.getCause());
            assertNotNull(exception.getTimestamp());
        }
    }
    
    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {
        
        @Test
        @DisplayName("Should provide meaningful string representation")
        void shouldProvideMeaningfulStringRepresentation() {
            // Arrange
            ShopifyException exception = ShopifyException.rateLimitExceeded("/products.json", 3, Duration.ofSeconds(30));
            
            // Act
            String stringRep = exception.toString();
            
            // Assert
            assertNotNull(stringRep);
            assertTrue(stringRep.contains("ShopifyException"));
            assertTrue(stringRep.contains("RATE_LIMIT_EXCEEDED"));
            assertTrue(stringRep.contains("429"));
            assertTrue(stringRep.contains("/products.json"));
            assertTrue(stringRep.contains("3"));
            assertTrue(stringRep.contains("30"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null error details")
        void shouldHandleNullErrorDetails() {
            // Act
            ShopifyException exception = ShopifyException.rateLimitExceeded("/products.json", 1, Duration.ofSeconds(30));
            
            // Assert
            assertNull(exception.getErrorDetails());
        }
        
        @Test
        @DisplayName("Should handle null retry after")
        void shouldHandleNullRetryAfter() {
            // Act
            ShopifyException exception = ShopifyException.authenticationFailed("/products.json", "Invalid key");
            
            // Assert
            assertNull(exception.getRetryAfter());
        }
        
        @Test
        @DisplayName("Should handle null cause")
        void shouldHandleNullCause() {
            // Act
            ShopifyException exception = ShopifyException.rateLimitExceeded("/products.json", 1, Duration.ofSeconds(30));
            
            // Assert
            assertNull(exception.getCause());
        }
        
        @Test
        @DisplayName("Should handle empty error details map")
        void shouldHandleEmptyErrorDetailsMap() {
            // Arrange
            Map<String, Object> emptyMap = new HashMap<>();
            
            // Act
            ShopifyException exception = ShopifyException.validationError("/products.json", emptyMap);
            
            // Assert
            assertNotNull(exception.getErrorDetails());
            assertTrue(exception.getErrorDetails().isEmpty());
        }
    }
}
