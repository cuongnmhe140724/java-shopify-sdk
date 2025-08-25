package com.mycompany.shopify.models;

import java.util.List;

/**
 * Represents a Shopify product option.
 */
public class ProductOption {
    
    private Long id;
    private Long productId;
    private String name;
    private Integer position;
    private List<String> values;
    
    // Constructor
    public ProductOption() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    
    public List<String> getValues() { return values; }
    public void setValues(List<String> values) { this.values = values; }
}
