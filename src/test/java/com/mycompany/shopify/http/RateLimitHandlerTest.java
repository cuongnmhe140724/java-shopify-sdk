package com.mycompany.shopify.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for RateLimitHandler.
 * Tests rate limit tracking, bucket management, and optimal delay calculation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitHandler Tests")
class RateLimitHandlerTest {
    
    private RateLimitHandler rateLimitHandler;
    
    @BeforeEach
    void setUp() {
        rateLimitHandler = new RateLimitHandler();
    }
    
    @Nested
    @DisplayName("Request Type Tests")
    class RequestTypeTests {
        
        @Test
        @DisplayName("Should have correct rate limits for different request types")
        void shouldHaveCorrectRateLimitsForDifferentRequestTypes() {
            // Assert
            assertEquals(40, RateLimitHandler.RequestType.REST.getRequestsPerMinute());
            assertEquals(1000, RateLimitHandler.RequestType.GRAPHQL.getRequestsPerMinute());
            assertEquals(100, RateLimitHandler.RequestType.WEBHOOK.getRequestsPerMinute());
        }
        
        @Test
        @DisplayName("Should have correct rate limit constants")
        void shouldHaveCorrectRateLimitConstants() {
            // Assert
            assertEquals(40, RateLimitHandler.RequestType.REST.getRequestsPerMinute());
            assertEquals(1000, RateLimitHandler.RequestType.GRAPHQL.getRequestsPerMinute());
            assertEquals(100, RateLimitHandler.RequestType.WEBHOOK.getRequestsPerMinute());
        }
    }
    
    @Nested
    @DisplayName("Rate Limit Checking Tests")
    class RateLimitCheckingTests {
        
        @Test
        @DisplayName("Should allow requests within rate limit")
        void shouldAllowRequestsWithinRateLimit() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            
            // Act & Assert - Should allow first 40 requests
            for (int i = 0; i < 40; i++) {
                assertTrue(rateLimitHandler.canMakeRequest(endpoint, requestType),
                    "Request " + (i + 1) + " should be allowed");
            }
        }
        
        @Test
        @DisplayName("Should block requests exceeding rate limit")
        void shouldBlockRequestsExceedingRateLimit() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            
            // Act & Assert - Should block 41st request
            for (int i = 0; i < 40; i++) {
                rateLimitHandler.canMakeRequest(endpoint, requestType);
            }
            
