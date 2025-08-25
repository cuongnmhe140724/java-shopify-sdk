# Shopify SDK for Java

A comprehensive Java SDK for Shopify REST and GraphQL APIs, built with Spring Boot and Maven.

## Features

- **Dual API Support**: Both REST and GraphQL API clients
- **Authentication**: Support for public apps (OAuth) and custom apps (permanent tokens)
- **HTTP Client**: Robust HTTP client with retry logic and rate limit handling
- **Error Handling**: Comprehensive error handling with retry mechanisms
- **JSON Utilities**: Jackson-based JSON utilities for data manipulation
- **Spring Boot Integration**: Native Spring Boot support with auto-configuration

## Requirements

- Java 17 or higher
- Maven 3.6+
- Spring Boot 3.2.0+

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.mycompany</groupId>
    <artifactId>shopify-sdk</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

Add the dependency to your `build.gradle`:

```gradle
implementation 'com.mycompany:shopify-sdk:0.1.0'
```

## Configuration

### Custom App (Permanent Token)

For custom apps with permanent access tokens, configure in `application.properties`:

```properties
shopify.shop-domain=your-shop.myshopify.com
shopify.api-version=2024-01
shopify.access-token=your_permanent_access_token_here
```

### Public App (OAuth)

For public apps requiring OAuth flow, configure in `application.properties`:

```properties
shopify.shop-domain=your-shop.myshopify.com
shopify.api-version=2024-01
shopify.api-key=your_api_key_here
shopify.api-secret=your_api_secret_here
shopify.redirect-uri=https://your-app.com/oauth/callback
shopify.scopes=read_products,write_products,read_orders,write_orders
```

## Usage

### Basic Usage

```java
@Service
public class ShopifyService {
    
    private final ShopifyClient shopifyClient;
    
    public ShopifyService(ShopifyClient shopifyClient) {
        this.shopifyClient = shopifyClient;
    }
    
    public void example() {
        // Access REST client
        RestClient restClient = shopifyClient.rest();
        
        // Access GraphQL client
        GraphQLClient graphqlClient = shopifyClient.graphql();
        
        // Access OAuth service
        OAuthService oauthService = shopifyClient.oauth();
        
        // Access token manager
        TokenManager tokenManager = shopifyClient.tokens();
        
        // Access JSON utilities
        JsonUtils jsonUtils = shopifyClient.json();
        
        // Access service layer
        ProductService productService = shopifyClient.products();
        OrderService orderService = shopifyClient.orders();
        CustomerService customerService = shopifyClient.customers();
    }
}
```

### REST API Examples

```java
// GET request
ResponseEntity<ProductList> response = shopifyClient.rest()
    .get("/products.json", ProductList.class);

// GET request with query parameters
Map<String, String> params = new HashMap<>();
params.put("limit", "50");
params.put("status", "active");
ResponseEntity<ProductList> response = shopifyClient.rest()
    .get("/products.json", params, ProductList.class);

// POST request
Product product = new Product();
product.setTitle("New Product");
ResponseEntity<Product> response = shopifyClient.rest()
    .post("/products.json", product, Product.class);

// PUT request
product.setTitle("Updated Product");
ResponseEntity<Product> response = shopifyClient.rest()
    .put("/products/" + product.getId() + ".json", product, Product.class);

// DELETE request
ResponseEntity<Void> response = shopifyClient.rest()
    .delete("/products/" + productId + ".json");

// Service Layer Examples
// Products
Product product = shopifyClient.products().getProductById(123L).getBody();
ResponseEntity<ProductList> products = shopifyClient.products().getProducts(50, null);
ResponseEntity<Product> newProduct = shopifyClient.products().createProduct(product);

// Orders
Order order = shopifyClient.orders().getOrderById(456L).getBody();
ResponseEntity<OrderList> orders = shopifyClient.orders().getOrdersByStatus("open");
ResponseEntity<Order> cancelledOrder = shopifyClient.orders().cancelOrder(456L, "Customer request");

// Customers
Customer customer = shopifyClient.customers().getCustomerById(789L).getBody();
ResponseEntity<CustomerList> customers = shopifyClient.customers().searchCustomers("john@example.com");
ResponseEntity<Customer> newCustomer = shopifyClient.customers().createCustomer(customer);
```

### GraphQL API Examples

