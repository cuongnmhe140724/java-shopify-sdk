package com.mycompany.shopify.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for PerformanceMonitor.
 * Tests performance tracking, statistics, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PerformanceMonitor Tests")
class PerformanceMonitorTest {
    
    private PerformanceMonitor performanceMonitor;
    
    @BeforeEach
    void setUp() {
        performanceMonitor = new PerformanceMonitor();
    }
    
    @Nested
    @DisplayName("Request Recording Tests")
    class RequestRecordingTests {
        
        @Test
        @DisplayName("Should record successful requests")
        void shouldRecordSuccessfulRequests() {
            // Arrange
            String endpoint = "/products.json";
            String method = "GET";
            long responseTime = 150;
            int statusCode = 200;
            
            // Act
            performanceMonitor.recordSuccess(endpoint, method, responseTime, statusCode);
            
            // Assert
            PerformanceMonitor.OverallStats stats = performanceMonitor.getOverallStats();
            assertEquals(1, stats.getTotalRequests());
            assertEquals(0, stats.getTotalErrors());
            assertEquals(1.0, stats.getSuccessRate());
            assertEquals(0.0, stats.getErrorRate());
            
            PerformanceMonitor.EndpointStats endpointStats = performanceMonitor.getEndpointStats(endpoint, method);
            assertNotNull(endpointStats);
            assertEquals(1, endpointStats.getTotalRequests());
            assertEquals(0, endpointStats.getTotalErrors());
            assertEquals(150, endpointStats.getAverageResponseTime());
            assertEquals(150, endpointStats.getMinResponseTime());
            assertEquals(150, endpointStats.getMaxResponseTime());
        }
        
        @Test
        @DisplayName("Should record failed requests")
        void shouldRecordFailedRequests() {
            // Arrange
            String endpoint = "/products.json";
            String method = "POST";
            long responseTime = 500;
            int statusCode = 500;
            RuntimeException error = new RuntimeException("Server error");
            
            // Act
            performanceMonitor.recordError(endpoint, method, responseTime, statusCode, error);
            
            // Assert
            PerformanceMonitor.OverallStats stats = performanceMonitor.getOverallStats();
            assertEquals(1, stats.getTotalRequests());
            assertEquals(1, stats.getTotalErrors());
            assertEquals(0.0, stats.getSuccessRate());
            assertEquals(1.0, stats.getErrorRate());
            
            PerformanceMonitor.EndpointStats endpointStats = performanceMonitor.getEndpointStats(endpoint, method);
            assertNotNull(endpointStats);
            assertEquals(1, endpointStats.getTotalRequests());
            assertEquals(1, endpointStats.getTotalErrors());
            assertEquals(500, endpointStats.getAverageResponseTime());
        }
        
        @Test
        @DisplayName("Should track multiple requests correctly")
        void shouldTrackMultipleRequestsCorrectly() {
            // Arrange
            String endpoint = "/products.json";
            String method = "GET";
            
            // Act - Record multiple requests with different response times
            performanceMonitor.recordSuccess(endpoint, method, 100, 200);
            performanceMonitor.recordSuccess(endpoint, method, 200, 200);
            performanceMonitor.recordSuccess(endpoint, method, 300, 200);
            
            // Assert
            PerformanceMonitor.EndpointStats endpointStats = performanceMonitor.getEndpointStats(endpoint, method);
            assertEquals(3, endpointStats.getTotalRequests());
            assertEquals(0, endpointStats.getTotalErrors());
            assertEquals(200, endpointStats.getAverageResponseTime());
            assertEquals(100, endpointStats.getMinResponseTime());
            assertEquals(300, endpointStats.getMaxResponseTime());
            assertEquals(1.0, endpointStats.getSuccessRate());
        }
        
