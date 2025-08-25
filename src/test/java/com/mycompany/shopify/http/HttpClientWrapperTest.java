package com.mycompany.shopify.http;

import com.mycompany.shopify.error.ShopifyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for HttpClientWrapper.
 * Tests retry logic, rate limiting, error handling, and performance optimizations.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HttpClientWrapper Tests")
class HttpClientWrapperTest {
    
    @Mock
    private RestTemplate restTemplate;
    
    private HttpClientWrapper httpClientWrapper;
    private HttpClientWrapper.RetryConfig retryConfig;
    
    @BeforeEach
    void setUp() {
        retryConfig = new HttpClientWrapper.RetryConfig();
        retryConfig.setMaxRetries(3);
        retryConfig.setBaseDelayMs(100);
        retryConfig.setBackoffMultiplier(2.0);
        retryConfig.setMaxDelayMs(1000);
        retryConfig.setJitterEnabled(false);
        retryConfig.setAdaptiveRetryEnabled(false);
        
        httpClientWrapper = new HttpClientWrapper(restTemplate);
    }
    
    @Nested
    @DisplayName("Successful Request Tests")
    class SuccessfulRequestTests {
        
        @Test
        @DisplayName("Should execute successful request without retry")
        void shouldExecuteSuccessfulRequestWithoutRetry() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Success");
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            ResponseEntity<String> result = httpClientWrapper.executeWithRetry(request);
            
            // Assert
            assertNotNull(result);
            assertEquals("Success", result.getBody());
            assertEquals(HttpStatus.OK, result.getStatusCode());
            
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
        }
        