```java
// GraphQL query
String query = """
    query {
        products(first: 10) {
            edges {
                node {
                    id
                    title
                    handle
                }
            }
        }
    }
    """;

ResponseEntity<GraphQLResponse> response = shopifyClient.graphql()
    .query(query, GraphQLResponse.class);

// GraphQL mutation
String mutation = """
    mutation productCreate($input: ProductInput!) {
        productCreate(input: $input) {
            product {
                id
                title
            }
            userErrors {
                field
                message
            }
        }
    }
    """;

Map<String, Object> variables = new HashMap<>();
variables.put("input", productInput);

ResponseEntity<GraphQLResponse> response = shopifyClient.graphql()
    .mutation(mutation, variables, GraphQLResponse.class);
```

### OAuth Flow (Public Apps)

```java
// Generate authorization URL
String authUrl = shopifyClient.oauth()
    .generateAuthorizationUrl("state123");

// Exchange authorization code for access token
OAuthService.AccessTokenResponse tokenResponse = shopifyClient.oauth()
    .exchangeCodeForToken("auth_code", "shop_domain");

// Store the token
shopifyClient.tokens()
    .storeToken("shop_domain", tokenResponse.getAccessToken(), tokenResponse.getExpiresIn());
```

### JSON Utilities

```java
// Serialize object to JSON
String json = shopifyClient.json().toJson(product);

// Deserialize JSON to object
Product product = shopifyClient.json().fromJson(json, Product.class);

// Convert object to Map
Map<String, Object> map = shopifyClient.json().toMap(product);

// Pretty print JSON
String formatted = shopifyClient.json().prettyPrint(json);
```

### Retry Utilities

```java
// Execute with retry
Product result = RetryUtils.executeWithRetry(() -> {
    return shopifyClient.rest().get("/products/123.json", Product.class).getBody();
});

// Execute with custom retry configuration
Product result = RetryUtils.executeWithRetry(
    () -> shopifyClient.rest().get("/products/123.json", Product.class).getBody(),
    5,                           // max retries
    Duration.ofSeconds(2),       // base delay
    2.0                          // backoff multiplier
);

// Execute with circuit breaker
CircuitBreaker circuitBreaker = new CircuitBreaker(5, Duration.ofSeconds(30), Duration.ofMinutes(1));
Product result = RetryUtils.executeWithCircuitBreaker(
    () -> shopifyClient.rest().get("/products/123.json", Product.class).getBody(),
    circuitBreaker
);

// Execute with circuit breaker and fallback
Product result = RetryUtils.executeWithCircuitBreakerAndFallback(
    () -> shopifyClient.rest().get("/products/123.json", Product.class).getBody(),
    () -> getCachedProduct(123L),
    circuitBreaker
);
```

## Error Handling

The SDK includes comprehensive error handling:

- **Rate Limiting**: Automatic handling of 429 responses with Retry-After header parsing
- **Network Errors**: Retry logic for network failures
- **Server Errors**: Configurable retry for 5xx errors
- **Client Errors**: Immediate failure for 4xx errors (except rate limiting)

### Enhanced Error Handling (v0.3)

```java
try {
    Product product = shopifyClient.products().getProductById(123L).getBody();
} catch (ShopifyException e) {
    if (e.isRateLimitError()) {
        // Handle rate limiting
        Duration waitTime = e.getRetryAfter();
        System.out.println("Rate limited. Wait for: " + waitTime);
    } else if (e.isAuthenticationError()) {
        // Handle authentication issues
        System.out.println("Auth failed: " + e.getUserMessage());
    } else if (e.isRetryable()) {
        // Retry the operation
        // The SDK will handle this automatically
    }
}
```

### Performance Monitoring

```java
// Get overall performance statistics
PerformanceMonitor.OverallStats stats = shopifyClient.getPerformanceMonitor().getOverallStats();
System.out.println("Total requests: " + stats.getTotalRequests());
System.out.println("Success rate: " + (stats.getSuccessRate() * 100) + "%");
System.out.println("Average response time: " + stats.getAverageResponseTime());

// Get endpoint-specific statistics
PerformanceMonitor.EndpointStats endpointStats = shopifyClient.getPerformanceMonitor()
    .getEndpointStats("/products.json", "GET");
System.out.println("Endpoint success rate: " + (endpointStats.getSuccessRate() * 100) + "%");

// Get top performing endpoints
Map<String, Duration> topEndpoints = shopifyClient.getPerformanceMonitor()
    .getTopPerformingEndpoints(5);
topEndpoints.forEach((endpoint, avgTime) -> 
    System.out.println(endpoint + ": " + avgTime.toMillis() + "ms"));
```

