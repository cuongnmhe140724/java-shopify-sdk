package com.mycompany.shopify.services;

import com.mycompany.shopify.http.GraphQLClient;
import com.mycompany.shopify.http.RestClient;
import com.mycompany.shopify.models.Order;
import com.mycompany.shopify.models.OrderList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Shopify orders.
 * Provides methods for CRUD operations using both REST and GraphQL APIs.
 */
@Service
public class OrderService {
    
    private final RestClient restClient;
    private final GraphQLClient graphqlClient;
    
    public OrderService(RestClient restClient, GraphQLClient graphqlClient) {
        this.restClient = restClient;
        this.graphqlClient = graphqlClient;
    }
    
    // REST API Methods
    
    /**
     * Gets all orders using REST API.
     * @return List of orders
     */
    public ResponseEntity<OrderList> getAllOrders() {
        return restClient.get("/orders.json", OrderList.class);
    }
    
    /**
     * Gets orders with pagination using REST API.
     * @param limit Maximum number of orders to return
     * @param pageInfo Pagination cursor
     * @return List of orders
     */
    public ResponseEntity<OrderList> getOrders(int limit, String pageInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        if (pageInfo != null) {
            params.put("page_info", pageInfo);
        }
        return restClient.get("/orders.json", params, OrderList.class);
    }
    
    /**
     * Gets orders by status using REST API.
     * @param status Order status (open, closed, cancelled, any)
     * @return List of orders
     */
    public ResponseEntity<OrderList> getOrdersByStatus(String status) {
        Map<String, String> params = new HashMap<>();
        params.put("status", status);
        return restClient.get("/orders.json", params, OrderList.class);
    }
    
    /**
     * Gets orders by financial status using REST API.
     * @param financialStatus Financial status (authorized, pending, paid, partially_paid, refunded, voided)
     * @return List of orders
     */
    public ResponseEntity<OrderList> getOrdersByFinancialStatus(String financialStatus) {
        Map<String, String> params = new HashMap<>();
        params.put("financial_status", financialStatus);
        return restClient.get("/orders.json", params, OrderList.class);
    }
    
    /**
     * Gets orders by fulfillment status using REST API.
     * @param fulfillmentStatus Fulfillment status (fulfilled, partial, unfulfilled)
     * @return List of orders
     */
    public ResponseEntity<OrderList> getOrdersByFulfillmentStatus(String fulfillmentStatus) {
        Map<String, String> params = new HashMap<>();
        params.put("fulfillment_status", fulfillmentStatus);
        return restClient.get("/orders.json", params, OrderList.class);
    }
    
    /**
     * Gets orders created after a specific date using REST API.
     * @param createdAtMin Minimum creation date
     * @return List of orders
     */
    public ResponseEntity<OrderList> getOrdersCreatedAfter(LocalDateTime createdAtMin) {
        Map<String, String> params = new HashMap<>();
        params.put("created_at_min", createdAtMin.toString());
        return restClient.get("/orders.json", params, OrderList.class);
    }
    
    /**
     * Gets orders created before a specific date using REST API.
     * @param createdAtMax Maximum creation date
     * @return List of orders
     */
    public ResponseEntity<OrderList> getOrdersCreatedBefore(LocalDateTime createdAtMax) {
        Map<String, String> params = new HashMap<>();
        params.put("created_at_max", createdAtMax.toString());
        return restClient.get("/orders.json", params, OrderList.class);
    }
    
    /**
     * Gets an order by ID using REST API.
     * @param orderId The order ID
     * @return The order
     */
    public ResponseEntity<Order> getOrderById(Long orderId) {
        return restClient.get("/orders/" + orderId + ".json", Order.class);
    }
    
    /**
     * Gets an order by order number using REST API.
     * @param orderNumber The order number
     * @return The order list (should contain one order)
     */
    public ResponseEntity<OrderList> getOrderByNumber(String orderNumber) {
        Map<String, String> params = new HashMap<>();
        params.put("name", orderNumber);
        return restClient.get("/orders.json", params, OrderList.class);
    }
    
    /**
     * Creates a new order using REST API.
     * @param order The order to create
     * @return The created order
     */
    public ResponseEntity<Order> createOrder(Order order) {
        Map<String, Order> wrapper = new HashMap<>();
        wrapper.put("order", order);
        return restClient.post("/orders.json", wrapper, Order.class);
    }
    
    /**
     * Updates an order using REST API.
     * @param orderId The order ID
     * @param order The updated order data
     * @return The updated order
     */
    public ResponseEntity<Order> updateOrder(Long orderId, Order order) {
        Map<String, Order> wrapper = new HashMap<>();
        wrapper.put("order", order);
        return restClient.put("/orders/" + orderId + ".json", wrapper, Order.class);
    }
    
