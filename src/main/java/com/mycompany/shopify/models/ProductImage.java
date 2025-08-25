package com.mycompany.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a Shopify product image.
 */
public class ProductImage {
    
    private Long id;
    private Long productId;
    private Integer position;
    private String alt;
    private Integer width;
    private Integer height;
    private String src;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    private List<String> variantIds;
    
    // Constructor
    public ProductImage() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    
    public String getAlt() { return alt; }
    public void setAlt(String alt) { this.alt = alt; }
    
    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }
    
    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }
    
    public String getSrc() { return src; }
    public void setSrc(String src) { this.src = src; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<String> getVariantIds() { return variantIds; }
    public void setVariantIds(List<String> variantIds) { this.variantIds = variantIds; }
}
