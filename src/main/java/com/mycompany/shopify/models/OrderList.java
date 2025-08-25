package com.mycompany.shopify.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Wrapper class for Shopify order list API responses.
 */
public class OrderList {
    
    private List<Order> orders;
    
    @JsonProperty("orders")
    public List<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