        @Test
        @DisplayName("Should handle mixed success and error requests")
        void shouldHandleMixedSuccessAndErrorRequests() {
            // Arrange
            String endpoint = "/products.json";
            String method = "GET";
            
            // Act
            performanceMonitor.recordSuccess(endpoint, method, 100, 200);
            performanceMonitor.recordError(endpoint, method, 500, 500, new RuntimeException("Error"));
            performanceMonitor.recordSuccess(endpoint, method, 150, 200);
            
            // Assert
            PerformanceMonitor.EndpointStats endpointStats = performanceMonitor.getEndpointStats(endpoint, method);
            assertEquals(3, endpointStats.getTotalRequests());
            assertEquals(1, endpointStats.getTotalErrors());
            assertEquals(250, endpointStats.getAverageResponseTime());
            assertEquals(2.0/3.0, endpointStats.getSuccessRate(), 0.001);
            assertEquals(1.0/3.0, endpointStats.getErrorRate(), 0.001);
        }
    }
    
    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {
        
        @Test
        @DisplayName("Should provide accurate overall statistics")
        void shouldProvideAccurateOverallStatistics() {
            // Arrange
            performanceMonitor.recordSuccess("/products.json", "GET", 100, 200);
            performanceMonitor.recordSuccess("/orders.json", "GET", 200, 200);
            performanceMonitor.recordError("/customers.json", "POST", 300, 500, new RuntimeException("Error"));
            
            // Act
            PerformanceMonitor.OverallStats stats = performanceMonitor.getOverallStats();
            
            // Assert
            assertEquals(3, stats.getTotalRequests());
            assertEquals(1, stats.getTotalErrors());
            assertEquals(3, stats.getActiveEndpoints());
            assertEquals(200.0, stats.getAverageResponseTime().toMillis());
            assertEquals(2.0/3.0, stats.getSuccessRate(), 0.001);
            assertEquals(1.0/3.0, stats.getErrorRate(), 0.001);
        }
        
        @Test
        @DisplayName("Should handle zero requests gracefully")
        void shouldHandleZeroRequestsGracefully() {
            // Act
            PerformanceMonitor.OverallStats stats = performanceMonitor.getOverallStats();
            
            // Assert
            assertEquals(0, stats.getTotalRequests());
            assertEquals(0, stats.getTotalErrors());
            assertEquals(0, stats.getActiveEndpoints());
            assertEquals(0.0, stats.getErrorRate());
            assertEquals(1.0, stats.getSuccessRate());
        }
        
        @Test
        @DisplayName("Should calculate total response time correctly")
        void shouldCalculateTotalResponseTimeCorrectly() {
            // Arrange
            performanceMonitor.recordSuccess("/products.json", "GET", 100, 200);
            performanceMonitor.recordSuccess("/products.json", "GET", 200, 200);
            performanceMonitor.recordSuccess("/products.json", "GET", 300, 200);
            
            // Act
            PerformanceMonitor.OverallStats stats = performanceMonitor.getOverallStats();
            
            // Assert
            assertEquals(600, stats.getTotalResponseTime().toMillis());
        }
    }
    
    @Nested
    @DisplayName("Endpoint Statistics Tests")
    class EndpointStatisticsTests {
        
        @Test
        @DisplayName("Should track endpoint statistics separately")
        void shouldTrackEndpointStatisticsSeparately() {
            // Arrange
            performanceMonitor.recordSuccess("/products.json", "GET", 100, 200);
            performanceMonitor.recordSuccess("/products.json", "POST", 200, 201);
            performanceMonitor.recordSuccess("/orders.json", "GET", 300, 200);
            
            // Act
            PerformanceMonitor.EndpointStats productsGet = performanceMonitor.getEndpointStats("/products.json", "GET");
            PerformanceMonitor.EndpointStats productsPost = performanceMonitor.getEndpointStats("/products.json", "POST");
            PerformanceMonitor.EndpointStats ordersGet = performanceMonitor.getEndpointStats("/orders.json", "GET");
            
            // Assert
            assertNotNull(productsGet);
            assertEquals(1, productsGet.getTotalRequests());
            assertEquals(100, productsGet.getAverageResponseTime());
            
            assertNotNull(productsPost);
            assertEquals(1, productsPost.getTotalRequests());
            assertEquals(200, productsPost.getAverageResponseTime());
            
            assertNotNull(ordersGet);
            assertEquals(1, ordersGet.getTotalRequests());
            assertEquals(300, ordersGet.getAverageResponseTime());
        }
        
        @Test
        @DisplayName("Should return null for unknown endpoint")
        void shouldReturnNullForUnknownEndpoint() {
            // Act
            PerformanceMonitor.EndpointStats stats = performanceMonitor.getEndpointStats("/unknown.json", "GET");
            
            // Assert
            assertNull(stats);
        }
        
        @Test
        @DisplayName("Should handle endpoint with mixed methods")
        void shouldHandleEndpointWithMixedMethods() {
            // Arrange
            String endpoint = "/products.json";
            performanceMonitor.recordSuccess(endpoint, "GET", 100, 200);
            performanceMonitor.recordSuccess(endpoint, "POST", 200, 201);
            performanceMonitor.recordError(endpoint, "PUT", 300, 500, new RuntimeException("Error"));
            
            // Act
            PerformanceMonitor.EndpointStats getStats = performanceMonitor.getEndpointStats(endpoint, "GET");
            PerformanceMonitor.EndpointStats postStats = performanceMonitor.getEndpointStats(endpoint, "POST");
            PerformanceMonitor.EndpointStats putStats = performanceMonitor.getEndpointStats(endpoint, "PUT");
            
            // Assert
            assertEquals(1, getStats.getTotalRequests());
            assertEquals(0, getStats.getTotalErrors());
            
            assertEquals(1, postStats.getTotalRequests());
            assertEquals(0, postStats.getTotalErrors());
            
            assertEquals(1, putStats.getTotalRequests());
            assertEquals(1, putStats.getTotalErrors());
        }
    }
    
