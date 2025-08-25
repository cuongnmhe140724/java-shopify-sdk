package com.mycompany.shopify.services;

import com.mycompany.shopify.http.GraphQLClient;
import com.mycompany.shopify.http.RestClient;
import com.mycompany.shopify.models.Customer;
import com.mycompany.shopify.models.CustomerList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Shopify customers.
 * Provides methods for CRUD operations using both REST and GraphQL APIs.
 */
@Service
public class CustomerService {
    
    private final RestClient restClient;
    private final GraphQLClient graphqlClient;
    
    public CustomerService(RestClient restClient, GraphQLClient graphqlClient) {
        this.restClient = restClient;
        this.graphqlClient = graphqlClient;
    }
    
    // REST API Methods
    
    /**
     * Gets all customers using REST API.
     * @return List of customers
     */
    public ResponseEntity<CustomerList> getAllCustomers() {
        return restClient.get("/customers.json", CustomerList.class);
    }
    
    /**
     * Gets customers with pagination using REST API.
     * @param limit Maximum number of customers to return
     * @param pageInfo Pagination cursor
     * @return List of customers
     */
    public ResponseEntity<CustomerList> getCustomers(int limit, String pageInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        if (pageInfo != null) {
            params.put("page_info", pageInfo);
        }
        return restClient.get("/customers.json", params, CustomerList.class);
    }
    
    /**
     * Gets customers by query using REST API.
     * @param query Search query for customer name, email, or phone
     * @return List of customers
     */
    public ResponseEntity<CustomerList> searchCustomers(String query) {
        Map<String, String> params = new HashMap<>();
        params.put("query", query);
        return restClient.get("/customers/search.json", params, CustomerList.class);
    }
    
    /**
     * Gets a customer by ID using REST API.
     * @param customerId The customer ID
     * @return The customer
     */
    public ResponseEntity<Customer> getCustomerById(Long customerId) {
        return restClient.get("/customers/" + customerId + ".json", Customer.class);
    }
    
    /**
     * Gets a customer by email using REST API.
     * @param email The customer email
     * @return The customer list (should contain one customer)
     */
    public ResponseEntity<CustomerList> getCustomerByEmail(String email) {
        Map<String, String> params = new HashMap<>();
        params.put("query", "email:" + email);
        return restClient.get("/customers/search.json", params, CustomerList.class);
    }
    
    /**
     * Creates a new customer using REST API.
     * @param customer The customer to create
     * @return The created customer
     */
    public ResponseEntity<Customer> createCustomer(Customer customer) {
        Map<String, Customer> wrapper = new HashMap<>();
        wrapper.put("customer", customer);
        return restClient.post("/customers.json", wrapper, Customer.class);
    }
    
    /**
     * Updates a customer using REST API.
     * @param customerId The customer ID
     * @param customer The updated customer data
     * @return The updated customer
     */
    public ResponseEntity<Customer> updateCustomer(Long customerId, Customer customer) {
        Map<String, Customer> wrapper = new HashMap<>();
        wrapper.put("customer", customer);
        return restClient.put("/customers/" + customerId + ".json", wrapper, Customer.class);
    }
    
    /**
     * Deletes a customer using REST API.
     * @param customerId The customer ID
     * @return Response indicating success
     */
    public ResponseEntity<Void> deleteCustomer(Long customerId) {
        return restClient.delete("/customers/" + customerId + ".json");
    }
    
    /**
     * Gets customers who accept marketing using REST API.
     * @return List of customers who accept marketing
     */
    public ResponseEntity<CustomerList> getCustomersWhoAcceptMarketing() {
        Map<String, String> params = new HashMap<>();
        params.put("query", "accepts_marketing:true");
        return restClient.get("/customers/search.json", params, CustomerList.class);
    }
    
    /**
     * Gets customers by tag using REST API.
     * @param tag The customer tag
     * @return List of customers with the specified tag
     */
    public ResponseEntity<CustomerList> getCustomersByTag(String tag) {
        Map<String, String> params = new HashMap<>();
        params.put("query", "tag:" + tag);
        return restClient.get("/customers/search.json", params, CustomerList.class);
    }
    
    // GraphQL API Methods
    
    /**
     * Gets customers using GraphQL API.
     * @param first Number of customers to fetch
     * @param after Cursor for pagination
     * @return GraphQL response
     */
    public ResponseEntity<Map> getCustomersGraphQL(int first, String after) {
        String query = """
            query getCustomers($first: Int!, $after: String) {
                customers(first: $first, after: $after) {
                    edges {
                        node {
                            id
                            firstName
                            lastName
                            email
                            phone
                            state
                            acceptsMarketing
                            createdAt
                            updatedAt
                            ordersCount
                            totalSpent
                            defaultAddress {
                                id
                                firstName
                                lastName
                                address1
                                city
                                province
                                country
                                zip
                            }
                        }
                    }
                    pageInfo {
                        hasNextPage
                        hasPreviousPage
                        startCursor
                        endCursor
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("first", first);
        if (after != null) {
            variables.put("after", after);
        }
        
        return graphqlClient.query(query, variables, Map.class);
    }
    
    /**
     * Gets a customer by ID using GraphQL API.
     * @param customerId The customer ID
     * @return GraphQL response
     */
    public ResponseEntity<Map> getCustomerByIdGraphQL(String customerId) {
        String query = """
            query getCustomer($id: ID!) {
                customer(id: $id) {
                    id
                    firstName
                    lastName
                    email
                    phone
                    state
                    acceptsMarketing
                    marketingOptInLevel
                    verifiedEmail
                    createdAt
                    updatedAt
                    ordersCount
                    totalSpent
                    defaultAddress {
                        id
                        firstName
                        lastName
                        address1
                        address2
                        city
                        province
                        country
                        zip
                        phone
                    }
                    orders(first: 50) {
                        edges {
                            node {
                                id
                                name
                                orderStatus
                                financialStatus
                                fulfillmentStatus
                                totalPriceSet {
                                    shopMoney {
                                        amount
                                        currencyCode
                                    }
                                }
                                createdAt
                            }
                        }
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", customerId);
        
        return graphqlClient.query(query, variables, Map.class);
    }
    
    /**
     * Creates a customer using GraphQL API.
     * @param customerData The customer data
     * @return GraphQL response
     */
    public ResponseEntity<Map> createCustomerGraphQL(Map<String, Object> customerData) {
        String mutation = """
            mutation customerCreate($input: CustomerInput!) {
                customerCreate(input: $input) {
                    customer {
                        id
                        firstName
                        lastName
                        email
                        phone
                        state
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", customerData);
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
    
    /**
     * Updates a customer using GraphQL API.
     * @param customerId The customer ID
     * @param customerData The updated customer data
     * @return GraphQL response
     */
    public ResponseEntity<Map> updateCustomerGraphQL(String customerId, Map<String, Object> customerData) {
        String mutation = """
            mutation customerUpdate($input: CustomerInput!) {
                customerUpdate(input: $input) {
                    customer {
                        id
                        firstName
                        lastName
                        email
                        phone
                        state
                        updatedAt
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        customerData.put("id", customerId);
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", customerData);
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
    
    /**
     * Deletes a customer using GraphQL API.
     * @param customerId The customer ID
     * @return GraphQL response
     */
    public ResponseEntity<Map> deleteCustomerGraphQL(String customerId) {
        String mutation = """
            mutation customerDelete($input: CustomerDeleteInput!) {
                customerDelete(input: $input) {
                    deletedCustomerId
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of("id", customerId));
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
}
