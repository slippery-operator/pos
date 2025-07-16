package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
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
        validateClientExists(productPojo.getClientId());
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
        Set<Integer> clientIds = productFormsWithRow.stream()
                .map(form -> form.getForm().getClientId())
                .collect(Collectors.toSet());
        Set<String> barcodes = productFormsWithRow.stream()
                .map(form -> form.getForm().getBarcode())
                .collect(Collectors.toSet());
        Map<Integer, Boolean> clientValidation = clientApi.validateClientsExistBatch(clientIds);
        Map<String, Boolean> barcodeValidation = productApi.validateBarcodesUniquenessBatch(barcodes);
        Map<Integer, ValidationError> errorByRow = constructErrorByRow(productFormsWithRow, clientValidation, barcodeValidation);
        return errorByRow;
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
                errorByRow.put(row, new ValidationError(row, "barcode", "Barcode alr exists"));
            }
        }
        return errorByRow;
    }
    public void validateProductForUpdate(ProductPojo product) {
        // Validate client exists
        clientApi.getClientById(product.getClientId());
        // Validate barcode uniqueness (exclude current product ID)
        productApi.validateBarcodeUniqueness(product.getBarcode(), product.getId());
    }

    public void validateClientExists(Integer clientId) {
        clientApi.getClientById(clientId);
    }
}