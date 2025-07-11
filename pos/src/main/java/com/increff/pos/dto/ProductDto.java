package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.response.TsvUploadResponse;
import com.increff.pos.model.response.TsvValidationError;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.model.form.ProductUpdateForm;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.util.ResponseUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static com.increff.pos.util.StringUtil.toLowerCase;
import com.increff.pos.exception.ApiException;

@Service
public class ProductDto extends AbstractDto<ProductForm> {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ConvertUtil convertUtil;

    public List<ProductResponse> searchProducts(ProductSearchForm searchRequest) {
        List<ProductPojo> products = productApi.searchProducts(toLowerCase(searchRequest.getBarcode()), toLowerCase(searchRequest.getProductName()));
        return convertUtil.convertList(products, ProductResponse.class);
    }

    public ProductResponse createProduct(ProductForm productForm) {
        validateForm(productForm);
        ProductPojo productPojo = convertUtil.convert(productForm, ProductPojo.class);
        ProductPojo createdProduct = productFlow.validateAndCreateProduct(productPojo);
        return convertUtil.convert(createdProduct, ProductResponse.class);
    }

    /**
     * Upload products from TSV file with all-or-nothing validation.
     * If any row has invalid client ID or other validation errors, the entire TSV file is rejected.
     * This simplifies the flow by removing complex partial success handling.
     * 
     * @param file The TSV file to upload
     * @return List of ProductResponse containing all successfully created products
     * @throws ApiException if any validation error occurs in any row
     */
    public List<ProductResponse> uploadProductsTsv(MultipartFile file) {
        // Step 1: Validate file format and structure
        validationUtil.validateTsvFile(file);
        
        // Step 2: Parse TSV file into forms
        List<ProductFormWithRow> productFormsWithRow = TsvParserUtil.parseProductTsv(file);
        
        // Step 3: Validate all forms - if any form is invalid, reject entire file
        List<TsvValidationError> formValidationErrors = validationUtil.validateFormsWithRow(productFormsWithRow);
        if (!formValidationErrors.isEmpty()) {
            // Build error message from first validation error
            TsvValidationError firstError = formValidationErrors.get(0);
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "TSV file rejected - Row " + firstError.getRowNumber() + ": " + firstError.getErrorMessage());
        }
        
        // Step 4: Convert to simple ProductForm list (no need for complex row tracking)
        List<ProductForm> productForms = new ArrayList<>();
        for (ProductFormWithRow productWithRow : productFormsWithRow) {
            productForms.add(productWithRow.getForm());
        }
        
        // Step 5: Validate all clients exist - if any client is invalid, reject entire file
        List<Integer> clientIds = productForms.stream()
            .map(ProductForm::getClientId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        for (Integer clientId : clientIds) {
            try {
                // This will throw exception if client doesn't exist
                productFlow.validateClientExists(clientId);
            } catch (ApiException e) {
                throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                    "TSV file rejected - Invalid client ID: " + clientId);
            }
        }
        
        // Step 6: Validate all barcodes are unique - if any barcode is duplicate, reject entire file
        List<String> barcodes = productForms.stream()
            .map(ProductForm::getBarcode)
            .collect(java.util.stream.Collectors.toList());
        
        // Check for duplicates within file
        Set<String> uniqueBarcodes = new HashSet<>(barcodes);
        if (uniqueBarcodes.size() != barcodes.size()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "TSV file rejected - Duplicate barcodes found in file");
        }
        
        // Check for duplicates with existing database records
        for (String barcode : barcodes) {
            try {
                productApi.validateBarcodeUniqueness(barcode, null);
            } catch (ApiException e) {
                throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                    "TSV file rejected - Barcode already exists: " + barcode);
            }
        }
        
        // Step 7: All validations passed - create all products
        List<ProductPojo> productPojos = convertUtil.convertList(productForms, ProductPojo.class);
        List<ProductPojo> createdProducts = productFlow.createProductsWithInventory(productPojos);
        
        // Step 8: Return all successful products
        return convertUtil.convertList(createdProducts, ProductResponse.class);
    }

    /**
     * Updates a product using the ProductUpdateForm.
     * Only allows updating name, mrp, and imageUrl fields.
     * Barcode and clientId cannot be changed after product creation.
     */
    public ProductResponse updateProduct(Integer id, ProductUpdateForm productUpdateForm) {
        validateUpdateInput(id, productUpdateForm);

        ProductPojo product = productApi.updateProduct(id, productUpdateForm.getName(), productUpdateForm.getMrp(),
                productUpdateForm.getImageUrl());
        return convertUtil.convert(product, ProductResponse.class);
    }

    private void validateUpdateInput(Integer id, ProductUpdateForm productUpdateForm) {
        validateId(id, "Product id");
        validateForm(productUpdateForm);
    }
}