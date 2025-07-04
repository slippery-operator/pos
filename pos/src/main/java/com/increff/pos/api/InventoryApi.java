package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.response.InventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryApi {

    @Autowired
    private InventoryDao inventoryDao;

    public List<InventoryResponse> searchInventory(Integer productId, Integer inventoryId) {
        List<InventoryPojo> inventories = inventoryDao.findByProductIdOrInventoryId(productId, inventoryId);
        return inventories.stream()
                .map(this::convertToInventoryData)
                .collect(Collectors.toList());
    }

    public InventoryResponse getInventoryById(Integer id) {
        InventoryPojo inventory = inventoryDao.selectById(id);
        if (inventory == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Inventory not found with id: " + id);
        }
        return convertToInventoryData(inventory);
    }

    public InventoryResponse createInventory(Integer productId, Integer quantity) {
        // Check if inventory already exists for this product
        InventoryPojo existingInventory = inventoryDao.selectByProductId(productId);
        if (existingInventory != null) {
            throw new ApiException(ApiException.ErrorType.RESOURCE_ALREADY_EXISTS, 
                "Inventory already exists for product id: " + productId);
        }

        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity != null ? quantity : 0);

        inventoryDao.insert(inventory);
        return convertToInventoryData(inventory);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, Integer quantity) {
        InventoryPojo existingInventory = inventoryDao.selectByProductId(productId);
        if (existingInventory == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Inventory not found for product id: " + productId);
        }

        existingInventory.setQuantity(quantity);
        inventoryDao.update(existingInventory);
        return convertToInventoryData(existingInventory);
    }

    public List<InventoryResponse> bulkCreateInventory(List<InventoryPojo> inventories) {
        inventoryDao.bulkInsert(inventories);

        return inventories.stream()
                .map(this::convertToInventoryData)
                .collect(Collectors.toList());
    }

    public void validateInventoryAvailability(Integer productId, Integer requiredQuantity) {
        InventoryPojo inventory = inventoryDao.selectByProductId(productId);
        if (inventory == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "No inventory found for product id: " + productId);
        }
        if (inventory.getQuantity() < requiredQuantity) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Insufficient inventory. Available: " + inventory.getQuantity() +
                    ", Required: " + requiredQuantity + " for product id: " + productId);
        }
    }

    public void reduceInventory(Integer productId, Integer quantity) {
        InventoryPojo inventory = inventoryDao.selectByProductId(productId);
        if (inventory == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "No inventory found for product id: " + productId);
        }

        int newQuantity = inventory.getQuantity() - quantity;
        if (newQuantity < 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Cannot reduce inventory below zero for product id: " + productId);
        }

        inventory.setQuantity(newQuantity);
        inventoryDao.update(inventory);
    }

    private InventoryResponse convertToInventoryData(InventoryPojo inventory) {
        InventoryResponse data = new InventoryResponse();
        data.setId(inventory.getId());
        data.setProductId(inventory.getProductId());
        data.setQuantity(inventory.getQuantity());
        data.setVersion(inventory.getVersion());
        data.setCreatedAt(inventory.getCreatedAt());
        data.setUpdatedAt(inventory.getUpdatedAt());
        return data;
    }
}
