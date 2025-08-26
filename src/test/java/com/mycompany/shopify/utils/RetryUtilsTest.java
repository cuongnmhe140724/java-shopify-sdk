package com.mycompany.shopify.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for RetryUtils.
 * Tests retry logic, circuit breaker pattern, and edge cases.
 */
@DisplayName("RetryUtils Tests")
class RetryUtilsTest {
    
    @Nested
    @DisplayName("Basic Retry Tests")
    class BasicRetryTests {
        
        @Test
        @DisplayName("Should execute successfully without retry")
        void shouldExecuteSuccessfullyWithoutRetry() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetry(() -> {
                attempts.incrementAndGet();
                return "Success";
            });
            
            // Assert
            assertEquals("Success", result);
            assertEquals(1, attempts.get());
        }
        
        @Test
        @DisplayName("Should retry and eventually succeed")
        void shouldRetryAndEventuallySucceed() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetry(() -> {
                int attempt = attempts.incrementAndGet();
                if (attempt < 3) {
                    throw new RuntimeException("Attempt " + attempt + " failed");
                }
                return "Success after " + attempt + " attempts";
            });
            
            // Assert
            assertEquals("Success after 3 attempts", result);
            assertEquals(3, attempts.get());
        }
        
        @Test
        @DisplayName("Should fail after max retries exceeded")
        void shouldFailAfterMaxRetriesExceeded() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithRetry(() -> {
                    attempts.incrementAndGet();
                    throw new RuntimeException("Always fails");
                });
            });
            
            assertEquals("Max retries exceeded", exception.getMessage());
            assertEquals(4, attempts.get()); // 1 initial + 3 retries
        }
        
        @Test
        @DisplayName("Should respect custom retry configuration")
        void shouldRespectCustomRetryConfiguration() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            Duration baseDelay = Duration.ofMillis(100);
            double backoffMultiplier = 2.0;
            
            // Act
            String result = RetryUtils.executeWithRetry(() -> {
                int attempt = attempts.incrementAndGet();
                if (attempt < 2) {
                    throw new RuntimeException("Attempt " + attempt + " failed");
                }
                return "Success";
            }, 2, baseDelay, backoffMultiplier);
            
            // Assert
            assertEquals("Success", result);
            assertEquals(2, attempts.get());
        }
    }
    
    @Nested
    @DisplayName("Async Retry Tests")
    class AsyncRetryTests {
        
        @Test
        @DisplayName("Should execute async retry successfully")
        void shouldExecuteAsyncRetrySuccessfully() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetryAsync(() -> {
                attempts.incrementAndGet();
                return "Async Success";
            }).join();
            
            // Assert
            assertEquals("Async Success", result);
            assertEquals(1, attempts.get());
        }
        
        @Test
        @DisplayName("Should handle async retry with failures")
        void shouldHandleAsyncRetryWithFailures() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetryAsync(() -> {
                int attempt = attempts.incrementAndGet();
                if (attempt < 2) {
                    throw new RuntimeException("Attempt " + attempt + " failed");
                }
                return "Async Success after " + attempt + " attempts";
            }).join();
            
            // Assert
            assertEquals("Async Success after 2 attempts", result);
            assertEquals(2, attempts.get());
        }
    }
    
    @Nested
    @DisplayName("Conditional Retry Tests")
    class ConditionalRetryTests {
        
        @Test
        @DisplayName("Should retry based on result condition")
        void shouldRetryBasedOnResultCondition() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetryCondition(
                () -> {
                    int attempt = attempts.incrementAndGet();
                    return "Result " + attempt;
                },
                (resultValue, attempt, lastException) -> {
                    // Retry if result doesn't contain "3"
                    return !resultValue.contains("3");
                },
                3, Duration.ofMillis(50), 1.5
            );
            
            // Assert
            assertEquals("Result 3", result);
            assertEquals(3, attempts.get());
        }
        
        @Test
        @DisplayName("Should retry based on exception condition")
        void shouldRetryBasedOnExceptionCondition() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetryCondition(
                () -> {
                    int attempt = attempts.incrementAndGet();
                    if (attempt < 3) {
                        throw new RuntimeException("Attempt " + attempt + " failed");
                    }
                    return "Success";
                },
                (resultValue, attempt, lastException) -> {
                    // Retry on any exception
                    return lastException != null;
                },
                3, Duration.ofMillis(50), 1.5
            );
            
            // Assert
            assertEquals("Success", result);
            assertEquals(4, attempts.get());
        }
        
        @Test
        @DisplayName("Should not retry when condition not met")
        void shouldNotRetryWhenConditionNotMet() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetryCondition(
                () -> {
                    attempts.incrementAndGet();
                    return "Success";
                },
                (resultValue, attempt, lastException) -> {
                    // Never retry
                    return false;
                },
                3, Duration.ofMillis(50), 1.5
            );
            
            // Assert
            assertEquals("Success", result);
            assertEquals(1, attempts.get());
        }
    }
    
    @Nested
    @DisplayName("Circuit Breaker Tests")
    class CircuitBreakerTests {
        
        @Test
        @DisplayName("Should execute successfully when circuit is closed")
        void shouldExecuteSuccessfullyWhenCircuitIsClosed() {
            // Arrange
            RetryUtils.CircuitBreaker circuitBreaker = new RetryUtils.CircuitBreaker(3, Duration.ofSeconds(30), Duration.ofMinutes(1));
            
            // Act
            String result = RetryUtils.executeWithCircuitBreaker(() -> "Success", circuitBreaker);
            
            // Assert
            assertEquals("Success", result);
            assertEquals(RetryUtils.CircuitBreaker.State.CLOSED, circuitBreaker.getState());
            assertEquals(0, circuitBreaker.getFailureCount());
        }
        
        @Test
        @DisplayName("Should open circuit after failure threshold")
        void shouldOpenCircuitAfterFailureThreshold() {
            // Arrange
            RetryUtils.CircuitBreaker circuitBreaker = new RetryUtils.CircuitBreaker(2, Duration.ofSeconds(30), Duration.ofMinutes(1));
            
            // Act & Assert - First failure
            RuntimeException exception1 = assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithCircuitBreaker(() -> {
                    throw new RuntimeException("First failure");
                }, circuitBreaker);
            });
            
            assertEquals("First failure", exception1.getMessage());
            assertEquals(RetryUtils.CircuitBreaker.State.CLOSED, circuitBreaker.getState());
            assertEquals(1, circuitBreaker.getFailureCount());
            
            // Second failure - should open circuit
            RuntimeException exception2 = assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithCircuitBreaker(() -> {
                    throw new RuntimeException("Second failure");
                }, circuitBreaker);
            });
            
            assertEquals("Second failure", exception2.getMessage());
            assertEquals(RetryUtils.CircuitBreaker.State.OPEN, circuitBreaker.getState());
            assertEquals(2, circuitBreaker.getFailureCount());
        }
        
        @Test
        @DisplayName("Should reject requests when circuit is open")
        void shouldRejectRequestsWhenCircuitIsOpen() {
            // Arrange
            RetryUtils.CircuitBreaker circuitBreaker = new RetryUtils.CircuitBreaker(1, Duration.ofSeconds(30), Duration.ofMinutes(1));
            
            // Fail once to open circuit
            assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithCircuitBreaker(() -> {
                    throw new RuntimeException("Failure");
                }, circuitBreaker);
            });
            
            // Act & Assert - Circuit should be open and reject requests
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithCircuitBreaker(() -> "Success", circuitBreaker);
            });
            
            assertEquals("Circuit breaker is OPEN", exception.getMessage());
            assertEquals(RetryUtils.CircuitBreaker.State.OPEN, circuitBreaker.getState());
        }
        
        @Test
        @DisplayName("Should transition to half-open after recovery time")
        void shouldTransitionToHalfOpenAfterRecoveryTime() {
            // Arrange
            RetryUtils.CircuitBreaker circuitBreaker = new RetryUtils.CircuitBreaker(1, Duration.ofSeconds(30), Duration.ofMillis(100));
            
            // Fail once to open circuit
            assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithCircuitBreaker(() -> {
                    throw new RuntimeException("Failure");
                }, circuitBreaker);
            });
            
            // Wait for recovery time
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Act - Should transition to half-open
            String result = RetryUtils.executeWithCircuitBreaker(() -> "Success", circuitBreaker);
            
            // Assert
            assertEquals("Success", result);
            assertEquals(RetryUtils.CircuitBreaker.State.CLOSED, circuitBreaker.getState());
            assertEquals(0, circuitBreaker.getFailureCount());
        }
        
        @Test
        @DisplayName("Should reset failure count on success")
        void shouldResetFailureCountOnSuccess() {
            // Arrange
            RetryUtils.CircuitBreaker circuitBreaker = new RetryUtils.CircuitBreaker(3, Duration.ofSeconds(30), Duration.ofMinutes(1));
            
            // Fail twice
            assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithCircuitBreaker(() -> {
                    throw new RuntimeException("Failure 1");
                }, circuitBreaker);
            });
            
            assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithCircuitBreaker(() -> {
                    throw new RuntimeException("Failure 2");
                }, circuitBreaker);
            });
            
            assertEquals(2, circuitBreaker.getFailureCount());
            
            // Act - Succeed
            String result = RetryUtils.executeWithCircuitBreaker(() -> "Success", circuitBreaker);
            
            // Assert
            assertEquals("Success", result);
            assertEquals(RetryUtils.CircuitBreaker.State.CLOSED, circuitBreaker.getState());
            assertEquals(0, circuitBreaker.getFailureCount());
        }
    }
    
    @Nested
    @DisplayName("Circuit Breaker with Fallback Tests")
    class CircuitBreakerWithFallbackTests {
        
        @Test
        @DisplayName("Should execute fallback when circuit breaker fails")
        void shouldExecuteFallbackWhenCircuitBreakerFails() {
            // Arrange
            RetryUtils.CircuitBreaker circuitBreaker = new RetryUtils.CircuitBreaker(1, Duration.ofSeconds(30), Duration.ofMinutes(1));
            
            // Act
            String result = RetryUtils.executeWithCircuitBreakerAndFallback(
                () -> {
                    throw new RuntimeException("Primary operation failed");
                },
                () -> "Fallback result",
                circuitBreaker
            );
            
            // Assert
            assertEquals("Fallback result", result);
        }
        
        @Test
        @DisplayName("Should execute primary operation when successful")
        void shouldExecutePrimaryOperationWhenSuccessful() {
            // Arrange
            RetryUtils.CircuitBreaker circuitBreaker = new RetryUtils.CircuitBreaker(1, Duration.ofSeconds(30), Duration.ofMinutes(1));
            
            // Act
            String result = RetryUtils.executeWithCircuitBreakerAndFallback(
                () -> "Primary result",
                () -> "Fallback result",
                circuitBreaker
            );
            
            // Assert
            assertEquals("Primary result", result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {
        
        @Test
        @DisplayName("Should handle null supplier gracefully")
        void shouldHandleNullSupplierGracefully() {
            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                RetryUtils.executeWithRetry(null);
            });
        }
        
        @Test
        @DisplayName("Should handle zero max retries")
        void shouldHandleZeroMaxRetries() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            
            // Act
            String result = RetryUtils.executeWithRetry(() -> {
                attempts.incrementAndGet();
                return "Success";
            }, 0, Duration.ofMillis(50), 1.5);
            
            // Assert
            assertEquals("Success", result);
            assertEquals(1, attempts.get());
        }
        
        @Test
        @DisplayName("Should handle very short delays")
        void shouldHandleVeryShortDelays() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            Duration veryShortDelay = Duration.ofNanos(1);
            
            // Act
            String result = RetryUtils.executeWithRetry(() -> {
                int attempt = attempts.incrementAndGet();
                if (attempt < 2) {
                    throw new RuntimeException("Attempt " + attempt + " failed");
                }
                return "Success";
            }, 2, veryShortDelay, 1.0);
            
            // Assert
            assertEquals("Success", result);
            assertEquals(2, attempts.get());
        }
        
        @Test
        @DisplayName("Should handle very long delays")
        void shouldHandleVeryLongDelays() {
            // Arrange
            AtomicInteger attempts = new AtomicInteger(0);
            Duration veryLongDelay = Duration.ofSeconds(10);
            
            // Act
            String result = RetryUtils.executeWithRetry(() -> {
                int attempt = attempts.incrementAndGet();
                if (attempt < 2) {
                    throw new RuntimeException("Attempt " + attempt + " failed");
                }
                return "Success";
            }, 2, veryLongDelay, 1.0);
            
            // Assert
            assertEquals("Success", result);
            assertEquals(2, attempts.get());
        }
    }
    
    @Nested
    @DisplayName("Shutdown Tests")
    class ShutdownTests {
        
        @Test
        @DisplayName("Should shutdown gracefully")
        void shouldShutdownGracefully() {
            // Act
            RetryUtils.shutdown();
            
            // Assert - Should not throw exception
            assertDoesNotThrow(() -> {
                // Additional shutdown calls should be safe
                RetryUtils.shutdown();
            });
        }
    }
}
