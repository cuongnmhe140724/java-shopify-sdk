package com.mycompany.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * Represents a Shopify customer.
 */
public class Customer {
    
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String tags;
    private String note;
    private String state;
    private String currency;
    private String acceptsMarketing;
    private String acceptsMarketingUpdatedAt;
    private String marketingOptInLevel;
    private String taxExempt;
    private String taxExemptions;
    private String adminGraphqlApiId;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("email_marketing_consent")
    private String emailMarketingConsent;
    
    @JsonProperty("sms_marketing_consent")
    private String smsMarketingConsent;
    
    @JsonProperty("verified_email")
    private String verifiedEmail;
    
    @JsonProperty("multipass_identifier")
    private String multipassIdentifier;
    
    @JsonProperty("last_order_id")
    private String lastOrderId;
    
    @JsonProperty("last_order_name")
    private String lastOrderName;
    
    @JsonProperty("orders_count")
    private String ordersCount;
    
    @JsonProperty("total_spent")
    private String totalSpent;
    
    @JsonProperty("default_address")
    private String defaultAddress;
    
    // Constructor
    public Customer() {}
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getAcceptsMarketing() { return acceptsMarketing; }
    public void setAcceptsMarketing(String acceptsMarketing) { this.acceptsMarketing = acceptsMarketing; }
    
    public String getAcceptsMarketingUpdatedAt() { return acceptsMarketingUpdatedAt; }
    public void setAcceptsMarketingUpdatedAt(String acceptsMarketingUpdatedAt) { this.acceptsMarketingUpdatedAt = acceptsMarketingUpdatedAt; }
    
    public String getMarketingOptInLevel() { return marketingOptInLevel; }
    public void setMarketingOptInLevel(String marketingOptInLevel) { this.marketingOptInLevel = marketingOptInLevel; }
    
    public String getTaxExempt() { return taxExempt; }
    public void setTaxExempt(String taxExempt) { this.taxExempt = taxExempt; }
    
    public String getTaxExemptions() { return taxExemptions; }
    public void setTaxExemptions(String taxExemptions) { this.taxExemptions = taxExemptions; }
    
    public String getAdminGraphqlApiId() { return adminGraphqlApiId; }
    public void setAdminGraphqlApiId(String adminGraphqlApiId) { this.adminGraphqlApiId = adminGraphqlApiId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getEmailMarketingConsent() { return emailMarketingConsent; }
    public void setEmailMarketingConsent(String emailMarketingConsent) { this.emailMarketingConsent = emailMarketingConsent; }
    
    public String getSmsMarketingConsent() { return smsMarketingConsent; }
    public void setSmsMarketingConsent(String smsMarketingConsent) { this.smsMarketingConsent = smsMarketingConsent; }
    
    public String getVerifiedEmail() { return verifiedEmail; }
    public void setVerifiedEmail(String verifiedEmail) { this.verifiedEmail = verifiedEmail; }
    
    public String getMultipassIdentifier() { return multipassIdentifier; }
    public void setMultipassIdentifier(String multipassIdentifier) { this.multipassIdentifier = multipassIdentifier; }
    
    public String getLastOrderId() { return lastOrderId; }
    public void setLastOrderId(String lastOrderId) { this.lastOrderId = lastOrderId; }
    
    public String getLastOrderName() { return lastOrderName; }
    public void setLastOrderName(String lastOrderName) { this.lastOrderName = lastOrderName; }
    
    public String getOrdersCount() { return ordersCount; }
    public void setOrdersCount(String ordersCount) { this.ordersCount = ordersCount; }
    
    public String getTotalSpent() { return totalSpent; }
    public void setTotalSpent(String totalSpent) { this.totalSpent = totalSpent; }
    
    public String getDefaultAddress() { return defaultAddress; }
    public void setDefaultAddress(String defaultAddress) { this.defaultAddress = defaultAddress; }
    
    /**
     * Gets the full name of the customer.
     * @return The full name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }
    
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", fullName='" + getFullName() + '\'' +
                ", phone='" + phone + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
