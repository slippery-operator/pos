package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.response.TsvUploadResponse;
import com.increff.pos.model.response.TsvValidationError;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class InventoryDto extends AbstractDto<InventoryForm> {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ConvertUtil convertUtil;

    public List<InventoryResponse> searchInventory(String productName) {
        List<InventoryPojo> inventoryPojos = inventoryApi.searchInventory(toLowerCase(productName));
        return convertUtil.convertList(inventoryPojos, InventoryResponse.class);
    }

    /**
     * Upload inventory from TSV file with all-or-nothing validation.
     * If any row has invalid product ID or other validation errors, the entire TSV file is rejected.
     * This simplifies the flow by removing complex partial success handling.
     * 
     * @param file The TSV file to upload
     * @return List of InventoryResponse containing all successfully updated inventory
     * @throws ApiException if any validation error occurs in any row
     */
    public List<InventoryResponse> uploadInventoryTsv(MultipartFile file) {
        // Step 1: Validate file format and structure
        validationUtil.validateTsvFile(file);
        
        // Step 2: Parse TSV file into forms
        List<InventoryFormWithRow> inventoryFormsWithRow = TsvParserUtil.parseInventoryTsvWithRow(file);
        
        // Step 3: Validate all forms - if any form is invalid, reject entire file
        List<TsvValidationError> formValidationErrors = validationUtil.validateInventoryFormsWithRow(inventoryFormsWithRow);
        if (!formValidationErrors.isEmpty()) {
            // Build error message from first validation error
            TsvValidationError firstError = formValidationErrors.get(0);
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "TSV file rejected - Row " + firstError.getRowNumber() + ": " + firstError.getErrorMessage());
        }
        
        // Step 4: Convert to simple InventoryForm list (no need for complex row tracking)
        List<InventoryForm> inventoryForms = new ArrayList<>();
        for (InventoryFormWithRow inventoryWithRow : inventoryFormsWithRow) {
            inventoryForms.add(inventoryWithRow.getForm());
        }
        
        // Step 5: Validate all product IDs exist - if any product is invalid, reject entire file
        List<Integer> productIds = inventoryForms.stream()
            .map(InventoryForm::getProductId)
            .distinct()
            .collect(java.util.stream.Collectors.toList());
        
        for (Integer productId : productIds) {
            try {
                // This will throw exception if product doesn't exist
                inventoryApi.validateProductExists(productId);
            } catch (ApiException e) {
                throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                    "TSV file rejected - Invalid product ID: " + productId);
            }
        }
        
        // Step 6: Check for duplicate product IDs within file
        Set<Integer> uniqueProductIds = new HashSet<>(productIds);
        if (uniqueProductIds.size() != productIds.size()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "TSV file rejected - Duplicate product IDs found in file");
        }
        
        // Step 7: All validations passed - update all inventory
        List<InventoryPojo> inventoryPojos = convertUtil.convertList(inventoryForms, InventoryPojo.class);
        List<InventoryPojo> updatedInventory = inventoryApi.bulkUpdateInventory(inventoryPojos);
        
        // Step 8: Return all successful inventory updates
        return convertUtil.convertList(updatedInventory, InventoryResponse.class);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryUpdateForm inventoryUpdateForm) {
        validateId(productId, "product Id");
        validateForm(inventoryUpdateForm);
        InventoryPojo updated = inventoryApi.updateInventoryByProductId(productId, inventoryUpdateForm.getQuantity());
        return convertUtil.convert(updated, InventoryResponse.class);
    }
}