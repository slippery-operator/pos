package com.increff.pos.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Response class for TSV upload operations.
 * Contains information about successful uploads and validation errors.
 */
@Getter
@Setter
@NoArgsConstructor
public class TsvUploadResponse<T> {
    
    /**
     * List of successfully processed items
     */
    private List<T> successfulItems;
    
    /**
     * List of validation errors with row numbers and error details
     */
    private List<ValidationError> validationErrors;
    
    /**
     * Total number of rows processed
     */
    private int totalRows;

    
    /**
     * Number of successful rows
     */
    private int successfulRows;
    
    /**
     * Number of failed rows
     */
    private int failedRows;
    
    /**
     * Constructor to initialize the response with all required fields
     */
    public TsvUploadResponse(List<T> successfulItems, List<ValidationError> validationErrors,
                           int totalRows, int successfulRows, int failedRows) {
        this.successfulItems = successfulItems;
        this.validationErrors = validationErrors;
        this.totalRows = totalRows;
        this.successfulRows = successfulRows;
        this.failedRows = failedRows;
    }
} 