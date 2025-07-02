package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.exception.ValidationException;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.TsvParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
//TODO: extend Abstractdto and keep all convesions in convertUtil
public class InventoryDto {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private InventoryFlow inventoryFlow;

    //TODO: to create a separate validationUtil component, move all validation logic there
    @Autowired
    private Validator validator;

    public List<InventoryResponse> searchInventory(Integer productId, Integer inventoryId) {
        validateSearchParams(productId, inventoryId);
        return inventoryApi.searchInventory(productId, inventoryId);
    }

    public List<InventoryResponse> uploadInventoryTsv(MultipartFile file) {
        validateTsvFile(file);

        // Parse and validate TSV file to InventoryPojo list
        List<InventoryForm> inventoryForms = TsvParserUtil.parseInventoryTsv(file);

        // Validate each inventory form
        for (InventoryForm form : inventoryForms) {
            validateInventoryForm(form);
        }

        // Convert forms to POJOs with validation
        List<InventoryPojo> inventoryPojos = inventoryForms.stream()
                .map(this::convertFormToInventory)
                .collect(Collectors.toList());

        return inventoryFlow.processInventoryTsvUpload(inventoryPojos);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryUpdateForm inventoryUpdateForm) {
        validateInventoryUpdateForm(inventoryUpdateForm);
        return inventoryApi.updateInventoryByProductId(productId, inventoryUpdateForm.getQuantity());
    }

    private void validateSearchParams(Integer productId, Integer inventoryId) {
//        if (productId == null && inventoryId == null) {
//            throw new RuntimeException("Either product ID or inventory ID must be provided");
//        }
    }

    private void validateInventoryForm(InventoryForm form) {
        Set<ConstraintViolation<InventoryForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<InventoryForm> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new ValidationException("Validation failed: " + sb.toString());
        }
    }

    private void validateInventoryUpdateForm(InventoryUpdateForm form) {
        Set<ConstraintViolation<InventoryUpdateForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<InventoryUpdateForm> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new ValidationException("Validation failed: " + sb.toString());
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

    private InventoryPojo convertFormToInventory(InventoryForm form) {
        InventoryPojo inventory = new InventoryPojo();
        inventory.setProductId(form.getProductId());
        inventory.setQuantity(form.getQuantity() != null ? form.getQuantity() : 0);
        return inventory;
    }
}