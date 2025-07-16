package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.response.ValidationError;

@Component
public class ValidationUtil {

    @Autowired
    private Validator validator;

    public <T> void validateForm(T form) {
        if (form == null) {
            throw new ApiException(ErrorType.BAD_REQUEST, "Form cannot be null");
        }
        
        Set<ConstraintViolation<T>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ApiException(ErrorType.BAD_REQUEST, "bad form failed: " + errorMessage);
        }
    }

    public <T> void validateForms(List<T> forms) {
        if (forms == null || forms.isEmpty()) {
            throw new ApiException(ErrorType.BAD_REQUEST, "Form list cannot be empty");
        }

        for (T form : forms) {
            validateForm(form);
        }
    }


    public void validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ApiException(ErrorType.BAD_REQUEST, "Invalid " + fieldName + ": " + id);
        }
    }


    /**
     * Validate TSV file.
     * Ensures the file is not null/empty, has the correct .tsv extension,
     * and does not exceed the maximum allowed number of rows (5000).
     * 
     * @param file The TSV file to validate
     * @throws ApiException with VALIDATION_ERROR type if file is invalid
     */
    public void validateTsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ErrorType.BAD_REQUEST, "TSV file is required");
        }
        if (!file.getOriginalFilename().endsWith(".tsv")) {
            throw new ApiException(ErrorType.BAD_REQUEST, "File must be in TSV format");
        }
        validateTsvRowCount(file);
    }
    
    /**
     * Validate the number of rows in a TSV file.
     * Counts the number of lines in the file and throws an exception if it exceeds 5000 rows.
     * This prevents processing of excessively large files that could impact performance.
     * 
     * @param file The TSV file to check for row count
     * @throws ApiException with VALIDATION_ERROR type if row count exceeds limit
     */
    private void validateTsvRowCount(MultipartFile file) {
        try {
            // Read the file content as string and split by newlines to count rows
            String content = new String(file.getBytes(), "UTF-8");
            String[] lines = content.split("\n");
            
            // Count non-empty lines (actual data rows)
            int rowCount = 0;
            for (String line : lines) {
                // Skip empty lines or lines with only whitespace
                if (line != null && !line.trim().isEmpty()) {
                    rowCount++;
                }
            }
            // Check if row count exceeds the maximum allowed limit
            final int MAX_ROWS = 5000;
            if (rowCount > MAX_ROWS) {
                throw new ApiException(ErrorType.BAD_REQUEST, "ROW LIMIT EXCEEDED.");
            }

        } catch (java.io.IOException e) {
            // Handle IOException specifically for file reading issues
            throw new ApiException(ErrorType.BAD_REQUEST, "Unable to read TSV file content");
        } catch (Exception e) {
            // If we can't read the file content, throw a generic error
            // This handles cases where the file might be corrupted or unreadable
            if (e instanceof ApiException) {
                throw e; // Re-throw ApiException as-is
            }
            throw new ApiException(ErrorType.BAD_REQUEST, "Unable to validate TSV file content");
        }
    }

    /**
     * Validate search parameters (can be null but if provided must be valid).
     * This method provides a placeholder for custom validation logic for search parameters.
     * Implementation depends on specific business rules.
     * 
     * @param params The search parameters to validate
     */
    public void validateSearchParams(Integer... params) {
        // Custom validation logic for search parameters
        // Implementation depends on specific business rules
        // Currently no validation is performed as parameters can be null
    }

    /**
     * Validates individual forms and returns validation errors with row information.
     *
     * @param productFormsWithRow List of product forms with row information
     * @return List of validation errors
     */
    public List<ValidationError> validateProductFormsWithRow(List<com.increff.pos.model.form.ProductFormWithRow> productFormsWithRow) {
        List<ValidationError> errors = new java.util.ArrayList<>();
        for (com.increff.pos.model.form.ProductFormWithRow productWithRow : productFormsWithRow) {
            try {
                validateForm(productWithRow.getForm());
            } catch (com.increff.pos.exception.ApiException e) {
                String field = com.increff.pos.util.StringUtil.extractFieldFromValidationError(e.getMessage());
                errors.add(new ValidationError(
                    productWithRow.getRowNumber(),
                    field,
                    e.getMessage(),
                    productWithRow.getOriginalData()
                ));
            }
        }
        return errors;
    }

    /**
     * Validates individual inventory forms and returns validation errors with row information.
     *
     * @param formsWithRow List of inventory forms with row information
     * @return List of validation errors
     */
    public List<ValidationError> validateInventoryFormsWithRow(List<InventoryFormWithRow> formsWithRow) {
        List<ValidationError> errors = new ArrayList<>();
        for (InventoryFormWithRow formWithRow : formsWithRow) {
            try {
                validateForm(formWithRow.getForm());
            } catch (Exception e) {
                errors.add(new ValidationError(
                    formWithRow.getRowNumber(),
                    "form",
                    e.getMessage(),
                    formWithRow.toString()
                ));
            }
        }
        return errors;
    }
}