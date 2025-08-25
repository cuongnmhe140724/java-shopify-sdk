package com.mycompany.shopify.http;

import com.mycompany.shopify.auth.ShopifyAuthConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

/**
 * Client for making Shopify REST API calls.
 * Handles authentication, URL construction, and request execution.
 */
@Component
public class RestClient {
    
    private final HttpClientWrapper httpClient;
    private final ShopifyAuthConfig authConfig;
    
    public RestClient(HttpClientWrapper httpClient, ShopifyAuthConfig authConfig) {
        this.httpClient = httpClient;
        this.authConfig = authConfig;
    }
    
    /**
     * Makes a GET request to the Shopify REST API.
     * @param endpoint The API endpoint (e.g., "/products", "/orders")
     * @param queryParams Optional query parameters
     * @param responseType The expected response type
     * @return The API response
     */
    public <T> ResponseEntity<T> get(String endpoint, Map<String, String> queryParams, Class<T> responseType) {
        String url = buildApiUrl(endpoint, queryParams);
        
        HttpRequest<T> request = HttpRequest.<T>builder()
                .url(url)
                .method(HttpMethod.GET)
                .responseType(responseType)
                .accessToken(authConfig.getAccessToken())
                .build();
        
        return httpClient.executeWithRetry(request);
    }
    
    /**
     * Makes a GET request to the Shopify REST API without query parameters.
     * @param endpoint The API endpoint
     * @param responseType The expected response type
     * @return The API response
     */
    public <T> ResponseEntity<T> get(String endpoint, Class<T> responseType) {
        return get(endpoint, null, responseType);
    }
    
    /**
     * Makes a POST request to the Shopify REST API.
     * @param endpoint The API endpoint
     * @param body The request body
     * @param responseType The expected response type
     * @return The API response
     */
    public <T> ResponseEntity<T> post(String endpoint, Object body, Class<T> responseType) {
        String url = buildApiUrl(endpoint, null);
        
        HttpEntity<Object> entity = new HttpEntity<>(body);
        
        HttpRequest<T> request = HttpRequest.<T>builder()
                .url(url)
                .method(HttpMethod.POST)
                .entity(entity)
                .responseType(responseType)
                .accessToken(authConfig.getAccessToken())
                .build();
        
        return httpClient.executeWithRetry(request);
    }
    
    /**
     * Makes a PUT request to the Shopify REST API.
     * @param endpoint The API endpoint
     * @param body The request body
     * @param responseType The expected response type
     * @return The API response
     */
    public <T> ResponseEntity<T> put(String endpoint, Object body, Class<T> responseType) {
        String url = buildApiUrl(endpoint, null);
        
        HttpEntity<Object> entity = new HttpEntity<>(body);
        
        HttpRequest<T> request = HttpRequest.<T>builder()
                .url(url)
                .method(HttpMethod.PUT)
                .entity(entity)
                .responseType(responseType)
                .accessToken(authConfig.getAccessToken())
                .build();
        
        return httpClient.executeWithRetry(request);
    }
    
    /**
     * Makes a DELETE request to the Shopify REST API.
     * @param endpoint The API endpoint
     * @return The API response
     */
    public ResponseEntity<Void> delete(String endpoint) {
        String url = buildApiUrl(endpoint, null);
        
        HttpRequest<Void> request = HttpRequest.<Void>builder()
                .url(url)
                .method(HttpMethod.DELETE)
                .responseType(Void.class)
                .accessToken(authConfig.getAccessToken())
                .build();
        
        return httpClient.executeWithRetry(request);
    }
    
    /**
     * Builds the full API URL for the given endpoint and query parameters.
     * @param endpoint The API endpoint
     * @param queryParams Optional query parameters
     * @return The complete API URL
     */
    private String buildApiUrl(String endpoint, Map<String, String> queryParams) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(authConfig.getAdminApiUrl())
                .path(endpoint);
        
        if (queryParams != null) {
            queryParams.forEach(builder::queryParam);
        }
        
        return builder.build().toUriString();
    }
    
    /**
     * Gets the current API version being used.
     * @return The API version
     */
    public String getApiVersion() {
        return authConfig.getApiVersion();
    }
    
    /**
     * Gets the shop domain being used.
     * @return The shop domain
     */
    public String getShopDomain() {
        return authConfig.getShopDomain();
    }
}
