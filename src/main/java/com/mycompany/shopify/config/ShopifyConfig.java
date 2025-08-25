package com.mycompany.shopify.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Spring configuration class for the Shopify SDK.
 * Provides necessary beans and configuration.
 */
@Configuration
public class ShopifyConfig {
    
    /**
     * Creates and configures a RestTemplate bean for HTTP operations.
     * @param builder The RestTemplateBuilder
     * @return Configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }
}
