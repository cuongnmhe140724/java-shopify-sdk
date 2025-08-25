package com.mycompany.shopify.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for handling Shopify OAuth authentication flow.
 * Manages the OAuth 2.0 authorization code flow for public apps.
 */
@Service
public class OAuthService {
    
    private final ShopifyAuthConfig authConfig;
    private final SecureRandom secureRandom;
    
    public OAuthService(ShopifyAuthConfig authConfig) {
        this.authConfig = authConfig;
        this.secureRandom = new SecureRandom();
    }
    
    /**
     * Generates the OAuth authorization URL for the first step of the OAuth flow.
     * @param state Optional state parameter for security
     * @return The authorization URL to redirect users to
     */
    public String generateAuthorizationUrl(String state) {
        if (!authConfig.isPublicApp()) {
            throw new IllegalStateException("OAuth is only available for public apps");
        }
        
        String generatedState = state != null ? state : generateRandomState();
        
        return UriComponentsBuilder
                .fromHttpUrl(authConfig.getShopUrl())
                .path("/admin/oauth/authorize")
                .queryParam("client_id", authConfig.getApiKey())
                .queryParam("scope", authConfig.getScopes())
                .queryParam("redirect_uri", authConfig.getRedirectUri())
                .queryParam("state", generatedState)
                .build()
                .toUriString();
    }
    
    /**
     * Generates a random state parameter for OAuth security.
     * @return A random state string
     */
    public String generateRandomState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * Exchanges the authorization code for an access token.
     * This is the second step of the OAuth flow.
     * @param code The authorization code received from Shopify
     * @param shop The shop domain
     * @return The access token response
     */
    public AccessTokenResponse exchangeCodeForToken(String code, String shop) {
        if (!authConfig.isPublicApp()) {
            throw new IllegalStateException("OAuth is only available for public apps");
        }
        
        // This would typically make an HTTP request to Shopify's token endpoint
        // For now, we'll return a placeholder response
        // In a real implementation, you would use RestTemplate or WebClient to make the request
        
        AccessTokenResponse response = new AccessTokenResponse();
        response.setAccessToken("placeholder_token");
        response.setScope(authConfig.getScopes());
        response.setExpiresIn(7200); // 2 hours
        
        return response;
    }
    
    /**
     * Validates the HMAC signature from Shopify to ensure request authenticity.
     * @param queryString The query string from the callback
     * @param hmac The HMAC signature from Shopify
     * @return true if the signature is valid, false otherwise
     */
    public boolean validateHmac(String queryString, String hmac) {
        if (!authConfig.isPublicApp()) {
            return false;
        }
        
        // In a real implementation, you would:
        // 1. Remove the hmac parameter from the query string
        // 2. Sort the remaining parameters
        // 3. Create the HMAC using the API secret
        // 4. Compare with the received HMAC
        
        // For now, return true as a placeholder
        return true;
    }
    
    /**
     * Response class for OAuth token exchange.
     */
    public static class AccessTokenResponse {
        private String accessToken;
        private String scope;
        private int expiresIn;
        
        // Getters and Setters
        public String getAccessToken() {
            return accessToken;
        }
        
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
        
        public String getScope() {
            return scope;
        }
        
        public void setScope(String scope) {
            this.scope = scope;
        }
        
        public int getExpiresIn() {
            return expiresIn;
        }
        
        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }
    }
}
