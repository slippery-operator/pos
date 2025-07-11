package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.response.TsvUploadResponse;
import com.increff.pos.model.response.TsvValidationError;
import com.increff.pos.util.CollectionUtil;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.ResponseUtil;
import com.increff.pos.util.TsvValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
@Transactional
public class ProductFlow {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ConvertUtil convertUtil;

    public ProductPojo validateAndCreateProduct(ProductPojo productPojo) {
        productApi.validateBarcodeUniqueness(productPojo.getBarcode(), null);
        ProductPojo createdProduct = productApi.createProduct(productPojo);
        inventoryApi.createInventory(createdProduct.getId(), 0);
        return createdProduct;
    }

    /**
     * Validates that a client exists by ID.
     * This method is used for simplified TSV validation.
     * 
     * @param clientId The client ID to validate
     * @throws ApiException if client doesn't exist
     */
    public void validateClientExists(Integer clientId) {
        Map<Integer, Boolean> validation = clientApi.validateClientsExistBatch(java.util.Set.of(clientId));
        if (!validation.getOrDefault(clientId, false)) {
            throw new ApiException(ApiException.ErrorType.NOT_FOUND, "Client not found: " + clientId);
        }
    }

    /**
     * Creates multiple products with inventory using simplified all-or-nothing approach.
     * All products are created in a single transaction - if any fails, all fail.
     * 
     * @param productPojos List of products to create
     * @return List of created products
     */
    public List<ProductPojo> createProductsWithInventory(List<ProductPojo> productPojos) {
        // Create all products in bulk
        List<ProductPojo> createdProducts = productApi.bulkCreateProducts(productPojos);
        
        // Create inventory for all products
        List<Integer> productIds = createdProducts.stream()
                .map(ProductPojo::getId)
                .collect(Collectors.toList());
        inventoryApi.bulkCreateInventory(productIds);
        
        return createdProducts;
    }

    /**
     * Orchestrates TSV upload process by coordinating multiple APIs.
     * Only contains orchestration logic, no business logic.
     * 
     * @deprecated Use the simplified all-or-nothing approach instead
     */
    @Deprecated
    public ProductTsvUploadResult processProductTsvUpload(List<ProductFormWithRow> productFormsWithRow) {
        List<TsvValidationError> validationErrors = new ArrayList<>();

        // Step 1: Group products for efficient batch validation
        Map<Integer, List<ProductFormWithRow>> productsByClientId = CollectionUtil.groupBy(productFormsWithRow, 
            product -> product.getForm().getClientId());
        Map<String, List<ProductFormWithRow>> productsByBarcode = CollectionUtil.groupBy(productFormsWithRow, 
            product -> product.getForm().getBarcode());

        // Step 2: Validate clients using ClientApi
        Map<Integer, Boolean> clientValidation = clientApi.validateClientsExistBatch(productsByClientId.keySet());
        TsvValidationUtil.collectClientValidationErrors(productsByClientId, clientValidation, validationErrors);

        // Step 3: Validate barcodes using ProductApi
        Map<String, Boolean> barcodeValidation = productApi.validateBarcodesUniquenessBatch(productsByBarcode.keySet());
        TsvValidationUtil.collectBarcodeValidationErrors(productsByBarcode, barcodeValidation, validationErrors);

        // Step 4: Filter valid products and create them
        List<ProductPojo> validProducts = filterValidProducts(productFormsWithRow, clientValidation, barcodeValidation);
        List<ProductPojo> createdProducts = new ArrayList<>();

        if (!validProducts.isEmpty()) {
            createdProducts = productApi.bulkCreateProducts(validProducts);
            List<Integer> productIds = createdProducts.stream()
                    .map(ProductPojo::getId)
                    .collect(Collectors.toList());
            inventoryApi.bulkCreateInventory(productIds);
        }

        // Step 5: Build result object (no conversion to ProductResponse here)
        return new ProductTsvUploadResult(createdProducts, validationErrors);
    }

    private List<ProductPojo> filterValidProducts(
            List<ProductFormWithRow> productFormsWithRow,
            Map<Integer, Boolean> clientValidation,
            Map<String, Boolean> barcodeValidation) {

        List<ProductPojo> validProducts = new ArrayList<>();
        Map<String, Integer> barcodeCount = CollectionUtil.countBy(productFormsWithRow, 
            product -> product.getForm().getBarcode());

        for (ProductFormWithRow productWithRow : productFormsWithRow) {
            Integer clientId = productWithRow.getForm().getClientId();
            String barcode = productWithRow.getForm().getBarcode();

            boolean clientValid = clientValidation.getOrDefault(clientId, false);
            boolean barcodeUniqueInFile = barcodeCount.get(barcode) == 1;
            boolean barcodeUniqueInDb = barcodeValidation.getOrDefault(barcode, false);

            if (clientValid && barcodeUniqueInFile && barcodeUniqueInDb) {
                validProducts.add(convertUtil.convert(productWithRow.getForm(), ProductPojo.class));
            }
        }

        return validProducts;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductTsvUploadResult {
        private List<ProductPojo> successfulProducts;
        private List<TsvValidationError> validationErrors;
    }
}