    @Nested
    @DisplayName("Performance Analysis Tests")
    class PerformanceAnalysisTests {
        
        @Test
        @DisplayName("Should identify top performing endpoints")
        void shouldIdentifyTopPerformingEndpoints() {
            // Arrange
            performanceMonitor.recordSuccess("/fast.json", "GET", 50, 200);
            performanceMonitor.recordSuccess("/medium.json", "GET", 100, 200);
            performanceMonitor.recordSuccess("/slow.json", "GET", 200, 200);
            
            // Act
            Map<String, Duration> topEndpoints = performanceMonitor.getTopPerformingEndpoints(3);
            
            // Assert
            assertEquals(3, topEndpoints.size());
            
            // Check order (fastest first)
            List<String> endpoints = List.copyOf(topEndpoints.keySet());
            assertTrue(topEndpoints.get(endpoints.get(1)).toMillis() <= topEndpoints.get(endpoints.get(0)).toMillis());
            assertTrue(topEndpoints.get(endpoints.get(2)).toMillis() <= topEndpoints.get(endpoints.get(1)).toMillis());
        }
        
        @Test
        @DisplayName("Should identify slowest endpoints")
        void shouldIdentifySlowestEndpoints() {
            // Arrange
            performanceMonitor.recordSuccess("/fast.json", "GET", 50, 200);
            performanceMonitor.recordSuccess("/medium.json", "GET", 100, 200);
            performanceMonitor.recordSuccess("/slow.json", "GET", 200, 200);
            
            // Act
            Map<String, Duration> slowestEndpoints = performanceMonitor.getSlowestEndpoints(3);
            
            // Assert
            assertEquals(3, slowestEndpoints.size());
            
            // Check order (slowest first)
            List<String> endpoints = List.copyOf(slowestEndpoints.keySet());
            assertTrue(slowestEndpoints.get(endpoints.get(0)).toMillis() >= slowestEndpoints.get(endpoints.get(1)).toMillis());
            assertTrue(slowestEndpoints.get(endpoints.get(1)).toMillis() >= slowestEndpoints.get(endpoints.get(2)).toMillis());
        }
        
        @Test
        @DisplayName("Should identify endpoints with highest error rates")
        void shouldIdentifyEndpointsWithHighestErrorRates() {
            // Arrange
            performanceMonitor.recordSuccess("/good.json", "GET", 100, 200);
            performanceMonitor.recordError("/bad.json", "GET", 100, 500, new RuntimeException("Error"));
            performanceMonitor.recordError("/bad.json", "GET", 100, 500, new RuntimeException("Error"));
            performanceMonitor.recordSuccess("/mixed.json", "GET", 100, 200);
            performanceMonitor.recordError("/mixed.json", "GET", 100, 500, new RuntimeException("Error"));
            
            // Act
            Map<String, Double> errorRates = performanceMonitor.getEndpointsWithHighestErrorRate(3);
            
            // Assert
            assertEquals(3, errorRates.size());
            
            // Check order (highest error rate first)
            List<String> endpoints = List.copyOf(errorRates.keySet());
            assertTrue(errorRates.get(endpoints.get(1)) >= errorRates.get(endpoints.get(0)));
            assertTrue(errorRates.get(endpoints.get(1)) >= errorRates.get(endpoints.get(2)));
            
            // Verify specific error rates
            assertEquals(1.0, errorRates.get("/bad.json:GET")); // 2 errors / 2 requests
            assertEquals(0.5, errorRates.get("/mixed.json:GET")); // 1 error / 2 requests
            assertEquals(0.0, errorRates.get("/good.json:GET")); // 0 errors / 1 request
        }
    }
    
    @Nested
    @DisplayName("Recent Requests Tests")
    class RecentRequestsTests {
        
        @Test
        @DisplayName("Should track recent requests")
        void shouldTrackRecentRequests() {
            // Arrange
            performanceMonitor.recordSuccess("/products.json", "GET", 100, 200);
            performanceMonitor.recordSuccess("/orders.json", "GET", 200, 200);
            performanceMonitor.recordSuccess("/customers.json", "GET", 300, 200);
            
            // Act
            List<PerformanceMonitor.RequestMetric> recentRequests = performanceMonitor.getRecentRequests(5);
            
            // Assert
            assertEquals(3, recentRequests.size());
            
            // Check order (most recent first)
            PerformanceMonitor.RequestMetric first = recentRequests.get(0);
            PerformanceMonitor.RequestMetric last = recentRequests.get(2);
            
            assertTrue(first.getTimestamp().isAfter(last.getTimestamp()) || first.getTimestamp().equals(last.getTimestamp()));
        }
        
