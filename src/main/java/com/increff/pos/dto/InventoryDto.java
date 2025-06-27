package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class InventoryDto {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private InventoryFlow inventoryFlow;

    public List<InventoryResponse> searchInventory(Integer productId, Integer inventoryId) {
//        validateSearchParams(productId, inventoryId);
        return inventoryApi.searchInventory(productId, inventoryId);
    }

    public List<InventoryResponse> uploadInventoryTsv(MultipartFile file) {
        validateTsvFile(file);
        return inventoryFlow.processInventoryTsvUpload(file);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryUpdateForm inventoryUpdateForm) {
        validateInventoryUpdateForm(inventoryUpdateForm);

        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setQuantity(inventoryUpdateForm.getQuantity());
        return inventoryApi.updateInventoryByProductId(productId, inventoryForm);
    }

    private void validateSearchParams(Integer productId, Integer inventoryId) {
        if (productId == null && inventoryId == null) {
            throw new RuntimeException("Either product ID or inventory ID must be provided");
        }
    }

    private void validateInventoryForm(InventoryForm form) {
        if (form == null) {
            throw new RuntimeException("Inventory form cannot be null");
        }
        if (form.getQuantity() == null || form.getQuantity() < 0) {
            throw new RuntimeException("Quantity must be greater than or equal to 0");
        }
    }

    private void validateInventoryUpdateForm(InventoryUpdateForm form) {
        if (form == null) {
            throw new RuntimeException("Inventory form cannot be null");
        }
        if (form.getQuantity() == null || form.getQuantity() < 0) {
            throw new RuntimeException("Quantity must be greater than or equal to 0");
        }
    }

    private void validateTsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("TSV file is required");
        }
        if (!file.getOriginalFilename().endsWith(".tsv")) {
            throw new RuntimeException("File must be in TSV format");
        }
    }
}