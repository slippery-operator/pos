package com.increff.pos.util;

import com.increff.pos.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidationUtil {

    @Autowired
    private Validator validator;

    /**
     * Generic form validation using Bean Validation
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
     * Validate list of forms
     */
    public <T> void validateForms(List<T> forms) {
        if (forms == null || forms.isEmpty()) {
            throw new ValidationException("Form list cannot be empty");
        }

        for (T form : forms) {
            validateForm(form);
        }
    }

    /**
     * Validate ID parameters
     */
    public void validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid " + fieldName + ": " + id);
        }
    }

    /**
     * Validate search name parameter
     */
    public void validateSearchName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Search name cannot be empty");
        }
    }

    /**
     * Validate TSV file
     */
    public void validateTsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("TSV file is required");
        }
        if (!file.getOriginalFilename().endsWith(".tsv")) {
            throw new ValidationException("File must be in TSV format");
        }
    }

    /**
     * Validate search parameters (can be null but if provided must be valid)
     */
    public void validateSearchParams(Integer... params) {
        // Custom validation logic for search parameters
        // Implementation depends on specific business rules
    }
}