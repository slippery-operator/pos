package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
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
            throw new RuntimeException("Inventory not found with id: " + id);
        }
        return convertToInventoryData(inventory);
    }

    public InventoryResponse createInventory(InventoryForm inventoryForm) {
        // Check if inventory already exists for this product
        InventoryPojo existingInventory = inventoryDao.selectByProductId(inventoryForm.getProductId());
        if (existingInventory != null) {
            throw new RuntimeException("Inventory already exists for product id: " + inventoryForm.getProductId());
        }

        InventoryPojo inventory = convertToInventory(inventoryForm);
        inventoryDao.insert(inventory);
        return convertToInventoryData(inventory);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryForm inventoryForm) {
        InventoryPojo existingInventory = inventoryDao.selectByProductId(productId);
        if (existingInventory == null) {
            throw new RuntimeException("Inventory not found for product id: " + productId);
        }

        existingInventory.setQuantity(inventoryForm.getQuantity());
        inventoryDao.update(existingInventory);
        return convertToInventoryData(existingInventory);
    }

    public List<InventoryResponse> bulkCreateInventory(List<InventoryForm> inventoryForms) {
        List<InventoryPojo> inventories = inventoryForms.stream()
                .map(this::convertToInventory)
                .collect(Collectors.toList());

        inventoryDao.bulkInsert(inventories);

        return inventories.stream()
                .map(this::convertToInventoryData)
                .collect(Collectors.toList());
    }

    private InventoryPojo convertToInventory(InventoryForm form) {
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(form.getProductId());
        inventory.setQuantity(form.getQuantity() != null ? form.getQuantity() : 0);
        return inventory;
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
