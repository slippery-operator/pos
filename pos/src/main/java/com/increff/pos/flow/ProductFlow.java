package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.response.ValidationError;
import com.increff.pos.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Service
@Transactional
public class ProductFlow {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ConvertUtil convertUtil;

    public ProductPojo validateAndCreateProduct(ProductPojo productPojo) {
        // Validate client exists
        clientApi.getClientById(productPojo.getClientId());
        // Validate barcode uniqueness
        productApi.validateBarcodeUniqueness(productPojo.getBarcode(), null);
        // Create product and initialize inventory
        ProductPojo createdProduct = productApi.createProduct(productPojo);
        inventoryApi.createInventory(createdProduct.getId(), 0);
        return createdProduct;
    }

    public List<ProductPojo> processProductTsvUpload(List<ProductPojo> products) {
        List<ProductPojo> createdProducts = new ArrayList<>();
        if (!products.isEmpty()) {
            createdProducts = productApi.bulkCreateProducts(products);
            List<Integer> productIds = createdProducts.stream()
                    .map(ProductPojo::getId)
                    .collect(Collectors.toList());
            inventoryApi.bulkCreateInventory(productIds);
        }
        return createdProducts;
    }

    public Map<Integer, ValidationError> validateProductTsvUpload(List<ProductFormWithRow> productFormsWithRow) {
        Map<Integer, ValidationError> errorByRow = new HashMap<>();
        Map<Integer, ValidationError> duplicateErrors  = validateDuplicateBarcodesInFile(productFormsWithRow);
        errorByRow.putAll(duplicateErrors);
        List<ProductFormWithRow> productFormsWithoutDuplicates = filterFormsWithoutDuplicates(productFormsWithRow, duplicateErrors);
        Map<Integer, ValidationError> dbErrors = validateAgainstDatabase(productFormsWithoutDuplicates);
        errorByRow.putAll(dbErrors);
        return errorByRow;
    }

    private Map<Integer, ValidationError> validateDuplicateBarcodesInFile(List<ProductFormWithRow> productForms) {
        Map<String, List<ProductFormWithRow>> barcodeGroups = productForms.stream()
                .collect(Collectors.groupingBy(
                        productForm -> productForm.getForm().getBarcode()
                ));
        Map<Integer, ValidationError> duplicateErrors = new HashMap<>();
        for(Map.Entry<String, List<ProductFormWithRow>> entry: barcodeGroups.entrySet()) {
            String barcode = entry.getKey();
            List<ProductFormWithRow> formsWithSameBarcode = entry.getValue();
            if(formsWithSameBarcode.size() > 1) {
                addDuplicateErrorsForBarcode(duplicateErrors, barcode, formsWithSameBarcode);
            }
        }
        return duplicateErrors;
    }

    private void addDuplicateErrorsForBarcode(Map<Integer, ValidationError> duplicateErrors, String barcode,
                                              List<ProductFormWithRow> formsWithSameBarcode) {
        for (ProductFormWithRow form : formsWithSameBarcode) {
            ValidationError error = new ValidationError(form.getRowNumber(), "barcode", "Barcode is multiple times in your file");
            duplicateErrors.put(form.getRowNumber(), error);
        }
    }

    private List<ProductFormWithRow> filterFormsWithoutDuplicates(List<ProductFormWithRow> productForms, Map<Integer, ValidationError> duplicateErrors) {
        List<ProductFormWithRow> validForms = productForms.stream()
                .filter(p -> !duplicateErrors.containsKey(p.getRowNumber()))
                .collect(Collectors.toList());
        return validForms;
    }

    private Map<Integer, ValidationError> validateAgainstDatabase(List<ProductFormWithRow> productForms) {
        Set<Integer> clientIds = productForms.stream()
                .map(form -> form.getForm().getClientId())
                .collect(Collectors.toSet());
        Set<String> barcodes = productForms.stream()
                .map(form -> form.getForm().getBarcode())
                .collect(Collectors.toSet());
        Map<Integer, Boolean> clientValidation = clientApi.validateClientsExistBatch(clientIds);
        Map<String, Boolean> barcodeValidation = productApi.validateBarcodesUniquenessBatch(barcodes);
        return constructErrorByRow(productForms, clientValidation, barcodeValidation);
    }

    private Map<Integer, ValidationError> constructErrorByRow(List<ProductFormWithRow> productFormsWithRow,Map<Integer, Boolean> clientValidation,
                                     Map<String, Boolean> barcodeValidation) {
        Map<Integer, ValidationError> errorByRow = new HashMap<>();
        for (ProductFormWithRow form : productFormsWithRow) {
            Integer row = form.getRowNumber();
            Integer clientId = form.getForm().getClientId();
            String barcode = form.getForm().getBarcode();
            if (!clientValidation.getOrDefault(clientId, false)) {
                errorByRow.put(row, new ValidationError(row, "clientId", "Client not found"));
            } else if (!barcodeValidation.getOrDefault(barcode, false)) {
                errorByRow.put(row, new ValidationError(row, "barcode", "Barcode alr exists in db"));
            }
        }
        return errorByRow;
    }
}