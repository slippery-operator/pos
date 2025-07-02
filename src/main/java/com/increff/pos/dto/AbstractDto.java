package com.increff.pos.dto;

import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Validator;

/**
 * Abstract base class for all DTO classes providing common validation functionality
 */
public abstract class AbstractDto {

    @Autowired
    protected ValidationUtil validationUtil;

    /**
     * Generic form validation method that can be overridden by subclasses
     * @param form The form object to validate
     * @param <T> The type of form being validated
     */
    protected <T> void validateForm(T form) {
        validationUtil.validateForm(form);
    }

    /**
     * Validates ID parameters (non-null and positive)
     * @param id The ID to validate
     * @param fieldName The name of the field for error reporting
     */
    protected void validateId(Integer id, String fieldName) {
        validationUtil.validateId(id, fieldName);
    }

    /**
     * Validates search parameters
     * @param searchTerm The search term to validate
     * @param fieldName The name of the field for error reporting
     */
    protected void validateSearchTerm(String searchTerm, String fieldName) {
        validationUtil.validateSearchTerm(searchTerm, fieldName);
    }

    /**
     * Validates file uploads
     * @param file The file to validate
     * @param expectedExtension The expected file extension (e.g., ".tsv")
     */
    protected void validateFile(org.springframework.web.multipart.MultipartFile file, String expectedExtension) {
        validationUtil.validateFile(file, expectedExtension);
    }
}