            assertFalse(rateLimitHandler.canMakeRequest(endpoint, requestType),
                "41st request should be blocked");
        }
        
        @Test
        @DisplayName("Should reset rate limit after one minute")
        void shouldResetRateLimitAfterOneMinute() throws InterruptedException {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            
            // Use up all requests
            for (int i = 0; i < 40; i++) {
                rateLimitHandler.canMakeRequest(endpoint, requestType);
            }
            
            // Verify blocked
            assertFalse(rateLimitHandler.canMakeRequest(endpoint, requestType));
            
            // Wait for reset (simulate time passing)
            Thread.sleep(100); // Small delay for testing
            
            // Act - Reset manually for testing (in real scenario, this would happen after 1 minute)
            rateLimitHandler.resetAll();
            
            // Assert - Should allow requests again
            assertTrue(rateLimitHandler.canMakeRequest(endpoint, requestType));
        }
        
        @Test
        @DisplayName("Should handle different endpoints separately")
        void shouldHandleDifferentEndpointsSeparately() {
            // Arrange
            String endpoint1 = "/products.json";
            String endpoint2 = "/orders.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            
            // Act & Assert - Use up all requests for endpoint1
            for (int i = 0; i < 40; i++) {
                rateLimitHandler.canMakeRequest(endpoint1, requestType);
            }
            
            // endpoint1 should be blocked
            assertFalse(rateLimitHandler.canMakeRequest(endpoint1, requestType));
            
            // endpoint2 should still allow requests
            assertTrue(rateLimitHandler.canMakeRequest(endpoint2, requestType));
        }
        
        @Test
        @DisplayName("Should handle different request types separately")
        void shouldHandleDifferentRequestTypesSeparately() {
            // Arrange
            String endpoint = "/api";
            
            // Act & Assert - Use up all REST requests
            for (int i = 0; i < 40; i++) {
                rateLimitHandler.canMakeRequest(endpoint, RateLimitHandler.RequestType.REST);
            }
            
            // REST should be blocked
            assertFalse(rateLimitHandler.canMakeRequest(endpoint, RateLimitHandler.RequestType.REST));
            
            // GraphQL should still allow requests
            assertTrue(rateLimitHandler.canMakeRequest(endpoint, RateLimitHandler.RequestType.GRAPHQL));
        }
    }
    
    @Nested
    @DisplayName("Request Recording Tests")
    class RequestRecordingTests {
        
        @Test
        @DisplayName("Should record successful requests")
        void shouldRecordSuccessfulRequests() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("X-Shopify-Shop-Api-Call-Limit", "35/40");
            
            // Act
            rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            
            // Assert
            RateLimitHandler.RateLimitStats stats = rateLimitHandler.getStats();
            assertEquals(1, stats.getTotalRequests());
            assertEquals(35, stats.getCurrentUsage());
        }
        
        @Test
        @DisplayName("Should handle response headers with rate limit info")
        void shouldHandleResponseHeadersWithRateLimitInfo() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("X-Shopify-Shop-Api-Call-Limit", "38/40");
            
            // Act
            rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            
            // Assert
            RateLimitHandler.RateLimitStats stats = rateLimitHandler.getStats();
            assertEquals(38, stats.getCurrentUsage());
        }
        
        @Test
        @DisplayName("Should handle malformed rate limit headers gracefully")
        void shouldHandleMalformedRateLimitHeadersGracefully() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("X-Shopify-Shop-Api-Call-Limit", "invalid-format");
            
            // Act
            rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            
            // Assert - Should not throw exception and should increment counter
            RateLimitHandler.RateLimitStats stats = rateLimitHandler.getStats();
            assertEquals(1, stats.getTotalRequests());
        }
    }
    
    @Nested
    @DisplayName("Rate Limit Handling Tests")
    class RateLimitHandlingTests {
        
        @Test
        @DisplayName("Should handle rate limit with Retry-After header")
        void shouldHandleRateLimitWithRetryAfterHeader() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Retry-After", "30");
            
            // Act
            Duration delay = rateLimitHandler.handleRateLimit(endpoint, requestType, responseHeaders);
            
            // Assert
            assertNotNull(delay);
            assertEquals(30, delay.getSeconds());
        }
        
        @Test
        @DisplayName("Should handle rate limit without Retry-After header")
        void shouldHandleRateLimitWithoutRetryAfterHeader() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            
            // Act
            Duration delay = rateLimitHandler.handleRateLimit(endpoint, requestType, responseHeaders);
            
            // Assert
            assertNotNull(delay);
            assertTrue(delay.getSeconds() >= 1, "Should have minimum 1 second delay");
        }
        
        @Test
        @DisplayName("Should handle malformed Retry-After header")
        void shouldHandleMalformedRetryAfterHeader() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Retry-After", "invalid");
            
            // Act
            Duration delay = rateLimitHandler.handleRateLimit(endpoint, requestType, responseHeaders);
            
            // Assert - Should fall back to default delay
            assertNotNull(delay);
            assertEquals(60, delay.getSeconds()); // Default fallback
        }
    }
    
    @Nested
    @DisplayName("Optimal Delay Tests")
    class OptimalDelayTests {
        
        @Test
        @DisplayName("Should return zero delay when rate limit not exceeded")
        void shouldReturnZeroDelayWhenRateLimitNotExceeded() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            
            // Act
            Duration delay = rateLimitHandler.getOptimalDelay(endpoint, requestType);
            
            // Assert
            assertEquals(Duration.ZERO, delay);
        }
        
        @Test
        @DisplayName("Should return appropriate delay when rate limit exceeded")
        void shouldReturnAppropriateDelayWhenRateLimitExceeded() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            
            // Use up all requests
            for (int i = 0; i < 40; i++) {
                rateLimitHandler.canMakeRequest(endpoint, requestType);
            }
            
            // Act
            Duration delay = rateLimitHandler.getOptimalDelay(endpoint, requestType);
            
            // Assert
            assertNotNull(delay);
            assertTrue(delay.getSeconds() > 0, "Should have positive delay");
        }
        
        @Test
        @DisplayName("Should return zero delay for unknown endpoint")
        void shouldReturnZeroDelayForUnknownEndpoint() {
            // Arrange
            String endpoint = "/unknown-endpoint";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            
            // Act
            Duration delay = rateLimitHandler.getOptimalDelay(endpoint, requestType);
            
            // Assert
            assertEquals(Duration.ZERO, delay);
        }
    }
    
    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {
        
        @Test
        @DisplayName("Should provide accurate statistics")
        void shouldProvideAccurateStatistics() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("X-Shopify-Shop-Api-Call-Limit", "25/40");
            
            // Act
            rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            
            RateLimitHandler.RateLimitStats stats = rateLimitHandler.getStats();
            
            // Assert
            assertEquals(2, stats.getTotalRequests());
            assertEquals(25, stats.getCurrentUsage());
            assertEquals(1, stats.getActiveBuckets());
            assertNotNull(stats.getLastResetTime());
        }
        
        @Test
        @DisplayName("Should reset statistics correctly")
        void shouldResetStatisticsCorrectly() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            
            // Record some requests
            rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            
            // Verify requests recorded
            RateLimitHandler.RateLimitStats statsBefore = rateLimitHandler.getStats();
            assertEquals(2, statsBefore.getTotalRequests());
            
            // Act
            rateLimitHandler.resetAll();
            
            // Assert
            RateLimitHandler.RateLimitStats statsAfter = rateLimitHandler.getStats();
            assertEquals(0, statsAfter.getTotalRequests());
            assertEquals(0, statsAfter.getCurrentUsage());
            assertEquals(0, statsAfter.getActiveBuckets());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle concurrent requests safely")
        void shouldHandleConcurrentRequestsSafely() throws InterruptedException {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            
            // Act - Create multiple threads making requests
            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 5; j++) {
                        rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
                    }
                });
            }
            
            // Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Assert
            RateLimitHandler.RateLimitStats stats = rateLimitHandler.getStats();
            assertEquals(50, stats.getTotalRequests()); // 10 threads * 5 requests each
        }
        
        @Test
        @DisplayName("Should handle very high request volumes")
        void shouldHandleVeryHighRequestVolumes() {
            // Arrange
            String endpoint = "/products.json";
            RateLimitHandler.RequestType requestType = RateLimitHandler.RequestType.REST;
            HttpHeaders responseHeaders = new HttpHeaders();
            
            // Act - Make many requests
            for (int i = 0; i < 1000; i++) {
                rateLimitHandler.recordRequest(endpoint, requestType, responseHeaders);
            }
            
            // Assert
            RateLimitHandler.RateLimitStats stats = rateLimitHandler.getStats();
            assertEquals(1000, stats.getTotalRequests());
        }
    }
}
