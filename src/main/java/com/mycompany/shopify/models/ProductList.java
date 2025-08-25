package com.mycompany.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Wrapper class for Shopify product list API responses.
 */
public class ProductList {
    
    private List<Product> products;
    
    @JsonProperty("products")
    public List<Product> getProducts() {
        return products;
    }
    
    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
