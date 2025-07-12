package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.response.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public List<InventoryPojo> searchInventory(String productName, int page, int size) {
        return inventoryDao.findByProductNameLike(productName, page, size);
    }

    public InventoryPojo createInventory(Integer productId, Integer quantity) {
        // Check if inventory already exists for this product
        InventoryPojo existingInventory = inventoryDao.selectByProductId(productId);
        if (existingInventory != null) {
            throw new ApiException(ErrorType.CONFLICT, "Inventory already exists for product id: " + productId);
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
            throw new ApiException(ErrorType.NOT_FOUND, "Inventory not found for product");
        }
        existingInventory.setQuantity(quantity);
        return existingInventory;
    }

    public void validateInventoryAvailability(Integer productId, Integer requiredQuantity) {
        InventoryPojo inventory = inventoryDao.selectByProductId(productId);
        if (inventory == null) {
            throw new ApiException(ErrorType.BAD_REQUEST, "No inventory found for product");
        }
        if (inventory.getQuantity() < requiredQuantity) {
            throw new ApiException(ErrorType.BAD_REQUEST, "Insufficient inventory");
        }
    }

    public void reduceInventory(Integer productId, Integer orderQuantity) {
        InventoryPojo inventory = inventoryDao.selectByProductId(productId);
        int newQuantity = inventory.getQuantity() - orderQuantity;
        inventory.setQuantity(newQuantity);
    }

    // TODO: check redundance in bulk functions

    public List<InventoryPojo> bulkCreateInventory(List<Integer> productIds) {
        inventoryDao.bulkInsert(productIds);
        return inventoryDao.selectByProductIds(productIds);
    }

    public List<InventoryPojo> bulkCreateOrUpdateInventory(List<InventoryPojo> inventoryList) {
        inventoryDao.bulkUpsert(inventoryList);
        return inventoryDao.selectByProductIds(inventoryList.stream()
                .map(inventory -> inventory.getProductId())
                .collect(Collectors.toList())
        );
    }

    public Map<Integer, ValidationError> validateInventoryWithoutSaving(Map<Integer, Integer> rowByProductId) {
        List<Integer> productIds = new ArrayList<>(rowByProductId.keySet());

        Map<Integer, Boolean> productValidation = inventoryDao.validateProductsExist(productIds);

        Map<Integer, ValidationError> errorByRow = new HashMap<>();
        rowByProductId.forEach((productId, row) -> {
            if(!productValidation.getOrDefault(productId, false)) {
                errorByRow.put(row, new ValidationError(
                        row, "productId", "Product not found"
                ));
            }
        });
        return errorByRow;
    }

//    private Map<Integer, Boolean> validateProductsExistBatch(List<Integer> productIds, boolean throwOnFailure) {
//        return inventoryDao.validateProductsExist(productIds);
//    }
}