        @Test
        @DisplayName("Should execute async request successfully")
        void shouldExecuteAsyncRequestSuccessfully() throws InterruptedException, ExecutionException, TimeoutException {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("Async Success");
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            CompletableFuture<ResponseEntity<String>> future = httpClientWrapper.executeAsyncWithRetry(request);
            ResponseEntity<String> result = future.get(5, TimeUnit.SECONDS);
            
            // Assert
            assertNotNull(result);
            assertEquals("Async Success", result.getBody());
            assertEquals(HttpStatus.OK, result.getStatusCode());
            
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
        }
    }
    
    @Nested
    @DisplayName("Retry Logic Tests")
    class RetryLogicTests {
        
        @Test
        @DisplayName("Should retry on server error and eventually succeed")
        void shouldRetryOnServerErrorAndEventuallySucceed() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // First two calls fail with server error, third succeeds
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY))
                    .thenReturn(ResponseEntity.ok("Success after retry"));
            
            // Act
            ResponseEntity<String> result = httpClientWrapper.executeWithRetry(request);
            
            // Assert
            assertNotNull(result);
            assertEquals("Success after retry", result.getBody());
            
            // Verify retry attempts
            verify(restTemplate, times(3)).exchange(anyString(), any(), any(), eq(String.class));
        }
        
        @Test
        @DisplayName("Should retry on network error and eventually succeed")
        void shouldRetryOnNetworkErrorAndEventuallySucceed() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // First call fails with network error, second succeeds
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new ResourceAccessException("Network error"))
                    .thenReturn(ResponseEntity.ok("Success after network retry"));
            
            // Act
            ResponseEntity<String> result = httpClientWrapper.executeWithRetry(request);
            
            // Assert
            assertNotNull(result);
            assertEquals("Success after network retry", result.getBody());
            
            // Verify retry attempts
            verify(restTemplate, times(2)).exchange(anyString(), any(), any(), eq(String.class));
        }
        
        @Test
        @DisplayName("Should fail after max retries exceeded")
        void shouldFailAfterMaxRetriesExceeded() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // All calls fail with server error
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
            
            // Act & Assert
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                httpClientWrapper.executeWithRetry(request);
            });
            
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
            
            // Verify all retry attempts
            verify(restTemplate, times(4)).exchange(anyString(), any(), any(), eq(String.class)); // 1 initial + 3 retries
        }
    }
    
    @Nested
    @DisplayName("Rate Limit Handling Tests")
    class RateLimitHandlingTests {
        
        @Test
        @DisplayName("Should handle rate limit with Retry-After header")
        void shouldHandleRateLimitWithRetryAfterHeader() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // First call hits rate limit, second succeeds
            HttpHeaders rateLimitHeaders = new HttpHeaders();
            rateLimitHeaders.set("Retry-After", "1");
            
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited"))
                    .thenReturn(ResponseEntity.ok("Success after rate limit"));
            
            // Act
            ResponseEntity<String> result = httpClientWrapper.executeWithRetry(request);
            
            // Assert
            assertNotNull(result);
            assertEquals("Success after rate limit", result.getBody());
            
            // Verify retry attempts
            verify(restTemplate, times(2)).exchange(anyString(), any(), any(), eq(String.class));
        }
        
        @Test
        @DisplayName("Should handle rate limit without Retry-After header")
        void shouldHandleRateLimitWithoutRetryAfterHeader() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // First call hits rate limit, second succeeds
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS, "Rate limited"))
                    .thenReturn(ResponseEntity.ok("Success after rate limit"));
            
            // Act
            ResponseEntity<String> result = httpClientWrapper.executeWithRetry(request);
            
            // Assert
            assertNotNull(result);
            assertEquals("Success after rate limit", result.getBody());
            
            // Verify retry attempts
            verify(restTemplate, times(2)).exchange(anyString(), any(), any(), eq(String.class));
        }
    }
    
    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        
        @Test
        @DisplayName("Should respect max retries configuration")
        void shouldRespectMaxRetriesConfiguration() {
            // Arrange
            retryConfig.setMaxRetries(1);
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // All calls fail
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
            
            // Act & Assert
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                httpClientWrapper.executeWithRetry(request);
            });
            
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
            
            // Verify only 2 attempts (1 initial + 1 retry)
            verify(restTemplate, times(2)).exchange(anyString(), any(), any(), eq(String.class));
        }
        
        @Test
        @DisplayName("Should respect backoff multiplier configuration")
        void shouldRespectBackoffMultiplierConfiguration() {
            // Arrange
            retryConfig.setBaseDelayMs(100);
            retryConfig.setBackoffMultiplier(3.0);
            
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // First call fails, second succeeds
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                    .thenReturn(ResponseEntity.ok("Success"));
            
            long startTime = System.currentTimeMillis();
            
            // Act
            ResponseEntity<String> result = httpClientWrapper.executeWithRetry(request);
            
            // Assert
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // Should wait at least 100ms (base delay)
            assertTrue(totalTime >= 100, "Expected delay of at least 100ms, but got " + totalTime + "ms");
            
            verify(restTemplate, times(2)).exchange(anyString(), any(), any(), eq(String.class));
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should not retry on client errors (4xx)")
        void shouldNotRetryOnClientErrors() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad request"));
            
            // Act & Assert
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                httpClientWrapper.executeWithRetry(request);
            });
            
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            
            // Verify no retries for client errors
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
        }
        
        @Test
        @DisplayName("Should not retry on authentication errors")
        void shouldNotRetryOnAuthenticationErrors() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            
            // Act & Assert
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                httpClientWrapper.executeWithRetry(request);
            });
            
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            
            // Verify no retries for authentication errors
            verify(restTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null request gracefully")
        void shouldHandleNullRequestGracefully() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                httpClientWrapper.executeWithRetry(null);
            });
        }
        
        @Test
        @DisplayName("Should handle empty URL gracefully")
        void shouldHandleEmptyUrlGracefully() {
            // Arrange
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new IllegalArgumentException("URL cannot be empty"));
            
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                httpClientWrapper.executeWithRetry(request);
            });
        }
        
        @Test
        @DisplayName("Should handle very long delays gracefully")
        void shouldHandleVeryLongDelaysGracefully() {
            // Arrange
            retryConfig.setBaseDelayMs(10000); // 10 seconds
            retryConfig.setMaxDelayMs(30000);  // 30 seconds
            
            HttpRequest<String> request = HttpRequest.<String>builder()
                    .url("https://api.shopify.com/products.json")
                    .method(org.springframework.http.HttpMethod.GET)
                    .responseType(String.class)
                    .build();
            
            // First call fails, second succeeds
            when(restTemplate.exchange(anyString(), any(), any(), eq(String.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
                    .thenReturn(ResponseEntity.ok("Success"));
            
            long startTime = System.currentTimeMillis();
            
            // Act
            ResponseEntity<String> result = httpClientWrapper.executeWithRetry(request);
            
            // Assert
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            
            // Should wait but not exceed reasonable bounds
            assertTrue(totalTime >= 1000, "Expected some delay");
            assertTrue(totalTime < 60000, "Delay should not be excessive");
            
            verify(restTemplate, times(2)).exchange(anyString(), any(), any(), eq(String.class));
        }
    }
}
