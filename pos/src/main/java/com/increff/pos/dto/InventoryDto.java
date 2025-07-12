package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.response.ValidationError;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.util.TsvResponseUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class InventoryDto extends AbstractDto<InventoryForm> {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ConvertUtil convertUtil;

    public List<InventoryResponse> searchInventory(String productName, int page, int size) {
        List<InventoryPojo> inventoryPojos = inventoryApi.searchInventory(toLowerCase(productName), page, size);
        return convertUtil.convertList(inventoryPojos, InventoryResponse.class);
    }

//    TODO: ask about responsive entity returning
    public ResponseEntity<String> uploadInventory(MultipartFile file) {
        validationUtil.validateTsvFile(file);
        List<InventoryFormWithRow> inventoryFormsWithRow = TsvParserUtil.parseInventoryTsvWithRow(file);
        
        // Validate all forms and collect errors
        List<ValidationError> allErrors = new ArrayList<>();
        allErrors.addAll(validationUtil.validateInventoryFormsWithRow(inventoryFormsWithRow));

        Set<Integer> invalidRowInfo = allErrors.stream()
                .map(ValidationError::getRowNumber)
                .collect(Collectors.toSet());
        // Get valid forms for API validation
        List<InventoryFormWithRow> validForms = inventoryFormsWithRow.stream()
                .filter(form -> !invalidRowInfo.contains(form.getRowNumber()))
                .collect(Collectors.toList());


        Map<Integer, Integer> validRowsByPrductId = new HashMap<>();
        for (InventoryFormWithRow validForm : validForms) {
            validRowsByPrductId.put(validForm.getForm().getProductId(), validForm.getRowNumber());
        }

        if(!validRowsByPrductId.isEmpty()) {
            Map<Integer, ValidationError> errorsByRowNum = inventoryApi.validateInventoryWithoutSaving(validRowsByPrductId);
            allErrors.addAll(errorsByRowNum.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));
        }

        if (allErrors.isEmpty()) {
            List<InventoryPojo> validatedInventory = validForms.stream()
                    .map(formWithRow ->
                        convertUtil.convert(formWithRow.getForm(), InventoryPojo.class)) // we shud just be using inventoryforms
                    .collect(Collectors.toList());
            inventoryApi.bulkCreateOrUpdateInventory(validatedInventory);
        }

        return TsvResponseUtil.generateInventoryTsvResponse(inventoryFormsWithRow, allErrors);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryUpdateForm inventoryUpdateForm) {
        validateId(productId, "product Id");
        validateForm(inventoryUpdateForm);
        InventoryPojo updated = inventoryApi.updateInventoryByProductId(productId, inventoryUpdateForm.getQuantity());
        return convertUtil.convert(updated, InventoryResponse.class);
    }
}