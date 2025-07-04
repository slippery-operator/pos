package com.increff.invoice.dto;

import com.increff.invoice.model.form.OrderRequest;
import com.increff.invoice.model.response.InvoiceResponse;
import com.increff.invoice.util.PdfGeneratorUtil;
import com.increff.invoice.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

/**
 * DTO layer for invoice generation in stateless invoice-app
 * Handles both external validation and business logic for PDF generation
 * 
 * Since this is a stateless service without persistence, the DTO layer
 * handles both external validation and business logic directly.
 * 
 * This follows the layering convention for stateless services:
 * - External validation (null checks, data type checks)
 * - Business logic (PDF generation, invoice number creation)
 * - Response formatting
 */
@Service
public class InvoiceDto {

    @Autowired
    private PdfGeneratorUtil pdfGeneratorUtil;

    /**
     * Generate invoice PDF and return as base64 string
     * Handles external validation and business logic for PDF generation
     * 
     * @param orderRequest Order request containing invoice data
     * @return InvoiceResponse with generated PDF data
     * @throws ApiException if validation fails or PDF generation fails
     */
    public InvoiceResponse generateInvoice(@Valid OrderRequest orderRequest) {
        // External validation: Check for null and basic structural validation
        validateOrderRequest(orderRequest);
        
        // Business validation: Check business rules and data integrity
        validateBusinessRules(orderRequest);
        
        try {
            // Business logic: Generate PDF and convert to base64
            String base64Pdf = pdfGeneratorUtil.generatePdfAsBase64(orderRequest);
            
            // Business logic: Generate invoice number
            String invoiceNumber = generateInvoiceNumber(orderRequest.getOrderId());
            
            return new InvoiceResponse(orderRequest.getOrderId(), invoiceNumber, base64Pdf);
            
        } catch (Exception e) {
            throw new ApiException(ApiException.ErrorType.INTERNAL_ERROR, 
                "Failed to generate invoice: " + e.getMessage());
        }
    }

    /**
     * External validation for order request
     * Performs basic structural validation (null checks, data type validation)
     */
    private void validateOrderRequest(OrderRequest orderRequest) {
        if (orderRequest == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order request cannot be null");
        }
        
        // Basic structural validation
        if (orderRequest.getOrderId() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order ID cannot be null");
        }
        if (orderRequest.getOrderTime() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order time cannot be null");
        }
        if (orderRequest.getClientName() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Client name cannot be null");
        }
        if (orderRequest.getOrderItems() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order items cannot be null");
        }
        if (orderRequest.getTotalRevenue() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Total revenue cannot be null");
        }
    }

    /**
     * Business validation for order request
     * Validates business rules and data integrity
     */
    private void validateBusinessRules(OrderRequest orderRequest) {
        if (orderRequest.getOrderId() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order ID must be positive");
        }
        if (orderRequest.getOrderTime().trim().isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order time cannot be empty");
        }
        if (orderRequest.getClientName().trim().isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Client name cannot be empty");
        }
        if (orderRequest.getOrderItems().isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order must have at least one item");
        }
        if (orderRequest.getTotalRevenue() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Total revenue must be positive");
        }
        
        // Validate order items
        for (OrderRequest.OrderItemRequest item : orderRequest.getOrderItems()) {
            validateOrderItem(item);
        }
    }

    /**
     * Business validation for order item
     * Validates individual order item data
     */
    private void validateOrderItem(OrderRequest.OrderItemRequest item) {
        if (item.getProductName() == null || item.getProductName().trim().isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Product name cannot be empty");
        }
        if (item.getBarcode() == null || item.getBarcode().trim().isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Barcode cannot be empty");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Quantity must be positive");
        }
        if (item.getMrp() == null || item.getMrp() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "MRP must be positive");
        }
        if (item.getTotalAmount() == null || item.getTotalAmount() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Total amount must be positive");
        }
        
        // Validate that total amount matches quantity * mrp
        double expectedTotal = item.getQuantity() * item.getMrp();
        if (Math.abs(item.getTotalAmount() - expectedTotal) > 0.01) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Total amount does not match quantity * MRP for product: " + item.getProductName());
        }
    }

    /**
     * Generate unique invoice number
     * Creates a formatted invoice number with order ID and timestamp
     */
    private String generateInvoiceNumber(Integer orderId) {
        return "INV-" + String.format("%06d", orderId) + "-" + 
               System.currentTimeMillis() % 10000;
    }
} 