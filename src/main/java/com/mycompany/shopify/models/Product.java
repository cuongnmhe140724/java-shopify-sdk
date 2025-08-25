package com.mycompany.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Represents a Shopify product.
 * Maps to the Shopify Product API response structure.
 */
public class Product {
    
    private Long id;
    private String title;
    private String bodyHtml;
    private String vendor;
    private String productType;
    private String handle;
    private String status;
    private String publishedScope;
    private String tags;
    private String adminGraphqlApiId;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;
    
    private String templateSuffix;
    private String titleTag;
    private String metaDescription;
    
    @JsonProperty("published_scope")
    private String publishedScopeField;
    
    private List<ProductVariant> variants;
    private List<ProductOption> options;
    private List<ProductImage> images;
    
    @JsonProperty("image")
    private ProductImage mainImage;
    
    // Additional fields for product management
    private String vendorCode;
    private String sku;
    private BigDecimal weight;
    private String weightUnit;
    private String inventoryPolicy;
    private Integer inventoryQuantity;
    private String inventoryManagement;
    private String fulfillmentService;
    private Boolean requiresShipping;
    private Boolean taxable;
    private String giftCard;
    private String barcode;
    private String hsCode;
    private String countryCodeOfOrigin;
    private String provinceCodeOfOrigin;
    private String harmonizedSystemCode;
    private String countryHarmonizedSystemCodes;
    private String originCountryCode;
    private String originProvinceCode;
    private String originCountryName;
    private String originProvinceName;
    
    // SEO and marketing fields
    private String seoTitle;
    private String seoDescription;
    private String googleProductCategory;
    private String googleProductType;
    private String gender;
    private String ageGroup;
    private String brand;
    private String condition;
    private String color;
    private String material;
    
