package com.increff.pos.unit.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductFlow class.
 * 
 * These tests verify:
 * - Business logic orchestration between API layers
 * - Validation workflows
 * - FK-Id relationship validation
 * - Error handling and exception propagation
 * - Complex business rules implementation
 * 
 * All dependencies are mocked to ensure isolation and fast execution.
 * Focus is on testing the flow logic, not the underlying API implementations.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductFlowTest {

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private ProductFlow productFlow;

    private ProductPojo testProduct;
    private ClientPojo testClient;
    private Integer testClientId;

    @Before
    public void setUp() {
        // Setup test data using TestData factory with proper FK-Id relationships
        testClientId = 1;
        testClient = TestData.client(testClientId, "Test Client");
        testProduct = TestData.product("FLOW123", testClientId, "Flow Test Product", 100.0);
    }

    /**
     * Test validating and creating product - successful case.
     * Verifies that all validation steps are performed and FK-Id relationships are validated.
     */
    @Test
    public void testValidateAndCreateProduct_Success() {
        // Given: Valid product and client exists
        when(clientApi.getClientById(testClientId)).thenReturn(testClient);
        when(productApi.createProduct(testProduct)).thenReturn(testProduct);

        // When: Flow validates and creates product
        ProductPojo result = productFlow.validateAndCreateProduct(testProduct);

        // Then: Product should be created successfully
        assertEquals(testProduct, result);
        assertEquals(testClientId, result.getClientId()); // Verify FK-Id relationship

        // And: All validation steps should be performed
        verify(clientApi).getClientById(testClientId); // FK-Id validation
        verify(productApi).validateBarcodeUniqueness(testProduct.getBarcode(), null);
        verify(productApi).createProduct(testProduct);
        verify(inventoryApi).createInventory(testProduct.getId(), 0); // Inventory initialization
    }

    /**
     * Test validating and creating product - client not found.
     * Verifies that FK-Id validation catches non-existent clients.
     */
    @Test
    public void testValidateAndCreateProduct_ClientNotFound() {
        // Given: Client doesn't exist
        when(clientApi.getClientById(testClientId)).thenThrow(new ApiException(ErrorType.NOT_FOUND, "Client not found"));

        // When & Then: Exception should be thrown
        try {
            productFlow.validateAndCreateProduct(testProduct);
            fail("Expected ApiException to be thrown for non-existent client");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertEquals("Client not found", e.getMessage());
        }

        // And: Product creation should not be attempted
        verify(clientApi).getClientById(testClientId);
        verify(productApi, never()).validateBarcodeUniqueness(anyString(), any());
        verify(productApi, never()).createProduct(any(ProductPojo.class));
    }

    /**
     * Test validating and creating product - duplicate barcode.
     * Verifies that barcode uniqueness validation is performed.
     */
    @Test
    public void testValidateAndCreateProduct_DuplicateBarcode() {
        // Given: Client exists but barcode is duplicate
        when(clientApi.getClientById(testClientId)).thenReturn(testClient);
        doThrow(new ApiException(ErrorType.VALIDATION_ERROR, "Barcode already exists"))
            .when(productApi).validateBarcodeUniqueness(testProduct.getBarcode(), null);

        // When & Then: Exception should be thrown
        try {
            productFlow.validateAndCreateProduct(testProduct);
            fail("Expected ApiException to be thrown for duplicate barcode");
        } catch (ApiException e) {
            assertEquals(ErrorType.VALIDATION_ERROR, e.getErrorType());
            assertEquals("Barcode already exists", e.getMessage());
        }

        // And: Product creation should not be attempted
        verify(clientApi).getClientById(testClientId);
        verify(productApi).validateBarcodeUniqueness(testProduct.getBarcode(), null);
        verify(productApi, never()).createProduct(any(ProductPojo.class));
    }

    /**
     * Test validating and creating product - null product.
     * Verifies that null safety is handled properly.
     */
    @Test
    public void testValidateAndCreateProduct_NullProduct() {
        // Given: Null product
        ProductPojo nullProduct = null;

        // When & Then: Exception should be thrown
        try {
            productFlow.validateAndCreateProduct(nullProduct);
            fail("Expected exception to be thrown for null product");
        } catch (Exception e) {
            // Some form of exception should be thrown
            assertNotNull(e);
        }

        // And: No API calls should be made
        verify(clientApi, never()).getClientById(any());
        verify(productApi, never()).validateBarcodeUniqueness(anyString(), any());
        verify(productApi, never()).createProduct(any(ProductPojo.class));
    }

    /**
     * Test validating and creating product - null client ID.
     * Verifies that missing FK-Id is handled properly.
     */
    @Test
    public void testValidateAndCreateProduct_NullClientId() {
        // Given: Product with null client ID
        ProductPojo productWithNullClientId = TestData.product("NULL123", null, "Product with null client", 100.0);

        // When & Then: Exception should be thrown
        try {
            productFlow.validateAndCreateProduct(productWithNullClientId);
            fail("Expected exception to be thrown for null client ID");
        } catch (Exception e) {
            // Some form of exception should be thrown
            assertNotNull(e);
        }

        // And: Product creation should not be attempted
        verify(productApi, never()).createProduct(any(ProductPojo.class));
    }

    /**
     * Test validating product for update - successful case.
     * Verifies that update validation includes FK-Id and barcode uniqueness checks.
     */
    @Test
    public void testValidateProductForUpdate_Success() {
        // Given: Valid product update and client exists
        Integer productId = 1;
        String newBarcode = "UPDATED123";
        ProductPojo updatedProduct = TestData.product(productId, newBarcode, testClientId, "Updated Product", 150.0);
        
        when(clientApi.getClientById(testClientId)).thenReturn(testClient);

        // When: Flow validates product for update
        productFlow.validateProductForUpdate(updatedProduct);

        // Then: All validation steps should be performed
        verify(clientApi).getClientById(testClientId); // FK-Id validation
        verify(productApi).validateBarcodeUniqueness(newBarcode, productId); // Barcode uniqueness with exclusion
    }

    /**
     * Test validating product for update - client not found.
     * Verifies that FK-Id validation works for updates.
     */
    @Test
    public void testValidateProductForUpdate_ClientNotFound() {
        // Given: Client doesn't exist
        Integer productId = 1;
        ProductPojo updatedProduct = TestData.product(productId, "UPDATED123", testClientId, "Updated Product", 150.0);
        
        when(clientApi.getClientById(testClientId)).thenThrow(new ApiException(ErrorType.NOT_FOUND, "Client not found"));

        // When & Then: Exception should be thrown
        try {
            productFlow.validateProductForUpdate(updatedProduct);
            fail("Expected ApiException to be thrown for non-existent client");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertEquals("Client not found", e.getMessage());
        }

        // And: Barcode validation should not be performed
        verify(clientApi).getClientById(testClientId);
        verify(productApi, never()).validateBarcodeUniqueness(anyString(), any());
    }

    /**
     * Test validating product for update - duplicate barcode.
     * Verifies that barcode uniqueness validation works for updates.
     */
    @Test
    public void testValidateProductForUpdate_DuplicateBarcode() {
        // Given: Client exists but barcode is duplicate
        Integer productId = 1;
        String duplicateBarcode = "DUPLICATE123";
        ProductPojo updatedProduct = TestData.product(productId, duplicateBarcode, testClientId, "Updated Product", 150.0);
        
        when(clientApi.getClientById(testClientId)).thenReturn(testClient);
        doThrow(new ApiException(ErrorType.VALIDATION_ERROR, "Barcode already exists"))
            .when(productApi).validateBarcodeUniqueness(duplicateBarcode, productId);

        // When & Then: Exception should be thrown
        try {
            productFlow.validateProductForUpdate(updatedProduct);
            fail("Expected ApiException to be thrown for duplicate barcode");
        } catch (ApiException e) {
            assertEquals(ErrorType.VALIDATION_ERROR, e.getErrorType());
            assertEquals("Barcode already exists", e.getMessage());
        }

        // And: All validation steps should be attempted
        verify(clientApi).getClientById(testClientId);
        verify(productApi).validateBarcodeUniqueness(duplicateBarcode, productId);
    }

    /**
     * Test validating client existence - successful case.
     * Verifies that FK-Id validation works correctly.
     */
    @Test
    public void testValidateClientExists_Success() {
        // Given: Client exists
        when(clientApi.getClientById(testClientId)).thenReturn(testClient);

        // When: Flow validates client existence
        productFlow.validateClientExists(testClientId);

        // Then: No exception should be thrown
        verify(clientApi).getClientById(testClientId);
    }

    /**
     * Test validating client existence - client not found.
     * Verifies that FK-Id validation properly handles non-existent clients.
     */
    @Test
    public void testValidateClientExists_NotFound() {
        // Given: Client doesn't exist
        when(clientApi.getClientById(testClientId)).thenThrow(new ApiException(ErrorType.NOT_FOUND, "Client not found"));

        // When & Then: Exception should be thrown
        try {
            productFlow.validateClientExists(testClientId);
            fail("Expected ApiException to be thrown for non-existent client");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertEquals("Client not found", e.getMessage());
        }

        verify(clientApi).getClientById(testClientId);
    }

    /**
     * Test validating client existence - null client ID.
     * Verifies that null FK-Id is handled properly.
     */
    @Test
    public void testValidateClientExists_NullClientId() {
        // Given: Null client ID
        Integer nullClientId = null;

        // When & Then: Exception should be thrown
        try {
            productFlow.validateClientExists(nullClientId);
            fail("Expected exception to be thrown for null client ID");
        } catch (Exception e) {
            // Some form of exception should be thrown
            assertNotNull(e);
        }

        // And: API should not be called
        verify(clientApi, never()).getClientById(any());
    }

    /**
     * Test complex validation workflow.
     * Verifies that multiple validation steps work together correctly.
     */
    @Test
    public void testComplexValidationWorkflow() {
        // Given: Multiple products with different validation scenarios
        ProductPojo validProduct = TestData.product("VALID123", testClientId, "Valid Product", 100.0);
        ProductPojo invalidProduct = TestData.product("INVALID123", 999, "Invalid Product", 200.0);

        when(clientApi.getClientById(testClientId)).thenReturn(testClient);
        when(clientApi.getClientById(999)).thenThrow(new ApiException(ErrorType.NOT_FOUND, "Client not found"));
        when(productApi.createProduct(validProduct)).thenReturn(validProduct);

        // When: Valid product is processed
        ProductPojo result = productFlow.validateAndCreateProduct(validProduct);

        // Then: Valid product should be created
        assertEquals(validProduct, result);
        assertEquals(testClientId, result.getClientId());

        // When: Invalid product is processed
        try {
            productFlow.validateAndCreateProduct(invalidProduct);
            fail("Expected ApiException to be thrown for invalid product");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
        }

        // Then: Verify all interactions
        verify(clientApi).getClientById(testClientId);
        verify(clientApi).getClientById(999);
        verify(productApi).validateBarcodeUniqueness("VALID123", null);
        verify(productApi).createProduct(validProduct);
        verify(productApi, never()).createProduct(invalidProduct);
    }

    /**
     * Test error handling and exception propagation.
     * Verifies that exceptions from underlying APIs are properly handled.
     */
    @Test
    public void testErrorHandlingAndExceptionPropagation() {
        // Given: Various exception scenarios
        when(clientApi.getClientById(testClientId)).thenReturn(testClient);
        doThrow(new ApiException(ErrorType.VALIDATION_ERROR, "Barcode validation failed"))
            .when(productApi).validateBarcodeUniqueness(testProduct.getBarcode(), null);

        // When & Then: Exception should be propagated
        try {
            productFlow.validateAndCreateProduct(testProduct);
            fail("Expected ApiException to be thrown");
        } catch (ApiException e) {
            assertEquals(ErrorType.VALIDATION_ERROR, e.getErrorType());
            assertEquals("Barcode validation failed", e.getMessage());
        }

        // And: Flow should stop at the first error
        verify(clientApi).getClientById(testClientId);
        verify(productApi).validateBarcodeUniqueness(testProduct.getBarcode(), null);
        verify(productApi, never()).createProduct(any(ProductPojo.class));
    }
} 