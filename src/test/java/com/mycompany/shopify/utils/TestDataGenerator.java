package com.mycompany.shopify.utils;

import com.mycompany.shopify.models.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for generating test data for Shopify SDK unit tests.
 * Provides realistic mock data for all model classes.
 */
public class TestDataGenerator {
    
    /**
     * Generates a sample Product with all fields populated.
     */
    public static Product createSampleProduct() {
        Product product = new Product();
        product.setId(123L);
        product.setTitle("Test Product");
        product.setBodyHtml("<p>This is a test product description</p>");
        product.setVendor("Test Vendor");
        product.setProductType("Electronics");
        product.setHandle("test-product");
        product.setStatus("active");
        product.setCreatedAt(LocalDateTime.now().minusDays(30));
        product.setVariants(createSampleProductVariants());
        product.setOptions(createSampleProductOptions());
        product.setImages(createSampleProductImages());
        return product;
    }
    
    /**
     * Generates a list of sample products.
     */
    public static List<Product> createSampleProducts(int count) {
        List<Product> products = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Product product = createSampleProduct();
            product.setId((long) i);
            product.setTitle("Test Product " + i);
            product.setHandle("test-product-" + i);
            products.add(product);
        }
        return products;
    }
    
    /**
     * Generates sample ProductVariant objects.
     */
    public static List<ProductVariant> createSampleProductVariants() {
        ProductVariant variant1 = new ProductVariant();
        variant1.setId(456L);
        variant1.setProductId(123L);
        variant1.setTitle("Default Title");
        variant1.setSku("TEST-SKU-001");
        variant1.setBarcode("1234567890123");
        variant1.setOption1("Default");
        variant1.setOption2(null);
        variant1.setOption3(null);
        variant1.setCreatedAt(LocalDateTime.now().minusDays(30));
        variant1.setUpdatedAt(LocalDateTime.now());
        variant1.setRequiresShipping(true);
        variant1.setTaxable(true);
        variant1.setPrice(new BigDecimal("29.99"));
        variant1.setCompareAtPrice(new BigDecimal("39.99"));
        variant1.setInventoryQuantity(100);
        
        ProductVariant variant2 = new ProductVariant();
        variant2.setId(457L);
        variant2.setProductId(123L);
        variant2.setTitle("Large");
        variant2.setSku("TEST-SKU-002");
        variant2.setBarcode("1234567890124");
        variant2.setOption1("Large");
        variant2.setOption2(null);
        variant2.setOption3(null);
        variant2.setCreatedAt(LocalDateTime.now().minusDays(30));
        variant2.setUpdatedAt(LocalDateTime.now());
        variant2.setRequiresShipping(true);
        variant2.setTaxable(true);
        variant2.setPrice(new BigDecimal("34.99"));
        variant2.setCompareAtPrice(new BigDecimal("44.99"));
        variant2.setInventoryQuantity(50);
        
        return Arrays.asList(variant1, variant2);
    }
    
    /**
     * Generates sample ProductOption objects.
     */
    public static List<ProductOption> createSampleProductOptions() {
        ProductOption option1 = new ProductOption();
        option1.setId(789L);
        option1.setProductId(123L);
        option1.setName("Size");
        option1.setPosition(1);
        option1.setValues(Arrays.asList("Small", "Medium", "Large"));
        
        ProductOption option2 = new ProductOption();
        option2.setId(790L);
        option2.setProductId(123L);
        option2.setName("Color");
        option2.setPosition(2);
        option2.setValues(Arrays.asList("Red", "Blue", "Green"));
        
        return Arrays.asList(option1, option2);
    }
    
    /**
     * Generates sample ProductImage objects.
     */
    public static List<ProductImage> createSampleProductImages() {
        ProductImage image1 = new ProductImage();
        image1.setId(101L);
        image1.setProductId(123L);
        image1.setPosition(1);
        image1.setAlt("Test Product Main Image");
        image1.setWidth(800);
        image1.setHeight(600);
        image1.setSrc("https://cdn.shopify.com/test-product-1.jpg");
        image1.setCreatedAt(LocalDateTime.now().minusDays(30));
        image1.setUpdatedAt(LocalDateTime.now());
        image1.setVariantIds(Arrays.asList("456", "457"));
        
        ProductImage image2 = new ProductImage();
        image2.setId(102L);
        image2.setProductId(123L);
        image2.setPosition(2);
        image2.setAlt("Test Product Secondary Image");
        image2.setWidth(800);
        image2.setHeight(600);
        image2.setSrc("https://cdn.shopify.com/test-product-2.jpg");
        image2.setCreatedAt(LocalDateTime.now().minusDays(30));
        image2.setUpdatedAt(LocalDateTime.now());
        image2.setVariantIds(Arrays.asList("456"));
        
        return Arrays.asList(image1, image2);
    }
    
    /**
     * Generates a sample Order with all fields populated.
     */
    public static Order createSampleOrder() {
        Order order = new Order();
        order.setId(1001L);
        order.setName("#1001");
        order.setEmail("customer@example.com");
        order.setPhone("+1-555-0123");
        order.setCurrency("USD");
        order.setFinancialStatus("paid");
        order.setFulfillmentStatus("fulfilled");
        order.setOrderNumber("1001");
        order.setOrderStatus("open");
        order.setCreatedAt(LocalDateTime.now().minusDays(7));
        order.setUpdatedAt(LocalDateTime.now());
        order.setProcessedAt(LocalDateTime.now().minusDays(7));
        order.setCancelledAt(null);
        order.setClosedAt(null);
        order.setCancelledReason(null);
        order.setTotalPrice("34.99");
        order.setTotalTax("2.99");
        order.setSubtotalPrice("29.99");
        order.setTotalDiscounts("0.00");
        order.setTotalWeight("2.5");
        return order;
    }
    
    /**
     * Generates a list of sample orders.
     */
    public static List<Order> createSampleOrders(int count) {
        List<Order> orders = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Order order = createSampleOrder();
            order.setId(1000L + i);
            order.setName("#" + (1000 + i));
            order.setOrderNumber(String.valueOf(1000 + i));
            orders.add(order);
        }
        return orders;
    }
    
    /**
     * Generates a sample Customer with all fields populated.
     */
    public static Customer createSampleCustomer() {
        Customer customer = new Customer();
        customer.setId(2001L);
        customer.setEmail("john.doe@example.com");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setPhone("+1-555-0124");
        customer.setTags("vip,returning");
        customer.setCreatedAt(LocalDateTime.now().minusDays(60));
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setOrdersCount("5");
        customer.setTotalSpent("149.95");
        return customer;
    }
    
    /**
     * Generates a list of sample customers.
     */
    public static List<Customer> createSampleCustomers(int count) {
        List<Customer> customers = new java.util.ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Customer customer = createSampleCustomer();
            customer.setId(2000L + i);
            customer.setEmail("customer" + i + "@example.com");
            customer.setFirstName("Customer" + i);
            customer.setLastName("Test");
            customers.add(customer);
        }
        return customers;
    }
    
    /**
     * Generates sample ProductList wrapper.
     */
    public static ProductList createSampleProductList(int count) {
        ProductList productList = new ProductList();
        productList.setProducts(createSampleProducts(count));
        return productList;
    }
    
    /**
     * Generates sample OrderList wrapper.
     */
    public static OrderList createSampleOrderList(int count) {
        OrderList orderList = new OrderList();
        orderList.setOrders(createSampleOrders(count));
        return orderList;
    }
    
    /**
     * Generates sample CustomerList wrapper.
     */
    public static CustomerList createSampleCustomerList(int count) {
        CustomerList customerList = new CustomerList();
        customerList.setCustomers(createSampleCustomers(count));
        return customerList;
    }
    
    /**
     * Generates sample GraphQL response data.
     */
    public static java.util.Map<String, Object> createSampleGraphQLResponse() {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        java.util.Map<String, Object> products = new java.util.HashMap<>();
        
        products.put("edges", Arrays.asList(
            createSampleGraphQLEdge(createSampleProduct()),
            createSampleGraphQLEdge(createSampleProduct())
        ));
        
        data.put("products", products);
        response.put("data", data);
        return response;
    }
    
    /**
     * Generates sample GraphQL edge for pagination.
     */
    private static java.util.Map<String, Object> createSampleGraphQLEdge(Product product) {
        java.util.Map<String, Object> edge = new java.util.HashMap<>();
        edge.put("node", product);
        edge.put("cursor", "eyJsYXN0X2lkIjoxMjN9");
        return edge;
    }
    
    /**
     * Generates sample error response for testing error handling.
     */
    public static java.util.Map<String, Object> createSampleErrorResponse() {
        java.util.Map<String, Object> error = new java.util.HashMap<>();
        error.put("message", "Test error message");
        error.put("code", "TEST_ERROR");
        error.put("field", "test_field");
        
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("errors", Arrays.asList(error));
        return response;
    }
    
    /**
     * Generates sample rate limit response headers.
     */
    public static java.util.Map<String, String> createSampleRateLimitHeaders() {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        headers.put("X-Shopify-Shop-Api-Call-Limit", "35/40");
        headers.put("Retry-After", "30");
        return headers;
    }
}