    // Constructor
    public Product() {}
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getBodyHtml() {
        return bodyHtml;
    }
    
    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }
    
    public String getVendor() {
        return vendor;
    }
    
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
    
    public String getProductType() {
        return productType;
    }
    
    public void setProductType(String productType) {
        this.productType = productType;
    }
    
    public String getHandle() {
        return handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPublishedScope() {
        return publishedScope;
    }
    
    public void setPublishedScope(String publishedScope) {
        this.publishedScope = publishedScope;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public String getAdminGraphqlApiId() {
        return adminGraphqlApiId;
    }
    
    public void setAdminGraphqlApiId(String adminGraphqlApiId) {
        this.adminGraphqlApiId = adminGraphqlApiId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
    
    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }
    
    public String getTemplateSuffix() {
        return templateSuffix;
    }
    
    public void setTemplateSuffix(String templateSuffix) {
        this.templateSuffix = templateSuffix;
    }
    
    public String getTitleTag() {
        return titleTag;
    }
    
    public void setTitleTag(String titleTag) {
        this.titleTag = titleTag;
    }
    
    public String getMetaDescription() {
        return metaDescription;
    }
    
    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }
    
    public String getPublishedScopeField() {
        return publishedScopeField;
    }
    
    public void setPublishedScopeField(String publishedScopeField) {
        this.publishedScopeField = publishedScopeField;
    }
    
    public List<ProductVariant> getVariants() {
        return variants;
    }
    
    public void setVariants(List<ProductVariant> variants) {
        this.variants = variants;
    }
    
    public List<ProductOption> getOptions() {
        return options;
    }
    
    public void setOptions(List<ProductOption> options) {
        this.options = options;
    }
    
    public List<ProductImage> getImages() {
        return images;
    }
    
    public void setImages(List<ProductImage> images) {
        this.images = images;
    }
    
    public ProductImage getMainImage() {
        return mainImage;
    }
    
    public void setMainImage(ProductImage mainImage) {
        this.mainImage = mainImage;
    }
    
    public String getVendorCode() {
        return vendorCode;
    }
    
    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }
    
    public String getSku() {
        return sku;
    }
    
    public void setSku(String sku) {
        this.sku = sku;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public String getWeightUnit() {
        return weightUnit;
    }
    
    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }
    
    public String getInventoryPolicy() {
        return inventoryPolicy;
    }
    
    public void setInventoryPolicy(String inventoryPolicy) {
        this.inventoryPolicy = inventoryPolicy;
    }
    
    public Integer getInventoryQuantity() {
        return inventoryQuantity;
    }
    
    public void setInventoryQuantity(Integer inventoryQuantity) {
        this.inventoryQuantity = inventoryQuantity;
    }
    
    public String getInventoryManagement() {
        return inventoryManagement;
    }
    
    public void setInventoryManagement(String inventoryManagement) {
        this.inventoryManagement = inventoryManagement;
    }
    
    public String getFulfillmentService() {
        return fulfillmentService;
    }
    
    public void setFulfillmentService(String fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }
    
    public Boolean getRequiresShipping() {
        return requiresShipping;
    }
    
    public void setRequiresShipping(Boolean requiresShipping) {
        this.requiresShipping = requiresShipping;
    }
    
    public Boolean getTaxable() {
        return taxable;
    }
    
    public void setTaxable(Boolean taxable) {
        this.taxable = taxable;
    }
    
    public String getGiftCard() {
        return giftCard;
    }
    
    public void setGiftCard(String giftCard) {
        this.giftCard = giftCard;
    }
    
    public String getBarcode() {
        return barcode;
    }
    
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
    
    public String getHsCode() {
        return hsCode;
    }
    
    public void setHsCode(String hsCode) {
        this.hsCode = hsCode;
    }
    
    public String getCountryCodeOfOrigin() {
        return countryCodeOfOrigin;
    }
    
    public void setCountryCodeOfOrigin(String countryCodeOfOrigin) {
        this.countryCodeOfOrigin = countryCodeOfOrigin;
    }
    
    public String getProvinceCodeOfOrigin() {
        return provinceCodeOfOrigin;
    }
    
    public void setProvinceCodeOfOrigin(String provinceCodeOfOrigin) {
        this.provinceCodeOfOrigin = provinceCodeOfOrigin;
    }
    
    public String getOriginCountryCode() {
        return originCountryCode;
    }
    
    public void setOriginCountryCode(String originCountryCode) {
        this.originCountryCode = originCountryCode;
    }
    
    public String getOriginProvinceCode() {
        return originProvinceCode;
    }
    
    public void setOriginProvinceCode(String originProvinceCode) {
        this.originProvinceCode = originProvinceCode;
    }
    
    public String getOriginCountryName() {
        return originCountryName;
    }
    
    public void setOriginCountryName(String originCountryName) {
        this.originCountryName = originCountryName;
    }
    
    public String getOriginProvinceName() {
        return originProvinceName;
    }
    
    public void setOriginProvinceName(String originProvinceName) {
        this.originProvinceName = originProvinceName;
    }
    
    public String getSeoTitle() {
        return seoTitle;
    }
    
    public void setSeoTitle(String seoTitle) {
        this.seoTitle = seoTitle;
    }
    
    public String getSeoDescription() {
        return seoDescription;
    }
    
    public void setSeoDescription(String seoDescription) {
        this.seoDescription = seoDescription;
    }
    
    public String getGoogleProductCategory() {
        return googleProductCategory;
    }
    
    public void setGoogleProductCategory(String googleProductCategory) {
        this.googleProductCategory = googleProductCategory;
    }
    
    public String getGoogleProductType() {
        return googleProductType;
    }
    
    public void setGoogleProductType(String googleProductType) {
        this.googleProductType = googleProductType;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getAgeGroup() {
        return ageGroup;
    }
    
    public void setAgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }
    
    public String getBrand() {
        return brand;
    }
    
    public void setBrand(String brand) {
        this.brand = brand;
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getMaterial() {
        return material;
    }
    
    public void setMaterial(String material) {
        this.material = material;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", handle='" + handle + '\'' +
                ", status='" + status + '\'' +
                ", vendor='" + vendor + '\'' +
                ", productType='" + productType + '\'' +
                '}';
    }
}