    /**
     * Cancels an order using REST API.
     * @param orderId The order ID
     * @param reason The cancellation reason
     * @return The cancelled order
     */
    public ResponseEntity<Order> cancelOrder(Long orderId, String reason) {
        Map<String, Object> cancelData = new HashMap<>();
        cancelData.put("order", Map.of("id", orderId, "cancelled_reason", reason));
        return restClient.post("/orders/" + orderId + "/cancel.json", cancelData, Order.class);
    }
    
    /**
     * Closes an order using REST API.
     * @param orderId The order ID
     * @return The closed order
     */
    public ResponseEntity<Order> closeOrder(Long orderId) {
        return restClient.post("/orders/" + orderId + "/close.json", null, Order.class);
    }
    
    /**
     * Reopens a closed order using REST API.
     * @param orderId The order ID
     * @return The reopened order
     */
    public ResponseEntity<Order> reopenOrder(Long orderId) {
        return restClient.post("/orders/" + orderId + "/open.json", null, Order.class);
    }
    
    // GraphQL API Methods
    
    /**
     * Gets orders using GraphQL API.
     * @param first Number of orders to fetch
     * @param after Cursor for pagination
     * @return GraphQL response
     */
    public ResponseEntity<Map> getOrdersGraphQL(int first, String after) {
        String query = """
            query getOrders($first: Int!, $after: String) {
                orders(first: $first, after: $after) {
                    edges {
                        node {
                            id
                            name
                            email
                            phone
                            currency
                            financialStatus
                            fulfillmentStatus
                            orderStatus
                            createdAt
                            updatedAt
                            totalPriceSet {
                                shopMoney {
                                    amount
                                    currencyCode
                                }
                            }
                            customer {
                                id
                                firstName
                                lastName
                                email
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
     * Gets an order by ID using GraphQL API.
     * @param orderId The order ID
     * @return GraphQL response
     */
    public ResponseEntity<Map> getOrderByIdGraphQL(String orderId) {
        String query = """
            query getOrder($id: ID!) {
                order(id: $id) {
                    id
                    name
                    email
                    phone
                    currency
                    financialStatus
                    fulfillmentStatus
                    orderStatus
                    createdAt
                    updatedAt
                    processedAt
                    cancelledAt
                    closedAt
                    cancelledReason
                    totalPriceSet {
                        shopMoney {
                            amount
                            currencyCode
                        }
                    }
                    subtotalPriceSet {
                        shopMoney {
                            amount
                            currencyCode
                        }
                    }
                    totalTaxSet {
                        shopMoney {
                            amount
                            currencyCode
                        }
                    }
                    customer {
                        id
                        firstName
                        lastName
                        email
                        phone
                    }
                    lineItems(first: 50) {
                        edges {
                            node {
                                id
                                title
                                quantity
                                variant {
                                    id
                                    title
                                    sku
                                    price
                                }
                            }
                        }
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", orderId);
        
        return graphqlClient.query(query, variables, Map.class);
    }
    
    /**
     * Creates an order using GraphQL API.
     * @param orderData The order data
     * @return GraphQL response
     */
    public ResponseEntity<Map> createOrderGraphQL(Map<String, Object> orderData) {
        String mutation = """
            mutation orderCreate($input: OrderInput!) {
                orderCreate(input: $input) {
                    order {
                        id
                        name
                        orderStatus
                        financialStatus
                        fulfillmentStatus
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", orderData);
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
    
    /**
     * Updates an order using GraphQL API.
     * @param orderId The order ID
     * @param orderData The updated order data
     * @return GraphQL response
     */
    public ResponseEntity<Map> updateOrderGraphQL(String orderId, Map<String, Object> orderData) {
        String mutation = """
            mutation orderUpdate($input: OrderInput!) {
                orderUpdate(input: $input) {
                    order {
                        id
                        name
                        orderStatus
                        financialStatus
                        fulfillmentStatus
                        updatedAt
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        orderData.put("id", orderId);
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", orderData);
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
    
    /**
     * Cancels an order using GraphQL API.
     * @param orderId The order ID
     * @param reason The cancellation reason
     * @return GraphQL response
     */
    public ResponseEntity<Map> cancelOrderGraphQL(String orderId, String reason) {
        String mutation = """
            mutation orderCancel($input: OrderCancelInput!) {
                orderCancel(input: $input) {
                    order {
                        id
                        cancelledAt
                        cancelledReason
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of("id", orderId, "reason", reason));
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
}
