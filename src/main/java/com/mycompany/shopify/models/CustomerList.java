package com.mycompany.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Wrapper class for Shopify customer list API responses.
 */
public class CustomerList {
    
    private List<Customer> customers;
    
    @JsonProperty("customers")
    public List<Customer> getCustomers() {
        return customers;
    }
    
    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
