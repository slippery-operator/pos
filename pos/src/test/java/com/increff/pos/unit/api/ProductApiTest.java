package com.increff.pos.unit.api;

import com.increff.pos.api.ProductApi;
import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductApi class.
 * 
 * These tests focus on:
 * - Business logic validation
 * - Error handling
 * - Interaction with DAO layer
 * - Proper use of FK-Id relationships
 * 
 * All dependencies are mocked to ensure isolation and fast execution.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductApiTest {

    @Mock
    private ProductDao productDao;

    @InjectMocks
    private ProductApi productApi;

    private ProductPojo testProduct;
    private Integer testClientId;

    @Before
    public void setUp() {
        // Setup test data using TestData factory with proper FK-Id relationships
        testClientId = 1;
        testProduct = TestData.product(1, "TEST123", testClientId, "Test Product", 100.0);
    }

    /**
     * Test finding products by barcodes - successful case.
     * Verifies that the method correctly maps barcodes to product IDs.
     */
    @Test
    public void testFindProductsByBarcodes_Success() {
        // Given: Multiple products with different barcodes
        List<String> barcodes = Arrays.asList("BC001", "BC002", "BC003");
        List<ProductPojo> products = Arrays.asList(
            TestData.product(1, "BC001", testClientId, "Product 1", 50.0),
            TestData.product(2, "BC002", testClientId, "Product 2", 75.0),
            TestData.product(3, "BC003", testClientId, "Product 3", 100.0)
        );

        // When: DAO returns the products
        when(productDao.selectByBarcodes(any(Set.class))).thenReturn(products);

        // Then: API should return correct barcode to ID mapping
        Map<String, Integer> result = productApi.findProductsByBarcodes(barcodes);

        assertEquals(3, result.size());
        assertEquals(Integer.valueOf(1), result.get("BC001"));
        assertEquals(Integer.valueOf(2), result.get("BC002"));
        assertEquals(Integer.valueOf(3), result.get("BC003"));

        // Verify DAO was called with unique barcodes set
        verify(productDao).selectByBarcodes(argThat(set -> set.size() == 3));
    }

    /**
     * Test finding products by barcodes - with duplicates.
     * Verifies that duplicate barcodes are handled correctly.
     */
    @Test
    public void testFindProductsByBarcodes_WithDuplicates() {
        // Given: List with duplicate barcodes
        List<String> barcodes = Arrays.asList("BC001", "BC002", "BC001", "BC002");
        List<ProductPojo> products = Arrays.asList(
            TestData.product(1, "BC001", testClientId, "Product 1", 50.0),
            TestData.product(2, "BC002", testClientId, "Product 2", 75.0)
        );

        when(productDao.selectByBarcodes(any(Set.class))).thenReturn(products);

        // When: API processes the list
        Map<String, Integer> result = productApi.findProductsByBarcodes(barcodes);

        // Then: Duplicates should be removed and result should be correct
        assertEquals(2, result.size());
        verify(productDao).selectByBarcodes(argThat(set -> set.size() == 2));
    }

    /**
     * Test searching products - successful case.
     * Verifies that search parameters are passed correctly to DAO.
     */
    @Test
    public void testSearchProducts_Success() {
        // Given: Search parameters
        String barcode = "test";
        String productName = "product";
        int page = 0;
        int size = 10;

        List<ProductPojo> expectedProducts = Arrays.asList(
            TestData.product(1, "TEST123", testClientId, "Test Product", 100.0),
            TestData.product(2, "TEST456", testClientId, "Test Product 2", 150.0)
        );

        // When: DAO returns matching products
        when(productDao.findBySearchCriteria(barcode, productName, page, size))
            .thenReturn(expectedProducts);

        // Then: API should return the same products
        List<ProductPojo> result = productApi.searchProducts(barcode, productName, page, size);

        assertEquals(2, result.size());
        assertEquals(expectedProducts, result);
        verify(productDao).findBySearchCriteria(barcode, productName, page, size);
    }

    /**
     * Test getting product by ID - successful case.
     * Verifies that existing product is returned correctly.
     */
    @Test
    public void testGetProductById_Success() {
        // Given: Product exists in database
        Integer productId = 1;
        when(productDao.selectById(productId)).thenReturn(testProduct);

        // When: API retrieves product
        ProductPojo result = productApi.getProductById(productId);

        // Then: Product should be returned
        assertEquals(testProduct, result);
        assertEquals(testClientId, result.getClientId()); // Verify FK-Id relationship
        verify(productDao).selectById(productId);
    }

    /**
     * Test getting product by ID - product not found.
     * Verifies that appropriate exception is thrown when product doesn't exist.
     */
    @Test
    public void testGetProductById_NotFound() {
        // Given: Product doesn't exist
        Integer productId = 999;
        when(productDao.selectById(productId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            productApi.getProductById(productId);
            fail("Expected ApiException to be thrown");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
//            assertEquals("Product not found", e.getMessage());
        }

        verify(productDao).selectById(productId);
    }

    /**
     * Test creating product - successful case.
     * Verifies that product is created with proper FK-Id relationships.
     */
    @Test
    public void testCreateProduct_Success() {
        // Given: Valid product to create
        ProductPojo newProduct = TestData.product("NEW123", testClientId, "New Product", 200.0);

        // When: API creates product
        ProductPojo result = productApi.createProduct(newProduct);

        // Then: Product should be inserted and returned
        assertEquals(newProduct, result);
        assertEquals(testClientId, result.getClientId()); // Verify FK-Id is preserved
        verify(productDao).insert(newProduct);
    }

    /**
     * Test updating product - successful case.
     * Verifies that product is updated correctly with FK-Id relationships intact.
     */
    @Test
    public void testUpdateProduct_Success() {
        // Given: Existing product and update parameters
        Integer productId = 1;
        String newName = "Updated Product";
        Double newMrp = 150.0;
        String newImageUrl = "http://example.com/image.jpg";

        when(productDao.selectById(productId)).thenReturn(testProduct);

        // When: API updates product
        ProductPojo result = productApi.updateProduct(productId, newName, newMrp, newImageUrl);

        // Then: Product should be updated with new values
        assertEquals(newName, result.getName());
        assertEquals(newMrp, result.getMrp());
        assertEquals(newImageUrl, result.getImageUrl());
        assertEquals(testClientId, result.getClientId()); // FK-Id should remain unchanged
        verify(productDao).selectById(productId);
//        verify(productDao).update(testProduct);
    }

    /**
     * Test updating product - product not found.
     * Verifies that appropriate exception is thrown when updating non-existent product.
     */
    @Test
    public void testUpdateProduct_NotFound() {
        // Given: Product doesn't exist
        Integer productId = 999;
        when(productDao.selectById(productId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            productApi.updateProduct(productId, "New Name", 100.0, null);
            fail("Expected ApiException to be thrown");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
//            assertEquals("Product not found", e.getMessage());
        }

        verify(productDao).selectById(productId);
        verify(productDao, never()).update(any(ProductPojo.class));
    }

    /**
     * Test barcode uniqueness validation - unique barcode.
     * Verifies that validation passes for unique barcodes.
     */
    @Test
    public void testValidateBarcodeUniqueness_Unique() {
        // Given: Barcode doesn't exist
        String barcode = "UNIQUE123";
        when(productDao.selectByBarcode(barcode)).thenReturn(null);

        // When & Then: No exception should be thrown
        productApi.validateBarcodeUniqueness(barcode, null);

        verify(productDao).selectByBarcode(barcode);
    }

    /**
     * Test barcode uniqueness validation - duplicate barcode.
     * Verifies that validation fails for duplicate barcodes.
     */
    @Test
    public void testValidateBarcodeUniqueness_Duplicate() {
        // Given: Product with barcode already exists
        String barcode = "DUPLICATE123";
        ProductPojo existingProduct = TestData.product(2, barcode, testClientId, "Existing", 100.0);
        when(productDao.selectByBarcode(barcode)).thenReturn(existingProduct);

        // When & Then: Exception should be thrown
        try {
            productApi.validateBarcodeUniqueness(barcode, null);
            fail("Expected ApiException to be thrown");
        } catch (ApiException e) {
            assertEquals(ErrorType.CONFLICT, e.getErrorType());
            assertTrue(e.getMessage().contains("already exists"));
        }

        verify(productDao).selectByBarcode(barcode);
    }

    /**
     * Test barcode uniqueness validation - excluding current product.
     * Verifies that validation passes when excluding the current product being updated.
     */
    @Test
    public void testValidateBarcodeUniqueness_ExcludeCurrent() {
        // Given: Barcode exists but belongs to the product being updated
        String barcode = "UPDATE123";
        Integer excludeId = 1;
        ProductPojo existingProduct = TestData.product(excludeId, barcode, testClientId, "Existing", 100.0);
        when(productDao.selectByBarcode(barcode)).thenReturn(existingProduct);

        // When & Then: No exception should be thrown
        productApi.validateBarcodeUniqueness(barcode, excludeId);

        verify(productDao).selectByBarcode(barcode);
    }

    /**
     * Test bulk product creation - successful case.
     * Verifies that multiple products are created correctly.
     */
    @Test
    public void testBulkCreateProducts_Success() {
        // Given: List of products to create
        List<ProductPojo> products = Arrays.asList(
            TestData.product("BULK001", testClientId, "Bulk Product 1", 100.0),
            TestData.product("BULK002", testClientId, "Bulk Product 2", 150.0),
            TestData.product("BULK003", testClientId, "Bulk Product 3", 200.0)
        );

        // When: API creates products in bulk
        List<ProductPojo> result = productApi.bulkCreateProducts(products);

        // Then: All products should be inserted
        assertEquals(3, result.size());
        for (ProductPojo product : result) {
            assertEquals(testClientId, product.getClientId()); // Verify FK-Id relationships
        }
        verify(productDao).bulkInsert(products);
    }

    /**
     * Test batch barcode uniqueness validation - all unique.
     * Verifies that validation passes when all barcodes are unique.
     */
    @Test
    public void testValidateBarcodesUniquenessBatch_AllUnique() {
        // Given: Set of unique barcodes
        Set<String> barcodes = new HashSet<>(Arrays.asList("BC001", "BC002", "BC003"));
        when(productDao.selectByBarcodes(barcodes)).thenReturn(Collections.emptyList());

        // When: API validates barcodes
        Map<String, Boolean> result = productApi.validateBarcodesUniquenessBatch(barcodes);

        // Then: All should be marked as unique
        assertEquals(3, result.size());
        assertTrue(result.get("BC001"));
        assertTrue(result.get("BC002"));
        assertTrue(result.get("BC003"));
        verify(productDao).selectByBarcodes(barcodes);
    }

    /**
     * Test batch barcode uniqueness validation - some duplicates.
     * Verifies that validation correctly identifies duplicate barcodes.
     */
    @Test
    public void testValidateBarcodesUniquenessBatch_SomeDuplicates() {
        // Given: Some barcodes already exist
        Set<String> barcodes = new HashSet<>(Arrays.asList("BC001", "BC002", "BC003"));
        List<ProductPojo> existingProducts = Arrays.asList(
            TestData.product(1, "BC001", testClientId, "Existing 1", 100.0),
            TestData.product(2, "BC003", testClientId, "Existing 3", 200.0)
        );
        when(productDao.selectByBarcodes(barcodes)).thenReturn(existingProducts);

        // When: API validates barcodes
        Map<String, Boolean> result = productApi.validateBarcodesUniquenessBatch(barcodes);

        // Then: Duplicates should be marked as false, unique as true
        assertEquals(3, result.size());
        assertFalse(result.get("BC001")); // Duplicate
        assertTrue(result.get("BC002"));  // Unique
        assertFalse(result.get("BC003")); // Duplicate
        verify(productDao).selectByBarcodes(barcodes);
    }

    /**
     * Test checking product existence - product exists.
     * Verifies that method correctly identifies existing products.
     */
    @Test
    public void testCheckProductExists_Exists() {
        // Given: Product exists
        String barcode = "EXISTS123";
        when(productDao.selectByBarcode(barcode)).thenReturn(testProduct);

        // When: API checks existence
        boolean result = productApi.checkProductExists(barcode);

        // Then: Should return true
        assertTrue(result);
        verify(productDao).selectByBarcode(barcode);
    }

    /**
     * Test checking product existence - product doesn't exist.
     * Verifies that method correctly identifies non-existent products.
     */
    @Test
    public void testCheckProductExists_NotExists() {
        // Given: Product doesn't exist
        String barcode = "NOTEXISTS123";
        when(productDao.selectByBarcode(barcode)).thenReturn(null);

        // When: API checks existence
        boolean result = productApi.checkProductExists(barcode);

        // Then: Should return false
        assertFalse(result);
        verify(productDao).selectByBarcode(barcode);
    }
} 