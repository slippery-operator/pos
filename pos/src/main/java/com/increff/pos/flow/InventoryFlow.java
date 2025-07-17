package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.response.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryFlow {

    @Autowired
    private ProductApi productApi;
    @Autowired
    private InventoryApi inventoryApi;

    public Map<Integer, ValidationError> validateInventoryUpload(List<InventoryFormWithRow> formsWithRow) {
        Map<Integer, ValidationError> errorByRow = new HashMap<>();
        // First validate duplicate barcodes in the file
        Map<Integer, ValidationError> duplicateErrors = validateDuplicateBarcodesInFile(formsWithRow);
        errorByRow.putAll(duplicateErrors);
        // Filter out forms with duplicate errors for database validation
        List<InventoryFormWithRow> formsWithoutDuplicates = filterFormsWithoutDuplicates(formsWithRow, duplicateErrors);
        // Validate against database
        Map<Integer, ValidationError> dbErrors = validateAgainstDatabase(formsWithoutDuplicates);
        errorByRow.putAll(dbErrors);
        return errorByRow;
    }

    private Map<Integer, ValidationError> validateDuplicateBarcodesInFile(List<InventoryFormWithRow> inventoryForms) {
        Map<String, List<InventoryFormWithRow>> barcodeGroups = inventoryForms.stream()
                .collect(Collectors.groupingBy(
                        inventoryForm -> inventoryForm.getForm().getBarcode()
                ));
        Map<Integer, ValidationError> duplicateErrors = new HashMap<>();
        for (Map.Entry<String, List<InventoryFormWithRow>> entry : barcodeGroups.entrySet()) {
            String barcode = entry.getKey();
            List<InventoryFormWithRow> formsWithSameBarcode = entry.getValue();
            if (formsWithSameBarcode.size() > 1) {
                addDuplicateErrorsForBarcode(duplicateErrors, barcode, formsWithSameBarcode);
            }
        }
        return duplicateErrors;
    }

    private List<InventoryFormWithRow> filterFormsWithoutDuplicates(List<InventoryFormWithRow> inventoryForms, Map<Integer, ValidationError> duplicateErrors) {
        return inventoryForms.stream()
                .filter(form -> !duplicateErrors.containsKey(form.getRowNumber()))
                .collect(Collectors.toList());
    }

    private void addDuplicateErrorsForBarcode(Map<Integer, ValidationError> duplicateErrors, String barcode,
                                              List<InventoryFormWithRow> formsWithSameBarcode) {
        for (InventoryFormWithRow form : formsWithSameBarcode) {
            ValidationError error = new ValidationError(form.getRowNumber(), "barcode", "Barcode is multiple times in your file");
            duplicateErrors.put(form.getRowNumber(), error);
        }
    }

    private Map<Integer, ValidationError> validateAgainstDatabase(List<InventoryFormWithRow> inventoryForms) {
        if (inventoryForms.isEmpty()) {
            return new HashMap<>();
        }
        Set<String> barcodes = inventoryForms.stream()
                .map(form -> form.getForm().getBarcode())
                .collect(Collectors.toSet());
        Map<String, Boolean> barcodeValidation = validateBarcodesExistBatch(barcodes);
        return constructErrorByRow(inventoryForms, barcodeValidation);
    }
    private Map<String, Boolean> validateBarcodesExistBatch(Set<String> barcodes) {
        Map<String, Integer> existingBarcodes = productApi.findProductsByBarcodes(new ArrayList<>(barcodes));
        return barcodes.stream()
                .collect(Collectors.toMap(
                        barcode -> barcode,
                        barcode -> existingBarcodes.containsKey(barcode)
                ));
    }

    private Map<Integer, ValidationError> constructErrorByRow(List<InventoryFormWithRow> inventoryFormsWithRow,
                                                              Map<String, Boolean> barcodeValidation) {
        Map<Integer, ValidationError> errorByRow = new HashMap<>();
        for (InventoryFormWithRow form : inventoryFormsWithRow) {
            Integer row = form.getRowNumber();
            String barcode = form.getForm().getBarcode();

            if (!barcodeValidation.getOrDefault(barcode, false)) {
                errorByRow.put(row, new ValidationError(row, "barcode",
                        "Product with barcode '" + barcode + "' does not exist"
                ));
            }
        }
        return errorByRow;
    }

    public List<InventoryPojo> processTsvUpload(List<InventoryFormWithRow> validForms) {
        // Get barcode to product ID mapping
        List<String> barcodes = validForms.stream()
                .map(form -> form.getForm().getBarcode())
                .collect(Collectors.toList());
        Map<String, Integer> barcodeToProductIdMap = productApi.findProductsByBarcodes(barcodes);
        // Convert to InventoryPojo with product IDs
        List<InventoryPojo> inventoryPojos = validForms.stream()
                .map(formWithRow -> {
                    InventoryForm form = formWithRow.getForm();
                    Integer productId = barcodeToProductIdMap.get(form.getBarcode());
                    InventoryPojo pojo = new InventoryPojo(productId, form.getQuantity());
                    return pojo;
                })
                .collect(Collectors.toList());
        return inventoryApi.bulkCreateOrUpdateInventory(inventoryPojos);
    }
}
