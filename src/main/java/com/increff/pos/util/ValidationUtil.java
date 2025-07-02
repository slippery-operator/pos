package com.increff.pos.util;

import com.increff.pos.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Centralized validation utility for all form validations across the application
 */
@Component
public class ValidationUtil {

    @Autowired
    private Validator validator;

    /**
     * Validates any form object using JSR-303 annotations
     * @param form The form object to validate
     * @param <T> The type of form being validated
     * @throws ValidationException if validation fails
     */
    public <T> void validateForm(T form) {
        Set<ConstraintViolation<T>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            throw new ValidationException("Validation failed: " + errorMessage);
        }
    }

    /**
     * Validates ID parameters (non-null and positive)
     * @param id The ID to validate
     * @param fieldName The name of the field for error reporting
     * @throws ValidationException if validation fails
     */
    public void validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid " + fieldName + ": " + id);
        }
    }

    /**
     * Validates search terms (non-null and non-empty)
     * @param searchTerm The search term to validate
     * @param fieldName The name of the field for error reporting
     * @throws ValidationException if validation fails
     */
    public void validateSearchTerm(String searchTerm, String fieldName) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validates file uploads
     * @param file The file to validate
     * @param expectedExtension The expected file extension
     * @throws ValidationException if validation fails
     */
    public void validateFile(MultipartFile file, String expectedExtension) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is required");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(expectedExtension.toLowerCase())) {
            throw new ValidationException("File must be in " + expectedExtension.toUpperCase() + " format");
        }
    }

    /**
     * Validates a list of items (non-null and non-empty)
     * @param items The list to validate
     * @param fieldName The name of the field for error reporting
     * @throws ValidationException if validation fails
     */
    public void validateList(java.util.List<?> items, String fieldName) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
}
