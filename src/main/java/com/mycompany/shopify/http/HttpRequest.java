package com.mycompany.shopify.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.core.ParameterizedTypeReference;

/**
 * Represents an HTTP request with all necessary parameters.
 * Generic type T represents the response body type.
 */
public class HttpRequest<T> {
    
    private final String url;
    private final HttpMethod method;
    private final HttpEntity<?> entity;
    private final Class<T> responseType;
    private final ParameterizedTypeReference<T> responseTypeRef;
    private final HttpHeaders headers;
    private final String accessToken;
    
    private HttpRequest(Builder<T> builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.entity = builder.entity;
        this.responseType = builder.responseType;
        this.responseTypeRef = builder.responseTypeRef;
        this.headers = builder.headers;
        this.accessToken = builder.accessToken;
    }
    
    // Getters
    public String getUrl() {
        return url;
    }
    
    public HttpMethod getMethod() {
        return method;
    }
    
    public HttpEntity<?> getEntity() {
        return entity;
    }
    
    public Class<T> getResponseType() {
        return responseType;
    }
    
    public ParameterizedTypeReference<T> getResponseTypeRef() {
        return responseTypeRef;
    }
    
    public HttpHeaders getHeaders() {
        return headers;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    /**
     * Builder class for HttpRequest.
     */
    public static class Builder<T> {
        private String url;
        private HttpMethod method = HttpMethod.GET;
        private HttpEntity<?> entity;
        private Class<T> responseType;
        private ParameterizedTypeReference<T> responseTypeRef;
        private HttpHeaders headers = new HttpHeaders();
        private String accessToken;
        
        public Builder<T> url(String url) {
            this.url = url;
            return this;
        }
        
        public Builder<T> method(HttpMethod method) {
            this.method = method;
            return this;
        }
        
        public Builder<T> entity(HttpEntity<?> entity) {
            this.entity = entity;
            return this;
        }
        
        public Builder<T> responseType(Class<T> responseType) {
            this.responseType = responseType;
            this.responseTypeRef = null;
            return this;
        }
        
        public Builder<T> responseType(ParameterizedTypeReference<T> responseTypeRef) {
            this.responseTypeRef = responseTypeRef;
            this.responseType = null;
            return this;
        }
        
        public Builder<T> headers(HttpHeaders headers) {
            this.headers = headers;
            return this;
        }
        
        public Builder<T> header(String name, String value) {
            this.headers.set(name, value);
            return this;
        }
        
        public Builder<T> accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }
        
        public HttpRequest<T> build() {
            if (url == null) {
                throw new IllegalStateException("URL is required");
            }
            if (responseType == null && responseTypeRef == null) {
                throw new IllegalStateException("Response type is required");
            }
            return new HttpRequest<>(this);
        }
    }
    
    /**
     * Creates a new builder instance.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }
}
