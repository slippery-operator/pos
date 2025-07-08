package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.response.TsvValidationError;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
@Transactional
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public List<InventoryPojo> searchInventory(String productName) {
        return inventoryDao.findByProductNameLike(productName);
    }

    public InventoryPojo createInventory(Integer productId, Integer quantity) {
        // Check if inventory already exists for this product
        InventoryPojo existingInventory = inventoryDao.selectByProductId(productId);
        if (existingInventory != null) {
            throw new ApiException(ApiException.ErrorType.CONFLICT, 
                "Inventory already exists for product id: " + productId);
        }

        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity != null ? quantity : 0);

        inventoryDao.insert(inventory);
        return inventory;
    }

    public InventoryPojo updateInventoryByProductId(Integer productId, Integer quantity) {
        InventoryPojo existingInventory = inventoryDao.selectByProductId(productId);
        if (existingInventory == null) {
            throw new ApiException(ApiException.ErrorType.NOT_FOUND, 
                "Inventory not found for product id: " + productId);
        }
        existingInventory.setQuantity(quantity);
        return existingInventory;
    }

    public List<InventoryPojo> bulkCreateInventory(List<Integer> productIds) {
        return inventoryDao.bulkInsert(productIds);
    }

    public void validateInventoryAvailability(Integer productId, Integer requiredQuantity) {
        InventoryPojo inventory = inventoryDao.selectByProductId(productId);
        if (inventory == null) {
            throw new ApiException(ApiException.ErrorType.BAD_REQUEST, "No inventory found for product");
        }
        if (inventory.getQuantity() < requiredQuantity) {
            throw new ApiException(ApiException.ErrorType.BAD_REQUEST, "Insufficient inventory.");
        }
    }

    public void reduceInventory(Integer productId, Integer quantity) {
        InventoryPojo inventory = inventoryDao.selectByProductId(productId);
        int newQuantity = inventory.getQuantity() - quantity;
        inventory.setQuantity(newQuantity);
    }

    public List<InventoryPojo> bulkCreateOrUpdateInventory(List<InventoryPojo> inventoryList) {
        List<Integer> productIds = inventoryList.stream()
                .map(InventoryPojo::getProductId)
                .collect(Collectors.toList());
        validateProductsExistBatch(productIds);

        inventoryDao.bulkUpsert(inventoryList);
        // After upsert, fetch the updated records to return
        List<InventoryPojo> result = new ArrayList<>();
        for (InventoryPojo pojo : inventoryList) {
            InventoryPojo updated = inventoryDao.selectByProductId(pojo.getProductId());
            if (updated != null) {
                result.add(updated);
            }
        }
        return result;
    }

    public InventoryTsvUploadResult bulkCreateOrUpdateInventoryWithResult(List<InventoryPojo> inventoryList, List<InventoryFormWithRow> validForms) {
        List<InventoryPojo> successfulItems = new ArrayList<>();
        List<TsvValidationError> apiErrors = new ArrayList<>();

        // Extract product IDs and validate they exist
        List<Integer> productIds = inventoryList.stream()
                .map(InventoryPojo::getProductId)
                .collect(Collectors.toList());

        Map<Integer, Boolean> productValidation = validateProductsExistBatch(productIds, false);

        // Filter valid inventory items and collect validation errors
        List<InventoryPojo> validInventoryItems = new ArrayList<>();
        for (int i = 0; i < inventoryList.size(); i++) {
            InventoryPojo inventory = inventoryList.get(i);
            InventoryFormWithRow formWithRow = validForms.get(i);

            if (productValidation.getOrDefault(inventory.getProductId(), false)) {
                validInventoryItems.add(inventory);
            } else {
                apiErrors.add(new TsvValidationError(
                        formWithRow.getRowNumber(),
                        "productId",
                        "Product not found with id: " + inventory.getProductId(),
                        formWithRow.toString()
                ));
            }
        }

        // Process valid items
        if (!validInventoryItems.isEmpty()) {
            try {
                inventoryDao.bulkUpsert(validInventoryItems);
                successfulItems.addAll(validInventoryItems);
            } catch (Exception e) {
                // Fallback to individual processing
                for (InventoryPojo inventory : validInventoryItems) {
                    try {
                        inventoryDao.bulkUpsert(Collections.singletonList(inventory));
                        successfulItems.add(inventory);
                    } catch (Exception ex) {
                        InventoryFormWithRow correspondingForm = validForms.stream()
                                .filter(form -> form.getForm().getProductId().equals(inventory.getProductId()))
                                .findFirst()
                                .orElse(null);

                        if (correspondingForm != null) {
                            apiErrors.add(new TsvValidationError(
                                    correspondingForm.getRowNumber(),
                                    "productId",
                                    ex.getMessage(),
                                    correspondingForm.toString()
                            ));
                        }
                    }
                }
            }
        }

        return new InventoryTsvUploadResult(successfulItems, apiErrors);

    }
    private void validateProductsExistBatch(List<Integer> productIds) {
        Map<Integer, Boolean> validationResults = validateProductsExistBatch(productIds, true);

        List<Integer> invalidProductIds = validationResults.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!invalidProductIds.isEmpty()) {
            throw new ApiException(ApiException.ErrorType.BAD_REQUEST,
                    "Products not found with ids: " + invalidProductIds);
        }
    }

    /**
     * Validates product existence by checking if they can be referenced.
     * Uses inventory DAO to validate foreign key constraints.
     *
     * @param productIds List of product IDs to validate
     * @param throwOnFailure Whether to throw exception on validation failure
     * @return Map of product ID to validation status
     */
    private Map<Integer, Boolean> validateProductsExistBatch(List<Integer> productIds, boolean throwOnFailure) {
        return inventoryDao.validateProductsExist(productIds);
    }


    @Data
    @AllArgsConstructor
    public static class InventoryTsvUploadResult {
        private List<InventoryPojo> successfulItems;
        private List<TsvValidationError> apiErrors;
    }
}
