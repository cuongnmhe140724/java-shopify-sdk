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
 * Client for making Shopify GraphQL API calls.
 * Handles authentication, query execution, and response parsing.
 */
@Component
public class GraphQLClient {
    
    private final HttpClientWrapper httpClient;
    private final ShopifyAuthConfig authConfig;
    
    public GraphQLClient(HttpClientWrapper httpClient, ShopifyAuthConfig authConfig) {
        this.httpClient = httpClient;
        this.authConfig = authConfig;
    }
    
    /**
     * Executes a GraphQL query.
     * @param query The GraphQL query string
     * @param variables Optional variables for the query
     * @param responseType The expected response type
     * @return The GraphQL response
     */
    public <T> ResponseEntity<T> query(String query, Map<String, Object> variables, Class<T> responseType) {
        return executeGraphQLRequest(query, variables, null, responseType);
    }
    
    /**
     * Executes a GraphQL query without variables.
     * @param query The GraphQL query string
     * @param responseType The expected response type
     * @return The GraphQL response
     */
    public <T> ResponseEntity<T> query(String query, Class<T> responseType) {
        return query(query, null, responseType);
    }
    
    /**
     * Executes a GraphQL mutation.
     * @param mutation The GraphQL mutation string
     * @param variables Optional variables for the mutation
     * @param responseType The expected response type
     * @return The GraphQL response
     */
    public <T> ResponseEntity<T> mutation(String mutation, Map<String, Object> variables, Class<T> responseType) {
        return executeGraphQLRequest(mutation, variables, null, responseType);
    }
    
    /**
     * Executes a GraphQL mutation without variables.
     * @param mutation The GraphQL mutation string
     * @param responseType The expected response type
     * @return The GraphQL response
     */
    public <T> ResponseEntity<T> mutation(String mutation, Class<T> responseType) {
        return mutation(mutation, null, responseType);
    }
    
    /**
     * Executes a GraphQL subscription (placeholder for future implementation).
     * @param subscription The GraphQL subscription string
     * @param variables Optional variables for the subscription
     * @param responseType The expected response type
     * @return The GraphQL response
     */
    public <T> ResponseEntity<T> subscription(String subscription, Map<String, Object> variables, Class<T> responseType) {
        // Note: GraphQL subscriptions typically use WebSocket connections
        // This is a placeholder for future implementation
        throw new UnsupportedOperationException("GraphQL subscriptions are not yet supported");
    }
    
    /**
     * Executes a GraphQL request with the given operation type.
     * @param operation The GraphQL operation (query, mutation, subscription)
     * @param variables Optional variables
     * @param operationName Optional operation name
     * @param responseType The expected response type
     * @return The GraphQL response
     */
    private <T> ResponseEntity<T> executeGraphQLRequest(String operation, Map<String, Object> variables, 
                                                      String operationName, Class<T> responseType) {
        String url = buildGraphQLUrl();
        
        GraphQLRequest requestBody = new GraphQLRequest();
        requestBody.setQuery(operation);
        requestBody.setVariables(variables);
        requestBody.setOperationName(operationName);
        
        HttpEntity<GraphQLRequest> entity = new HttpEntity<>(requestBody);
        
        HttpRequest<T> request = HttpRequest.<T>builder()
                .url(url)
                .method(HttpMethod.POST)
                .entity(entity)
                .responseType(responseType)
                .accessToken(authConfig.getAccessToken())
                .header("Content-Type", "application/json")
                .build();
        
        return httpClient.executeWithRetry(request);
    }
    
    /**
     * Builds the GraphQL API URL.
     * @return The GraphQL API URL
     */
    private String buildGraphQLUrl() {
        return UriComponentsBuilder
                .fromHttpUrl(authConfig.getShopUrl())
                .path("/admin/api/graphql.json")
                .build()
                .toUriString();
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
    
    /**
     * Internal class representing a GraphQL request.
     */
    private static class GraphQLRequest {
        private String query;
        private Map<String, Object> variables;
        private String operationName;
        
        // Getters and Setters
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
        
        public Map<String, Object> getVariables() {
            return variables;
        }
        
        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }
        
        public String getOperationName() {
            return operationName;
        }
        
        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }
    }
}
