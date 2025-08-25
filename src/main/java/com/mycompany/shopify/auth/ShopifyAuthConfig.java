package com.mycompany.shopify.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration class for Shopify authentication settings.
 * Supports both public app (OAuth) and custom app (permanent token) authentication methods.
 */
@Component
@ConfigurationProperties(prefix = "shopify")
public class ShopifyAuthConfig {
    
    private String shopDomain;
    private String apiVersion = "2024-01";
    private String accessToken;
    private String apiKey;
    private String apiSecret;
    private String redirectUri;
    private String scopes;
    
    // Constructor
    public ShopifyAuthConfig() {}
    
    // Getters and Setters
    public String getShopDomain() {
        return shopDomain;
    }
    
    public void setShopDomain(String shopDomain) {
        this.shopDomain = shopDomain;
    }
    
    public String getApiVersion() {
        return apiVersion;
    }
    
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getApiSecret() {
        return apiSecret;
    }
    
    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }
    
    public String getRedirectUri() {
        return redirectUri;
    }
    
    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }
    
    public String getScopes() {
        return scopes;
    }
    
    public void setScopes(String scopes) {
        this.scopes = scopes;
    }
    
    /**
     * Gets the base URL for the Shopify shop.
     * @return The formatted shop URL
     */
    public String getShopUrl() {
        if (shopDomain == null || shopDomain.trim().isEmpty()) {
            throw new IllegalStateException("Shop domain is not configured");
        }
        
        if (!shopDomain.startsWith("http")) {
            return "https://" + shopDomain;
        }
        return shopDomain;
    }
    
    /**
     * Gets the admin API URL for the shop.
     * @return The admin API URL
     */
    public String getAdminApiUrl() {
        return getShopUrl() + "/admin/api/" + apiVersion;
    }
    
    /**
     * Checks if this is a custom app configuration (has access token).
     * @return true if custom app, false if public app
     */
    public boolean isCustomApp() {
        return accessToken != null && !accessToken.trim().isEmpty();
    }
    
    /**
     * Checks if this is a public app configuration (has API key and secret).
     * @return true if public app, false if custom app
     */
    public boolean isPublicApp() {
        return apiKey != null && !apiKey.trim().isEmpty() && 
               apiSecret != null && !apiSecret.trim().isEmpty();
    }
    
    /**
     * Validates the configuration based on the authentication type.
     * @throws IllegalStateException if configuration is invalid
     */
    public void validate() {
        if (shopDomain == null || shopDomain.trim().isEmpty()) {
            throw new IllegalStateException("Shop domain is required");
        }
        
        if (isCustomApp()) {
            // Custom app validation
            if (accessToken.trim().isEmpty()) {
                throw new IllegalStateException("Access token cannot be empty for custom app");
            }
        } else if (isPublicApp()) {
            // Public app validation
            if (apiKey.trim().isEmpty() || apiSecret.trim().isEmpty()) {
                throw new IllegalStateException("API key and secret cannot be empty for public app");
            }
            if (redirectUri == null || redirectUri.trim().isEmpty()) {
                throw new IllegalStateException("Redirect URI is required for public app");
            }
        } else {
            throw new IllegalStateException("Either access token (custom app) or API key/secret (public app) must be configured");
        }
    }
}