## Configuration Options

### Retry Configuration

```java
HttpClientWrapper.RetryConfig retryConfig = new HttpClientWrapper.RetryConfig();
retryConfig.setMaxRetries(5);
retryConfig.setBaseDelayMs(2000);
retryConfig.setBackoffMultiplier(1.5);
retryConfig.setMaxDelayMs(60000);

// Enhanced v0.3 features
retryConfig.setJitterEnabled(true);           // Add randomness to delays
retryConfig.setJitterFactor(0.1);             // 10% jitter
retryConfig.setAdaptiveRetryEnabled(true);    // Adjust delays based on response time
retryConfig.setAdaptiveRetryThreshold(2);     // Start adaptive retry after 2 attempts
```

### Circuit Breaker Configuration

```java
// Create circuit breaker with failure threshold, timeout, and recovery time
CircuitBreaker circuitBreaker = new CircuitBreaker(
    5,                              // failure threshold
    Duration.ofSeconds(30),         // timeout
    Duration.ofMinutes(1)           // recovery time
);

// Check circuit breaker state
CircuitBreaker.State state = circuitBreaker.getState();
if (state == CircuitBreaker.State.OPEN) {
    System.out.println("Circuit breaker is open - service is failing");
}
```

### HTTP Timeouts

Configure in `ShopifyConfig.java`:

```java
@Bean
public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
        .setConnectTimeout(Duration.ofSeconds(30))
        .setReadTimeout(Duration.ofSeconds(60))
        .build();
}
```

## Testing

The SDK includes comprehensive unit tests using JUnit 5 and Mockito:

```bash
mvn test
```

### Test Coverage

The test suite covers:

- **Core Components**: HttpClientWrapper, RateLimitHandler, RestClient, GraphQLClient
- **Services**: ProductService, OrderService, CustomerService
- **Models**: All Shopify model classes with validation
- **Error Handling**: ShopifyException hierarchy and error scenarios
- **Utilities**: RetryUtils, JsonUtils, PerformanceMonitor
- **Edge Cases**: Rate limiting, network failures, validation errors

### Test Data Generation

```java
// Generate sample test data
Product sampleProduct = TestDataGenerator.createSampleProduct();
List<Product> products = TestDataGenerator.createSampleProducts(5);
Order sampleOrder = TestDataGenerator.createSampleOrder();
Customer sampleCustomer = TestDataGenerator.createSampleCustomer();

// Generate GraphQL responses
Map<String, Object> graphqlResponse = TestDataGenerator.createSampleGraphQLResponse();
Map<String, Object> errorResponse = TestDataGenerator.createSampleErrorResponse();
```

### Running Specific Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Run tests with specific pattern
mvn test -Dtest=*ServiceTest

# Run tests with coverage report
mvn test jacoco:report
```

## Building

```bash
mvn clean compile
mvn clean package
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue on GitHub or contact the development team.

## Roadmap

- **v0.1**: Core functionality (REST/GraphQL clients, authentication, HTTP wrapper) ✅
- **v0.2**: Service layer (ProductService, OrderService, CustomerService) ✅
- **v0.3**: Enhanced retry and rate limit handling ✅
- **v0.4**: Comprehensive unit test coverage ✅
- **v0.5**: Maven Central publishing preparation

## Changelog

### v0.4.0
- Comprehensive unit test coverage with JUnit 5 and Mockito
- Test data generators for all Shopify models
- Mock API responses and error scenarios
- Test utilities and configuration classes
- Coverage for all core components and services
- Integration test examples and edge case testing

### v0.3.0
- Enhanced retry logic with jitter and adaptive retry strategies
- Sophisticated rate limit handling with leaky bucket algorithm
- Comprehensive error handling with ShopifyException hierarchy
- Performance monitoring with detailed metrics and analytics
- Circuit breaker pattern for fault tolerance
- Advanced retry configuration with configurable backoff strategies

### v0.2.0
- Added ProductService with comprehensive product management
- Added OrderService with order lifecycle management
- Added CustomerService with customer relationship management
- Support for both REST and GraphQL APIs in all services
- Enhanced model classes (Product, Order, Customer, ProductVariant, ProductOption, ProductImage)

### v0.1.0
- Initial release with core functionality
- REST and GraphQL API clients
- OAuth authentication support
- HTTP client wrapper with retry logic
- JSON utilities and retry utilities
- Spring Boot integration
