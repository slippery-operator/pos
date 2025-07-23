package com.increff.pos.util;

import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.response.UploadResponse;
import com.increff.pos.model.response.ValidationError;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for generating TSV response format with validation results.
 * Used for TSV upload endpoints to return validation status in TSV format.
 */
public class TsvResponseUtil {

    public static String generateInventoryTsvResponse( List<InventoryFormWithRow> inventoryFormsWithRow,
            List<ValidationError> validationErrors) {
        if(validationErrors.isEmpty()) {
            return "";
        }
        StringBuilder tsvContent = new StringBuilder();
        tsvContent.append("barcode\tquantity\tvalidity\tremarks\n");
        // Create error map for quick lookup
        Map<Integer, String> errorMap = validationErrors.stream()
                .collect(Collectors.toMap(
                    ValidationError::getRowNumber,
                    ValidationError::getErrorMessage,
                    (existing, replacement) -> existing + "; " + replacement
                ));
        
        for (InventoryFormWithRow formWithRow : inventoryFormsWithRow) {
            InventoryForm form = formWithRow.getForm();
            String errorMessage = errorMap.get(formWithRow.getRowNumber());

            String validity = (errorMessage == null) ? "valid" : "invalid";
            String remarks = (errorMessage == null) ? "" : errorMessage;

            tsvContent.append(nullToString(form.getBarcode())).append("\t")
                    .append(nullToString(form.getQuantity())).append("\t")
                    .append(validity).append("\t")
                    .append(remarks).append("\n");
        }
        return tsvContent.toString();
    }

    public static String buildProductTsvContent( List<ProductFormWithRow> productFormsWithRow,
            List<ValidationError> validationErrors) {
        if(validationErrors.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("barcode\tclient_id\tname\tmrp\timageUrl\tvalidity\tremarks\n");
        Map<Integer, String> errorMap = validationErrors.stream()
                .collect(Collectors.toMap(
                        ValidationError::getRowNumber,
                        ValidationError::getErrorMessage,
                        (a, b) -> a + "; " + b // merge multiple errors for same row
                ));
        for (ProductFormWithRow row : productFormsWithRow) {
            ProductForm form = row.getForm();
            String errorMessage = errorMap.get(row.getRowNumber());
            String validity = (errorMessage == null) ? "valid" : "invalid";
            String remarks = (errorMessage == null) ? "" : errorMessage;
            sb.append(nullToString(form.getBarcode())).append("\t")
                    .append(nullToString(form.getClientId())).append("\t")
                    .append(nullToString(form.getName())).append("\t")
                    .append(nullToString(form.getMrp())).append("\t")
                    .append(nullToString(form.getImageUrl())).append("\t")
                    .append(validity).append("\t")
                    .append(remarks).append("\n");
        }
        return sb.toString();
    }

    private static String nullToString(Object val) {
        return val == null ? "" : val.toString();
    }

    public static UploadResponse createInventoryUploadResponse(List<InventoryFormWithRow> allForms, List<ValidationError> allErrors) {
        String tsvContent = allErrors.isEmpty() ? "" : generateInventoryTsvResponse(allForms, allErrors);
        String base64Tsv = tsvContent.isEmpty() ? "" : Base64.getEncoder().encodeToString(tsvContent.getBytes(StandardCharsets.UTF_8));
        UploadResponse response = new UploadResponse();
        response.setStatus(allErrors.isEmpty() ? "success" : "error");
        response.setTsvBase64(base64Tsv);
        response.setFilename("inventory_upload_results.tsv");
        return response;
    }

    public static UploadResponse createProductUploadResponse(List<ProductFormWithRow> allForms, List<ValidationError> allErrors) {
        String tsvContent = allErrors.isEmpty() ? "" : buildProductTsvContent(allForms, allErrors);
        String base64Tsv = tsvContent.isEmpty() ? "" : Base64.getEncoder().encodeToString(tsvContent.getBytes(StandardCharsets.UTF_8));
        UploadResponse response = new UploadResponse();
        response.setStatus(allErrors.isEmpty() ? "success" : "error");
        response.setTsvBase64(base64Tsv);
        response.setFilename("product_upload_results.tsv");
        return response;
    }
} 