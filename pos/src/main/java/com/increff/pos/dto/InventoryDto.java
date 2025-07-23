package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.response.UploadResponse;
import com.increff.pos.model.response.ValidationError;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.util.TsvResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class InventoryDto extends AbstractDto<InventoryForm> {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ConvertUtil convertUtil;

    @Autowired
    private InventoryFlow flow;

    public List<InventoryResponse> searchInventory(String productName, int page, int size) {
        List<InventoryPojo> inventoryPojos = inventoryApi.searchInventory(toLowerCase(productName), page, size);
        return convertUtil.convertList(inventoryPojos, InventoryResponse.class);
    }

    public ResponseEntity<UploadResponse> uploadInventory(MultipartFile file) {
        validationUtil.validateTsvFile(file);
        List<InventoryFormWithRow> allForms = TsvParserUtil.parseInventoryTsvWithRow(file);
        List<ValidationError> allErrors = new ArrayList<>();
        allErrors.addAll(validateInventoryForms(allForms));

        List<InventoryFormWithRow> validForms = filterValidForms(allForms, allErrors);
        Map<Integer, ValidationError> flowErrors = flow.validateInventoryUpload(validForms);
        allErrors.addAll(flowErrors.values());

        List<InventoryFormWithRow> finalValidForms = filterValidForms(validForms, allErrors);
        if (!finalValidForms.isEmpty() && allErrors.isEmpty()) {
            flow.processTsvUpload(finalValidForms);
        }

        UploadResponse response = TsvResponseUtil.createInventoryUploadResponse(allForms, allErrors);
        return ResponseEntity.ok(response);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryUpdateForm inventoryUpdateForm) {
        validateId(productId, "product Id");
        validateForm(inventoryUpdateForm);
        InventoryPojo updated = inventoryApi.updateInventoryByProductId(productId, inventoryUpdateForm.getQuantity());
        return convertUtil.convert(updated, InventoryResponse.class);
    }

    private List<ValidationError> validateInventoryForms(List<InventoryFormWithRow> inventoryFormsWithRow) {
        return validationUtil.validateInventoryFormsWithRow(inventoryFormsWithRow);
    }

    private List<InventoryFormWithRow> filterValidForms(List<InventoryFormWithRow> allForms, List<ValidationError> allErrors) {
        Set<Integer> invalidRows = allErrors.stream()
                .map(ValidationError::getRowNumber)
                .collect(Collectors.toSet());

        return allForms.stream()
                .filter(form -> !invalidRows.contains(form.getRowNumber()))
                .collect(Collectors.toList());
    }

//    private Map<Integer, Integer> mapProductIdsToRowNumbers(List<InventoryFormWithRow> validForms) {
//        Map<Integer, Integer> productRowMap = new HashMap<>();
//        for (InventoryFormWithRow form : validForms) {
//            productRowMap.put(form.getForm().getProductId(), form.getRowNumber());
//        }
//        return productRowMap;
//    }

//    private List<ValidationError> validateInventoryWithoutSaving(Map<Integer, Integer> productRowMap) {
//        if (productRowMap.isEmpty()) return Collections.emptyList();
//
//        return inventoryApi.validateInventoryWithoutSaving(productRowMap).entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .map(Map.Entry::getValue)
//                .collect(Collectors.toList());
//    }
//
//    private List<InventoryPojo> convertToInventoryPojos(List<InventoryFormWithRow> validForms) {
//        return validForms.stream()
//                .map(form -> convertUtil.convert(form.getForm(), InventoryPojo.class))
//                .collect(Collectors.toList());
//    }
}