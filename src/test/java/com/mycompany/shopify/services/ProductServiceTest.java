package com.mycompany.shopify.services;

import com.mycompany.shopify.http.GraphQLClient;
import com.mycompany.shopify.http.RestClient;
import com.mycompany.shopify.models.Product;
import com.mycompany.shopify.models.ProductList;
import com.mycompany.shopify.utils.TestDataGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ProductService.
 * Tests all methods with various scenarios including success, failure, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests")
class ProductServiceTest {
    
    @Mock
    private RestClient restClient;
    
    @Mock
    private GraphQLClient graphqlClient;
    
    private ProductService productService;
    
    @BeforeEach
    void setUp() {
        productService = new ProductService(restClient, graphqlClient);
    }
    
    @Nested
    @DisplayName("REST API Tests")
    class RestApiTests {
        
        @Test
        @DisplayName("Should get all products successfully")
        void shouldGetAllProductsSuccessfully() {
            // Arrange
            ProductList expectedProducts = TestDataGenerator.createSampleProductList(3);
            ResponseEntity<ProductList> expectedResponse = ResponseEntity.ok(expectedProducts);
            
            when(restClient.get(eq("/products.json"), eq(ProductList.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            ResponseEntity<ProductList> result = productService.getAllProducts();
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(expectedProducts, result.getBody());
            assertEquals(3, result.getBody().getProducts().size());
            
            verify(restClient).get(eq("/products.json"), eq(ProductList.class));
        }
        
        @Test
        @DisplayName("Should get product by ID successfully")
        void shouldGetProductByIdSuccessfully() {
            // Arrange
            Long productId = 123L;
            Product expectedProduct = TestDataGenerator.createSampleProduct();
            ResponseEntity<Product> expectedResponse = ResponseEntity.ok(expectedProduct);
            
            when(restClient.get(eq("/products/" + productId + ".json"), eq(Product.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            ResponseEntity<Product> result = productService.getProductById(productId);
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(expectedProduct, result.getBody());
            assertEquals(productId, result.getBody().getId());
            
            verify(restClient).get(eq("/products/" + productId + ".json"), eq(Product.class));
        }
        
        @Test
        @DisplayName("Should get product by handle successfully")
        void shouldGetProductByHandleSuccessfully() {
            // Arrange
            String handle = "test-product";
            ProductList expectedProducts = TestDataGenerator.createSampleProductList(1);
            ResponseEntity<ProductList> expectedResponse = ResponseEntity.ok(expectedProducts);
            
            when(restClient.get(eq("/products.json"), argThat(params -> 
                    params.containsKey("handle") && params.get("handle").equals(handle)), eq(ProductList.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            ResponseEntity<ProductList> result = productService.getProductByHandle(handle);
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(expectedProducts, result.getBody());
            
            verify(restClient).get(eq("/products.json"), anyMap(), eq(ProductList.class));
        }
        
        @Test
        @DisplayName("Should create product successfully")
        void shouldCreateProductSuccessfully() {
            // Arrange
            Product productToCreate = TestDataGenerator.createSampleProduct();
            Product createdProduct = TestDataGenerator.createSampleProduct();
            createdProduct.setId(999L);
            ResponseEntity<Product> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
            
            when(restClient.post(eq("/products.json"), anyMap(), eq(Product.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            ResponseEntity<Product> result = productService.createProduct(productToCreate);
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.CREATED, result.getStatusCode());
            assertEquals(createdProduct, result.getBody());
            assertEquals(999L, result.getBody().getId());
            
            verify(restClient).post(eq("/products.json"),  anyMap(), eq(Product.class));
        }
        
        @Test
        @DisplayName("Should handle product not found error")
        void shouldHandleProductNotFoundError() {
            // Arrange
            Long productId = 999L;
            when(restClient.get(eq("/products/" + productId + ".json"), eq(Product.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Product not found"));
            
            // Act & Assert
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                productService.getProductById(productId);
            });
            
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Product not found", exception.getStatusText());
            
            verify(restClient).get(eq("/products/" + productId + ".json"), eq(Product.class));
        }
        
        @Test
        @DisplayName("Should handle validation error")
        void shouldHandleValidationError() {
            // Arrange
            Product invalidProduct = new Product(); // Missing required fields
            when(restClient.post(eq("/products.json"), anyMap(), eq(Product.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed"));
            
            // Act & Assert
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                productService.createProduct(invalidProduct);
            });
            
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatusCode());
            assertEquals("Validation failed", exception.getStatusText());
            
            verify(restClient).post(eq("/products.json"), anyMap(), eq(Product.class));
        }
        
        @Test
        @DisplayName("Should handle network error")
        void shouldHandleNetworkError() {
            // Arrange
            when(restClient.get(eq("/products.json"), eq(ProductList.class)))
                    .thenThrow(new ResourceAccessException("Network connection failed"));
            
            // Act & Assert
            ResourceAccessException exception = assertThrows(ResourceAccessException.class, () -> {
                productService.getAllProducts();
            });
            
            assertEquals("Network connection failed", exception.getMessage());
            
            verify(restClient).get(eq("/products.json"), eq(ProductList.class));
        }
    }
    
    @Nested
    @DisplayName("GraphQL API Tests")
    class GraphQLApiTests {
        
        @Test
        @DisplayName("Should get products via GraphQL successfully")
        void shouldGetProductsViaGraphQLSuccessfully() {
            // Arrange
            Map<String, Object> expectedResponse = TestDataGenerator.createSampleGraphQLResponse();
            ResponseEntity<Map> expectedResponseEntity = ResponseEntity.ok(expectedResponse);
            
            when(graphqlClient.query(anyString(), anyMap(), eq(Map.class)))
                    .thenReturn(expectedResponseEntity);
            
            // Act
            ResponseEntity<Map> result = productService.getProductsGraphQL(10, null);
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(expectedResponse, result.getBody());
            
            verify(graphqlClient).query(anyString(), anyMap(), eq(Map.class));
        }
        
        @Test
        @DisplayName("Should create product via GraphQL successfully")
        void shouldCreateProductViaGraphQLSuccessfully() {
            // Arrange
            Map<String, Object> productData = new HashMap<>();
            productData.put("title", "New Product");
            productData.put("vendor", "Test Vendor");
            
            Map<String, Object> expectedResponse = TestDataGenerator.createSampleGraphQLResponse();
            ResponseEntity<Map> expectedResponseEntity = ResponseEntity.ok(expectedResponse);
            
            when(graphqlClient.mutation(anyString(),anyMap(), eq(Map.class)))
                    .thenReturn(expectedResponseEntity);
            
            // Act
            ResponseEntity<Map> result = productService.createProductGraphQL(productData);
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(expectedResponse, result.getBody());
            
            verify(graphqlClient).mutation(anyString(), anyMap(), eq(Map.class));
        }
        
        @Test
        @DisplayName("Should handle GraphQL error response")
        void shouldHandleGraphQLErrorResponse() {
            // Arrange
            Map<String, Object> errorResponse = TestDataGenerator.createSampleErrorResponse();
            ResponseEntity<Map> errorResponseEntity = ResponseEntity.ok(errorResponse);
            
            when(graphqlClient.query(anyString(), anyMap(), eq(Map.class)))
                    .thenReturn(errorResponseEntity);
            
            // Act
            ResponseEntity<Map> result = productService.getProductsGraphQL(10, null);
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertTrue(result.getBody().containsKey("errors"));
            
            verify(graphqlClient).query(anyString(), anyMap(), eq(Map.class));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCasesAndErrorScenarios {
        
        @Test
        @DisplayName("Should handle null product gracefully")
        void shouldHandleNullProductGracefully() {
            // Arrange
            when(restClient.post(eq("/products.json"), anyMap(), eq(Product.class)))
                    .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Product cannot be null"));
            
            // Act & Assert
            HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
                productService.createProduct(null);
            });
            
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            
            verify(restClient).post(eq("/products.json"), anyMap(), eq(Product.class));
        }
        
        @Test
        @DisplayName("Should handle empty product list response")
        void shouldHandleEmptyProductListResponse() {
            // Arrange
            ProductList emptyProducts = new ProductList();
            emptyProducts.setProducts(java.util.Collections.emptyList());
            ResponseEntity<ProductList> expectedResponse = ResponseEntity.ok(emptyProducts);
            
            when(restClient.get(eq("/products.json"), eq(ProductList.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            ResponseEntity<ProductList> result = productService.getAllProducts();
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertTrue(result.getBody().getProducts().isEmpty());
            
            verify(restClient).get(eq("/products.json"), eq(ProductList.class));
        }
        
        @Test
        @DisplayName("Should handle large product list response")
        void shouldHandleLargeProductListResponse() {
            // Arrange
            ProductList largeProductList = TestDataGenerator.createSampleProductList(100);
            ResponseEntity<ProductList> expectedResponse = ResponseEntity.ok(largeProductList);
            
            when(restClient.get(eq("/products.json"), eq(ProductList.class)))
                    .thenReturn(expectedResponse);
            
            // Act
            ResponseEntity<ProductList> result = productService.getAllProducts();
            
            // Assert
            assertNotNull(result);
            assertEquals(HttpStatus.OK, result.getStatusCode());
            assertEquals(100, result.getBody().getProducts().size());
            
            verify(restClient).get(eq("/products.json"), eq(ProductList.class));
        }
    }
    
    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {
        
        @Test
        @DisplayName("Should handle complete product lifecycle")
        void shouldHandleCompleteProductLifecycle() {
            // Arrange
            Product productToCreate = TestDataGenerator.createSampleProduct();
            Product createdProduct = TestDataGenerator.createSampleProduct();
            createdProduct.setId(999L);
            
            // Create product
            when(restClient.post(eq("/products.json"), anyMap(), eq(Product.class)))
                    .thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(createdProduct));
            
            // Get created product
            when(restClient.get(eq("/products/999.json"), eq(Product.class)))
                    .thenReturn(ResponseEntity.ok(createdProduct));
            
            // Act - Create product
            ResponseEntity<Product> createResult = productService.createProduct(productToCreate);
            
            // Assert - Create
            assertNotNull(createResult);
            assertEquals(HttpStatus.CREATED, createResult.getStatusCode());
            assertEquals(999L, createResult.getBody().getId());
            
            // Act - Get created product
            ResponseEntity<Product> getResult = productService.getProductById(999L);
            
            // Assert - Get
            assertNotNull(getResult);
            assertEquals(HttpStatus.OK, getResult.getStatusCode());
            assertEquals(createdProduct, getResult.getBody());
            
            // Verify all interactions
            verify(restClient).post(eq("/products.json"), anyMap(), eq(Product.class));
            verify(restClient).get(eq("/products/999.json"), eq(Product.class));
        }
    }
}
