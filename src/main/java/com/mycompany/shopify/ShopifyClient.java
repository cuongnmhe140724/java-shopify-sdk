package com.mycompany.shopify;

import com.mycompany.shopify.auth.OAuthService;
import com.mycompany.shopify.auth.ShopifyAuthConfig;
import com.mycompany.shopify.auth.TokenManager;
import com.mycompany.shopify.http.GraphQLClient;
import com.mycompany.shopify.http.RestClient;
import com.mycompany.shopify.services.CustomerService;
import com.mycompany.shopify.services.OrderService;
import com.mycompany.shopify.services.ProductService;
import com.mycompany.shopify.utils.JsonUtils;
import com.mycompany.shopify.utils.RetryUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * Main entry point for the Shopify SDK.
 * Provides access to all Shopify API functionality including REST, GraphQL, and authentication.
 */
@Component
public class ShopifyClient {
    
    private final ShopifyAuthConfig authConfig;
    private final OAuthService oauthService;
    private final TokenManager tokenManager;
    private final RestClient restClient;
    private final GraphQLClient graphqlClient;
    private final ProductService productService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final JsonUtils jsonUtils;
    
    public ShopifyClient(ShopifyAuthConfig authConfig, OAuthService oauthService, TokenManager tokenManager,
                        RestClient restClient, GraphQLClient graphqlClient, ProductService productService,
                        OrderService orderService, CustomerService customerService, JsonUtils jsonUtils) {
        this.authConfig = authConfig;
        this.oauthService = oauthService;
        this.tokenManager = tokenManager;
        this.restClient = restClient;
        this.graphqlClient = graphqlClient;
        this.productService = productService;
        this.orderService = orderService;
        this.customerService = customerService;
        this.jsonUtils = jsonUtils;
    }
    
    /**
     * Initializes the Shopify client and validates configuration.
     */
    @PostConstruct
    public void initialize() {
        try {
            authConfig.validate();
            System.out.println("Shopify client initialized successfully for shop: " + authConfig.getShopDomain());
        } catch (Exception e) {
            System.err.println("Failed to initialize Shopify client: " + e.getMessage());
            throw new RuntimeException("Shopify client initialization failed", e);
        }
    }
    
    /**
     * Gets the REST client for making REST API calls.
     * @return The REST client instance
     */
    public RestClient rest() {
        return restClient;
    }
    
    /**
     * Gets the GraphQL client for making GraphQL API calls.
     * @return The GraphQL client instance
     */
    public GraphQLClient graphql() {
        return graphqlClient;
    }
    
    /**
     * Gets the product service for managing products.
     * @return The product service instance
     */
    public ProductService products() {
        return productService;
    }
    
    /**
     * Gets the order service for managing orders.
     * @return The order service instance
     */
    public OrderService orders() {
        return orderService;
    }
    
    /**
     * Gets the customer service for managing customers.
     * @return The customer service instance
     */
    public CustomerService customers() {
        return customerService;
    }
    
    /**
     * Gets the OAuth service for handling authentication.
     * @return The OAuth service instance
     */
    public OAuthService oauth() {
        return oauthService;
    }
    
    /**
     * Gets the token manager for handling access tokens.
     * @return The token manager instance
     */
    public TokenManager tokens() {
        return tokenManager;
    }
    
    /**
     * Gets the JSON utilities for JSON operations.
     * @return The JSON utilities instance
     */
    public JsonUtils json() {
        return jsonUtils;
    }
    
    /**
     * Gets the authentication configuration.
     * @return The authentication configuration
     */
    public ShopifyAuthConfig getAuthConfig() {
        return authConfig;
    }
    
    /**
     * Gets the shop domain.
     * @return The shop domain
     */
    public String getShopDomain() {
        return authConfig.getShopDomain();
    }
    
    /**
     * Gets the API version being used.
     * @return The API version
     */
    public String getApiVersion() {
        return authConfig.getApiVersion();
    }
    
    /**
     * Checks if this is a custom app configuration.
     * @return true if custom app, false if public app
     */
    public boolean isCustomApp() {
        return authConfig.isCustomApp();
    }
    
    /**
     * Checks if this is a public app configuration.
     * @return true if public app, false if custom app
     */
    public boolean isPublicApp() {
        return authConfig.isPublicApp();
    }
    
    /**
     * Gets the current access token.
     * @return The access token, or null if not available
     */
    public String getAccessToken() {
        return authConfig.getAccessToken();
    }
    
    /**
     * Validates the current configuration.
     * @throws IllegalStateException if configuration is invalid
     */
    public void validateConfiguration() {
        authConfig.validate();
    }
    
    /**
     * Gets the admin API base URL.
     * @return The admin API base URL
     */
    public String getAdminApiUrl() {
        return authConfig.getAdminApiUrl();
    }
    
    /**
     * Gets the shop URL.
     * @return The shop URL
     */
    public String getShopUrl() {
        return authConfig.getShopUrl();
    }
    
    /**
     * Shuts down the client and cleans up resources.
     */
    @PreDestroy
    public void shutdown() {
        try {
            RetryUtils.shutdown();
            System.out.println("Shopify client shutdown completed");
        } catch (Exception e) {
            System.err.println("Error during Shopify client shutdown: " + e.getMessage());
        }
    }
}
