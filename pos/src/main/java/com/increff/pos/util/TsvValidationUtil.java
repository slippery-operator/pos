package com.increff.pos.util;

import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.response.TsvValidationError;

import java.util.List;
import java.util.Map;

/**
 * Utility class for TSV validation operations.
 * Provides methods for collecting and managing validation errors during TSV processing.
 */
public class TsvValidationUtil {

    /**
     * Collects validation errors for client validation failures.
     * 
     * @param productsByClientId Map of client IDs to lists of products
     * @param clientValidation Map of client IDs to their validation status
     * @param validationErrors List to collect validation errors
     */
    public static void collectClientValidationErrors(
            Map<Integer, List<ProductFormWithRow>> productsByClientId,
            Map<Integer, Boolean> clientValidation,
            List<TsvValidationError> validationErrors) {

        for (Map.Entry<Integer, List<ProductFormWithRow>> entry : productsByClientId.entrySet()) {
            Integer clientId = entry.getKey();
            if (!clientValidation.getOrDefault(clientId, false)) {
                for (ProductFormWithRow product : entry.getValue()) {
                    validationErrors.add(new TsvValidationError(
                            product.getRowNumber(), "client_id", "Client not found", product.getOriginalData()
                    ));
                }
            }
        }
    }

    /**
     * Collects validation errors for barcode validation failures.
     * Checks for both duplicates within the file and conflicts with existing database records.
     * 
     * @param productsByBarcode Map of barcodes to lists of products
     * @param barcodeValidation Map of barcodes to their validation status
     * @param validationErrors List to collect validation errors
     */
    public static void collectBarcodeValidationErrors(
            Map<String, List<ProductFormWithRow>> productsByBarcode,
            Map<String, Boolean> barcodeValidation,
            List<TsvValidationError> validationErrors) {

        for (Map.Entry<String, List<ProductFormWithRow>> entry : productsByBarcode.entrySet()) {
            String barcode = entry.getKey();
            List<ProductFormWithRow> products = entry.getValue();

            // Check for duplicates within file
            if (products.size() > 1) {
                for (ProductFormWithRow product : products) {
                    validationErrors.add(new TsvValidationError(
                            product.getRowNumber(), "barcode",
                            "Duplicate barcode '" + barcode + "' found in upload file",
                            product.getOriginalData()
                    ));
                }
            }
            // Check against database
            else if (!barcodeValidation.getOrDefault(barcode, false)) {
                validationErrors.add(new TsvValidationError(
                        products.get(0).getRowNumber(), "barcode",
                        "Barcode '" + barcode + "' already exists in database",
                        products.get(0).getOriginalData()
                ));
            }
        }
    }
} 