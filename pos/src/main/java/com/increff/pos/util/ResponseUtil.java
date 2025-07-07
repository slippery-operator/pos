package com.increff.pos.util;

import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.response.TsvUploadResponse;
import com.increff.pos.model.response.TsvValidationError;

import java.util.List;

/**
 * Utility class for building response objects.
 * Provides methods for constructing standardized response objects across the application.
 */
public class ResponseUtil {

    /**
     * Builds a TsvUploadResponse with calculated statistics.
     * 
     * @param originalItems The original list of items that were processed
     * @param successfulItems The list of successfully processed items
     * @param validationErrors The list of validation errors encountered
     * @param <T> The type of successful items
     * @return TsvUploadResponse containing the results and statistics
     */
    public static <T> TsvUploadResponse<T> buildTsvUploadResponse(
            List<?> originalItems,
            List<T> successfulItems,
            List<TsvValidationError> validationErrors) {

        int totalRows = originalItems.size();
        int successfulRows = successfulItems.size();
        int failedRows = validationErrors.size();

        return new TsvUploadResponse<>(successfulItems, validationErrors, totalRows, successfulRows, failedRows);
    }

} 