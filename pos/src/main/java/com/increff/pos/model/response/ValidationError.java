package com.increff.pos.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a validation error for a specific row in a TSV file upload.
 * Contains the row number and detailed error message.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    
    /**
     * The row number in the TSV file (1-based, excluding header)
     */
    private int rowNumber;
    
    /**
     * The field that caused the validation error
     */
    private String field;
    
    /**
     * Detailed error message
     */
    private String errorMessage;
    
    /**
     * The original data from the problematic row
     */
    private String originalData;

    /**
     * Constructor for field-specific errors
     */
    public ValidationError(int rowNumber, String field, String errorMessage) {
        this.rowNumber = rowNumber;
        this.field = field;
        this.errorMessage = errorMessage;
        this.originalData = "";
    }
    

} 