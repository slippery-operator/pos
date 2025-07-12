package com.increff.pos.util;

import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.response.ValidationError;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for generating TSV response format with validation results.
 * Used for TSV upload endpoints to return validation status in TSV format.
 */
public class TsvResponseUtil {

    /**
     * Generates TSV response for inventory upload with validation results.
     * Format: productId, quantity, remarks
     * 
     * @param inventoryFormsWithRow List of inventory forms with row numbers
     * @param validationErrors List of validation errors
     * @return ResponseEntity with TSV content
     */
    public static ResponseEntity<String> generateInventoryTsvResponse(
            List<InventoryFormWithRow> inventoryFormsWithRow,
            List<ValidationError> validationErrors) {
        
        StringBuilder tsvContent = new StringBuilder();
        tsvContent.append("productId\tquantity\tremarks\n");
        
        // Create error map for quick lookup
        Map<Integer, String> errorMap = validationErrors.stream()
                .collect(Collectors.toMap(
                    ValidationError::getRowNumber,
                    ValidationError::getErrorMessage,
                    (existing, replacement) -> existing + "; " + replacement
                ));
        
        for (InventoryFormWithRow formWithRow : inventoryFormsWithRow) {
            String productId = formWithRow.getForm().getProductId() != null ? 
                formWithRow.getForm().getProductId().toString() : "invalid";
            String quantity = formWithRow.getForm().getQuantity() != null ? 
                formWithRow.getForm().getQuantity().toString() : "invalid";
            String remarks = errorMap.getOrDefault(formWithRow.getRowNumber(), "valid");
            
            tsvContent.append(productId).append("\t")
                      .append(quantity).append("\t")
                      .append(remarks).append("\n");
        }
        
        return createTsvResponse(tsvContent.toString(), "inventory_results.tsv");
    }
    
    /**
     * Generates TSV response for product upload with validation results.
     * Format: barcode, clientId, name, mrp, imageUrl, remarks
     * 
     * @param productFormsWithRow List of product forms with row numbers
     * @param validationErrors List of validation errors
     * @return ResponseEntity with TSV content
     */
    public static ResponseEntity<String> generateProductTsvResponse(
            List<ProductFormWithRow> productFormsWithRow,
            List<ValidationError> validationErrors) {
        
        StringBuilder tsvContent = new StringBuilder();
        tsvContent.append("barcode\tclientId\tname\tmrp\timageUrl\tremarks\n");
        
        // Create error map for quick lookup
        Map<Integer, String> errorMap = validationErrors.stream()
                .collect(Collectors.toMap(
                    ValidationError::getRowNumber,
                    ValidationError::getErrorMessage,
                    (existing, replacement) -> existing + "; " + replacement
                ));
        
        for (ProductFormWithRow formWithRow : productFormsWithRow) {
            String barcode = formWithRow.getForm().getBarcode() != null ? 
                formWithRow.getForm().getBarcode() : "invalid";
            String clientId = formWithRow.getForm().getClientId() != null ? 
                formWithRow.getForm().getClientId().toString() : "invalid";
            String name = formWithRow.getForm().getName() != null ? 
                formWithRow.getForm().getName() : "invalid";
            String mrp = formWithRow.getForm().getMrp() != null ? 
                formWithRow.getForm().getMrp().toString() : "invalid";
            String imageUrl = formWithRow.getForm().getImageUrl() != null ? 
                formWithRow.getForm().getImageUrl() : "";
            String remarks = errorMap.getOrDefault(formWithRow.getRowNumber(), "valid");
            
            tsvContent.append(barcode).append("\t")
                      .append(clientId).append("\t")
                      .append(name).append("\t")
                      .append(mrp).append("\t")
                      .append(imageUrl).append("\t")
                      .append(remarks).append("\n");
        }
        
        return createTsvResponse(tsvContent.toString(), "product_validation_results.tsv");
    }
    
    /**
     * Creates ResponseEntity with TSV content and appropriate headers.
     * 
     * @param tsvContent The TSV content as string
     * @param filename The filename for download
     * @return ResponseEntity with TSV content
     */
    private static ResponseEntity<String> createTsvResponse(String tsvContent, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(tsvContent);
    }
} 