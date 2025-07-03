package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for validation operations across the application.
 * This class provides centralized validation logic and uses the new ApiException
 * with ErrorType enum for consistent error handling.
 */
@Component
public class ValidationUtil {

    @Autowired
    private Validator validator;

    /**
     * Generic form validation using Bean Validation.
     * Validates a single form object and throws ApiException with VALIDATION_ERROR
     * if validation fails.
     * 
     * @param form The form object to validate
     * @param <T> The type of the form object
     * @throws ApiException with VALIDATION_ERROR type if validation fails
     */
    public <T> void validateForm(T form) {
        Set<ConstraintViolation<T>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Validation failed: " + errorMessage);
        }
    }

    /**
     * Validate list of forms.
     * Checks if the list is not null/empty and validates each form individually.
     * 
     * @param forms The list of forms to validate
     * @param <T> The type of the form objects
     * @throws ApiException with VALIDATION_ERROR type if validation fails
     */
    public <T> void validateForms(List<T> forms) {
        if (forms == null || forms.isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Form list cannot be empty");
        }

        for (T form : forms) {
            validateForm(form);
        }
    }

    /**
     * Validate ID parameters.
     * Ensures the ID is not null and greater than 0.
     * 
     * @param id The ID to validate
     * @param fieldName The name of the field for error message
     * @throws ApiException with VALIDATION_ERROR type if ID is invalid
     */
    public void validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Invalid " + fieldName + ": " + id);
        }
    }

    /**
     * Validate search name parameter.
     * Ensures the search name is not null or empty.
     * 
     * @param name The search name to validate
     * @throws ApiException with VALIDATION_ERROR type if name is invalid
     */
    public void validateSearchName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Search name cannot be empty");
        }
    }

    /**
     * Validate TSV file.
     * Ensures the file is not null/empty and has the correct .tsv extension.
     * 
     * @param file The TSV file to validate
     * @throws ApiException with VALIDATION_ERROR type if file is invalid
     */
    public void validateTsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "TSV file is required");
        }
        if (!file.getOriginalFilename().endsWith(".tsv")) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "File must be in TSV format");
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
}