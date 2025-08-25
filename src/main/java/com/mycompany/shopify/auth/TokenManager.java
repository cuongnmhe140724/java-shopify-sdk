package com.mycompany.shopify.auth;

import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Manages Shopify access tokens and their lifecycle.
 * Handles token storage, validation, and refresh logic.
 */
@Component
public class TokenManager {
    
    private final Map<String, TokenInfo> tokenStore;
    
    public TokenManager() {
        this.tokenStore = new ConcurrentHashMap<>();
    }
    
    /**
     * Stores an access token for a specific shop.
     * @param shopDomain The shop domain
     * @param accessToken The access token
     * @param expiresInSeconds Token expiration time in seconds
     */
    public void storeToken(String shopDomain, String accessToken, int expiresInSeconds) {
        TokenInfo tokenInfo = new TokenInfo();
        tokenInfo.setAccessToken(accessToken);
        tokenInfo.setExpiresAt(Instant.now().plusSeconds(expiresInSeconds));
        tokenInfo.setShopDomain(shopDomain);
        
        tokenStore.put(shopDomain, tokenInfo);
    }
    
    /**
     * Retrieves the access token for a specific shop.
     * @param shopDomain The shop domain
     * @return The access token, or null if not found or expired
     */
    public String getToken(String shopDomain) {
        TokenInfo tokenInfo = tokenStore.get(shopDomain);
        if (tokenInfo == null) {
            return null;
        }
        
        if (isTokenExpired(tokenInfo)) {
            tokenStore.remove(shopDomain);
            return null;
        }
        
        return tokenInfo.getAccessToken();
    }
    
    /**
     * Checks if a token exists and is valid for a shop.
     * @param shopDomain The shop domain
     * @return true if token exists and is valid, false otherwise
     */
    public boolean hasValidToken(String shopDomain) {
        return getToken(shopDomain) != null;
    }
    
    /**
     * Removes a token for a specific shop.
     * @param shopDomain The shop domain
     */
    public void removeToken(String shopDomain) {
        tokenStore.remove(shopDomain);
    }
    
    /**
     * Checks if a token is expired.
     * @param tokenInfo The token information
     * @return true if expired, false otherwise
     */
    private boolean isTokenExpired(TokenInfo tokenInfo) {
        return Instant.now().isAfter(tokenInfo.getExpiresAt());
    }
    
    /**
     * Gets the time until token expiration.
     * @param shopDomain The shop domain
     * @return Seconds until expiration, or -1 if token not found
     */
    public long getTimeUntilExpiration(String shopDomain) {
        TokenInfo tokenInfo = tokenStore.get(shopDomain);
        if (tokenInfo == null) {
            return -1;
        }
        
        long secondsRemaining = tokenInfo.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, secondsRemaining);
    }
    
    /**
     * Clears all stored tokens.
     */
    public void clearAllTokens() {
        tokenStore.clear();
    }
    
    /**
     * Gets the number of stored tokens.
     * @return The number of tokens
     */
    public int getTokenCount() {
        return tokenStore.size();
    }
    
    /**
     * Internal class for storing token information.
     */
    private static class TokenInfo {
        private String accessToken;
        private Instant expiresAt;
        private String shopDomain;
        
        // Getters and Setters
        public String getAccessToken() {
            return accessToken;
        }
        
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
        
        public Instant getExpiresAt() {
            return expiresAt;
        }
        
        public void setExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
        }
        
        public String getShopDomain() {
            return shopDomain;
        }
        
        public void setShopDomain(String shopDomain) {
            this.shopDomain = shopDomain;
        }
    }
}
