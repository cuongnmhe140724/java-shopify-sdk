package com.mycompany.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a Shopify order.
 */
public class Order {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String currency;
    private String financialStatus;
    private String fulfillmentStatus;
    private String orderNumber;
    private String orderStatus;
    private String processingMethod;
    private String sourceName;
    private String tags;
    private String note;
    private String referringSite;
    private String source;
    private String locationId;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("processed_at")
    private LocalDateTime processedAt;
    
    @JsonProperty("cancelled_at")
    private LocalDateTime cancelledAt;
    
    @JsonProperty("closed_at")
    private LocalDateTime closedAt;
    
    @JsonProperty("cancelled_reason")
    private String cancelledReason;
    
    private String totalPrice;
    private String totalTax;
    private String subtotalPrice;
    private String totalDiscounts;
    private String totalWeight;
    
    // Constructor
    public Order() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getFinancialStatus() { return financialStatus; }
    public void setFinancialStatus(String financialStatus) { this.financialStatus = financialStatus; }
    
    public String getFulfillmentStatus() { return fulfillmentStatus; }
    public void setFulfillmentStatus(String fulfillmentStatus) { this.fulfillmentStatus = fulfillmentStatus; }
    
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    
    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
    
    public String getProcessingMethod() { return processingMethod; }
    public void setProcessingMethod(String processingMethod) { this.processingMethod = processingMethod; }
    
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public String getReferringSite() { return referringSite; }
    public void setReferringSite(String referringSite) { this.referringSite = referringSite; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    
    public String getCancelledReason() { return cancelledReason; }
    public void setCancelledReason(String cancelledReason) { this.cancelledReason = cancelledReason; }
    
    public BigDecimal getTotalPrice() { 
        try { 
            return new BigDecimal(totalPrice); 
        } catch (Exception e) { 
            return BigDecimal.ZERO; 
        } 
    }
    
    public void setTotalPrice(String totalPrice) { this.totalPrice = totalPrice; }
    
    public String getTotalTax() { return totalTax; }
    public void setTotalTax(String totalTax) { this.totalTax = totalTax; }
    
    public String getSubtotalPrice() { return subtotalPrice; }
    public void setSubtotalPrice(String subtotalPrice) { this.subtotalPrice = subtotalPrice; }
    
    public String getTotalDiscounts() { return totalDiscounts; }
    public void setTotalDiscounts(String totalDiscounts) { this.totalDiscounts = totalDiscounts; }
    
    public String getTotalWeight() { return totalWeight; }
    public void setTotalWeight(String totalWeight) { this.totalWeight = totalWeight; }
    
    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", status='" + orderStatus + '\'' +
                ", financialStatus='" + financialStatus + '\'' +
                ", fulfillmentStatus='" + fulfillmentStatus + '\'' +
                '}';
    }
}