        @Test
        @DisplayName("Should limit recent requests to specified count")
        void shouldLimitRecentRequestsToSpecifiedCount() {
            // Arrange
            for (int i = 0; i < 10; i++) {
                performanceMonitor.recordSuccess("/products.json", "GET", 100, 200);
            }
            
            // Act
            List<PerformanceMonitor.RequestMetric> recentRequests = performanceMonitor.getRecentRequests(5);
            
            // Assert
            assertEquals(5, recentRequests.size());
        }
        
        @Test
        @DisplayName("Should maintain recent requests queue size")
        void shouldMaintainRecentRequestsQueueSize() {
            // Arrange - Make more than MAX_RECENT_REQUESTS
            for (int i = 0; i < 1500; i++) {
                performanceMonitor.recordSuccess("/products.json", "GET", 100, 200);
            }
            
            // Act
            List<PerformanceMonitor.RequestMetric> recentRequests = performanceMonitor.getRecentRequests(1000);
            
            // Assert - Should not exceed MAX_RECENT_REQUESTS
            assertTrue(recentRequests.size() <= 1000);
        }
    }
    
    @Nested
    @DisplayName("Reset Tests")
    class ResetTests {
        
        @Test
        @DisplayName("Should reset all statistics")
        void shouldResetAllStatistics() {
            // Arrange
            performanceMonitor.recordSuccess("/products.json", "GET", 100, 200);
            performanceMonitor.recordError("/orders.json", "POST", 200, 500, new RuntimeException("Error"));
            
            // Verify data exists
            PerformanceMonitor.OverallStats statsBefore = performanceMonitor.getOverallStats();
            assertEquals(2, statsBefore.getTotalRequests());
            assertEquals(1, statsBefore.getTotalErrors());
            
            // Act
            performanceMonitor.reset();
            
            // Assert
            PerformanceMonitor.OverallStats statsAfter = performanceMonitor.getOverallStats();
            assertEquals(0, statsAfter.getTotalRequests());
            assertEquals(0, statsAfter.getTotalErrors());
            assertEquals(0, statsAfter.getActiveEndpoints());
            
            // Endpoint stats should also be cleared
            assertNull(performanceMonitor.getEndpointStats("/products.json", "GET"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle very long response times")
        void shouldHandleVeryLongResponseTimes() {
            // Arrange
            long veryLongResponseTime = Long.MAX_VALUE / 2;
            performanceMonitor.recordSuccess("/slow.json", "GET", veryLongResponseTime, 200);
            
            // Act
            PerformanceMonitor.EndpointStats stats = performanceMonitor.getEndpointStats("/slow.json", "GET");
            
            // Assert
            assertEquals(veryLongResponseTime, stats.getMaxResponseTime());
            assertEquals(veryLongResponseTime, stats.getAverageResponseTime());
        }
        
        @Test
        @DisplayName("Should handle zero response time")
        void shouldHandleZeroResponseTime() {
            // Arrange
            performanceMonitor.recordSuccess("/instant.json", "GET", 0, 200);
            
            // Act
            PerformanceMonitor.EndpointStats stats = performanceMonitor.getEndpointStats("/instant.json", "GET");
            
            // Assert
            assertEquals(0, stats.getMinResponseTime());
            assertEquals(0, stats.getAverageResponseTime());
            assertEquals(0, stats.getMaxResponseTime());
        }
        
        @Test
        @DisplayName("Should handle concurrent access safely")
        void shouldHandleConcurrentAccessSafely() throws InterruptedException {
            // Arrange
            Thread[] threads = new Thread[10];
            for (int i = 0; i < 10; i++) {
                threads[i] = new Thread(() -> {
                    for (int j = 0; j < 100; j++) {
                        performanceMonitor.recordSuccess("/concurrent.json", "GET", 100, 200);
                    }
                });
            }
            
            // Act - Start all threads
            for (Thread thread : threads) {
                thread.start();
            }
            
            // Wait for all threads to complete
            for (Thread thread : threads) {
                thread.join();
            }
            
            // Assert
            PerformanceMonitor.OverallStats stats = performanceMonitor.getOverallStats();
            assertEquals(1000, stats.getTotalRequests()); // 10 threads * 100 requests each
        }
    }
}
