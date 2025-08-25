package com.mycompany.shopify.services;

import com.mycompany.shopify.http.GraphQLClient;
import com.mycompany.shopify.http.RestClient;
import com.mycompany.shopify.models.Product;
import com.mycompany.shopify.models.ProductList;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Shopify products.
 * Provides methods for CRUD operations using both REST and GraphQL APIs.
 */
@Service
public class ProductService {
    
    private final RestClient restClient;
    private final GraphQLClient graphqlClient;
    
    public ProductService(RestClient restClient, GraphQLClient graphqlClient) {
        this.restClient = restClient;
        this.graphqlClient = graphqlClient;
    }
    
    // REST API Methods
    
    /**
     * Gets all products using REST API.
     * @return List of products
     */
    public ResponseEntity<ProductList> getAllProducts() {
        return restClient.get("/products.json", ProductList.class);
    }
    
    /**
     * Gets products with pagination using REST API.
     * @param limit Maximum number of products to return
     * @param pageInfo Pagination cursor
     * @return List of products
     */
    public ResponseEntity<ProductList> getProducts(int limit, String pageInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("limit", String.valueOf(limit));
        if (pageInfo != null) {
            params.put("page_info", pageInfo);
        }
        return restClient.get("/products.json", params, ProductList.class);
    }
    
    /**
     * Gets products by status using REST API.
     * @param status Product status (active, archived, draft)
     * @return List of products
     */
    public ResponseEntity<ProductList> getProductsByStatus(String status) {
        Map<String, String> params = new HashMap<>();
        params.put("status", status);
        return restClient.get("/products.json", params, ProductList.class);
    }
    
    /**
     * Gets a product by ID using REST API.
     * @param productId The product ID
     * @return The product
     */
    public ResponseEntity<Product> getProductById(Long productId) {
        return restClient.get("/products/" + productId + ".json", Product.class);
    }
    
    /**
     * Gets a product by handle using REST API.
     * @param handle The product handle
     * @return The product list (should contain one product)
     */
    public ResponseEntity<ProductList> getProductByHandle(String handle) {
        Map<String, String> params = new HashMap<>();
        params.put("handle", handle);
        return restClient.get("/products.json", params, ProductList.class);
    }
    
    /**
     * Creates a new product using REST API.
     * @param product The product to create
     * @return The created product
     */
    public ResponseEntity<Product> createProduct(Product product) {
        Map<String, Product> wrapper = new HashMap<>();
        wrapper.put("product", product);
        return restClient.post("/products.json", wrapper, Product.class);
    }
    
    /**
     * Updates a product using REST API.
     * @param productId The product ID
     * @param product The updated product data
     * @return The updated product
     */
    public ResponseEntity<Product> updateProduct(Long productId, Product product) {
        Map<String, Product> wrapper = new HashMap<>();
        wrapper.put("product", product);
        return restClient.put("/products/" + productId + ".json", wrapper, Product.class);
    }
    
    /**
     * Deletes a product using REST API.
     * @param productId The product ID
     * @return Response indicating success
     */
    public ResponseEntity<Void> deleteProduct(Long productId) {
        return restClient.delete("/products/" + productId + ".json");
    }
    
    /**
     * Publishes a product using REST API.
     * @param productId The product ID
     * @return The published product
     */
    public ResponseEntity<Product> publishProduct(Long productId) {
        Map<String, Object> publishData = new HashMap<>();
        publishData.put("product", Map.of("id", productId, "published", true));
        return restClient.put("/products/" + productId + ".json", publishData, Product.class);
    }
    
    /**
     * Unpublishes a product using REST API.
     * @param productId The product ID
     * @return The unpublished product
     */
    public ResponseEntity<Product> unpublishProduct(Long productId) {
        Map<String, Object> publishData = new HashMap<>();
        publishData.put("product", Map.of("id", productId, "published", false));
        return restClient.put("/products/" + productId + ".json", publishData, Product.class);
    }
    
    // GraphQL API Methods
    
    /**
     * Gets products using GraphQL API.
     * @param first Number of products to fetch
     * @param after Cursor for pagination
     * @return GraphQL response
     */
    public ResponseEntity<Map> getProductsGraphQL(int first, String after) {
        String query = """
            query getProducts($first: Int!, $after: String) {
                products(first: $first, after: $after) {
                    edges {
                        node {
                            id
                            title
                            handle
                            status
                            vendor
                            productType
                            createdAt
                            updatedAt
                            variants(first: 10) {
                                edges {
                                    node {
                                        id
                                        title
                                        sku
                                        price
                                        inventoryQuantity
                                    }
                                }
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
     * Gets a product by ID using GraphQL API.
     * @param productId The product ID
     * @return GraphQL response
     */
    public ResponseEntity<Map> getProductByIdGraphQL(String productId) {
        String query = """
            query getProduct($id: ID!) {
                product(id: $id) {
                    id
                    title
                    handle
                    status
                    vendor
                    productType
                    bodyHtml
                    createdAt
                    updatedAt
                    variants(first: 50) {
                        edges {
                            node {
                                id
                                title
                                sku
                                barcode
                                price
                                compareAtPrice
                                inventoryQuantity
                                option1
                                option2
                                option3
                            }
                        }
                    }
                    options {
                        id
                        name
                        position
                        values
                    }
                    images(first: 50) {
                        edges {
                            node {
                                id
                                url
                                altText
                                width
                                height
                            }
                        }
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("id", productId);
        
        return graphqlClient.query(query, variables, Map.class);
    }
    
    /**
     * Creates a product using GraphQL API.
     * @param productData The product data
     * @return GraphQL response
     */
    public ResponseEntity<Map> createProductGraphQL(Map<String, Object> productData) {
        String mutation = """
            mutation productCreate($input: ProductInput!) {
                productCreate(input: $input) {
                    product {
                        id
                        title
                        handle
                        status
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", productData);
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
    
    /**
     * Updates a product using GraphQL API.
     * @param productId The product ID
     * @param productData The updated product data
     * @return GraphQL response
     */
    public ResponseEntity<Map> updateProductGraphQL(String productId, Map<String, Object> productData) {
        String mutation = """
            mutation productUpdate($input: ProductInput!) {
                productUpdate(input: $input) {
                    product {
                        id
                        title
                        handle
                        status
                        updatedAt
                    }
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        productData.put("id", productId);
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", productData);
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
    
    /**
     * Deletes a product using GraphQL API.
     * @param productId The product ID
     * @return GraphQL response
     */
    public ResponseEntity<Map> deleteProductGraphQL(String productId) {
        String mutation = """
            mutation productDelete($input: ProductDeleteInput!) {
                productDelete(input: $input) {
                    deletedProductId
                    userErrors {
                        field
                        message
                    }
                }
            }
            """;
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("input", Map.of("id", productId));
        
        return graphqlClient.mutation(mutation, variables, Map.class);
    }
}
