package com.increff.pos.controller;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
// invocie_path -> null
/**
 * Global exception handler for REST controllers.
 * This class centralizes error handling and provides consistent error responses.
 * 
 * The new approach uses ApiException with ErrorType enum to:
 * 1. Reduce redundant code by having a single exception handler method
 * 2. Provide consistent error responses across the application
 * 3. Make error handling more maintainable and type-safe
 * 4. Eliminate the need for multiple specific exception classes
 */
@RestControllerAdvice
public class AppRestControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(AppRestControllerAdvice.class);

    /**
     * Handles ApiException with ErrorType enum.
     * This is the main exception handler that processes all application-specific exceptions.
     * The ErrorType enum provides the HTTP status code and error code automatically.
     * 
     * @param ex The ApiException with ErrorType
     * @param request The web request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        logger.warn("API Exception: {} - {}", ex.getErrorType(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * Handles validation exceptions from form validation.
     * Extracts the first validation error and creates a standardized error response.
     * 
     * @param ex The validation exception
     * @param request The web request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, WebRequest request) {

        logger.warn("Validation error: {}", ex.getMessage());

        // Extract first error message from BindingResult
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorType.BAD_REQUEST.getErrorCode(),
                errorMessage,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(ErrorType.BAD_REQUEST.getHttpStatus()).body(errorResponse);
    }

    /**
     * Handles file upload size exceeded exceptions.
     * Provides a user-friendly message for file size violations.
     * 
     * @param ex The file size exceeded exception
     * @param request The web request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorType.FILE_SIZE_EXCEEDED.getErrorCode(),
                "File size exceeds the maximum allowed limit",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(ErrorType.FILE_SIZE_EXCEEDED.getHttpStatus()).body(errorResponse);
    }

    /**
     * Generic exception handler for any uncaught exceptions.
     * This is a fallback handler that catches all other exceptions.
     * 
     * @param ex The generic exception
     * @param request The web request
     * @return ResponseEntity with ErrorResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, WebRequest request) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                ErrorType.INTERNAL_SERVER_ERROR.getErrorCode(),
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(ErrorType.INTERNAL_SERVER_ERROR.getHttpStatus()).body(errorResponse);
    }
}
