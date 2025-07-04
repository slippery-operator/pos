package com.increff.pos.dto;

import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.InvoiceGenerationForm;
import com.increff.pos.model.form.InvoiceItemForm;
import com.increff.pos.model.response.InvoiceGenerationResponse;
import com.increff.pos.model.response.OrderItemInvoiceResponse;
import com.increff.pos.model.response.OrderWithInvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * DTO layer for Invoice operations in POS app
 * Handles external layer responsibilities: validation, conversion, external service calls, and communication
 * 
 * This DTO follows the layering convention by:
 * - Performing external validation (null checks, data type checks)
 * - Converting external requests to internal format using manual conversion
 * - Making calls to external systems/APIs (invoice-app service)
 * - Calling the appropriate Flow layer for business logic
 * - Converting internal responses to external format
 */
@Service
public class InvoiceDto {

    @Autowired
    private InvoiceFlow invoiceFlow;

    @Value("${invoice.app.url:http://localhost:9001}")
    private String invoiceAppUrl;

    private RestTemplate restTemplate = new RestTemplate();

    /**
     * Generate invoice for order and store locally
     * Handles external validation, external service calls, and delegates to Flow layer for business logic
     * 
     * External Layer Responsibilities:
     * 1. Validate input parameters (null checks, data type validation)
     * 2. Call external invoice service to generate PDF
     * 3. Call Flow layer for business logic execution
     * 4. Return result to controller
     * 
     * @param orderId The ID of the order to generate invoice for
     * @return The file path where the PDF is stored locally
     * @throws ApiException if external validation fails or external service is unavailable
     */
    public String generateInvoice(Integer orderId) {
        // External validation: Check for null and data type validation
        validateOrderId(orderId);
        
        // Get order data from Flow layer for external service call
        OrderWithInvoiceResponse orderData = invoiceFlow.getOrderDataForInvoice(orderId);
        
        // Call external invoice service to generate PDF (DTO responsibility)
        String base64Pdf = callExternalInvoiceService(orderData);
        
        // Call Flow layer for business logic execution (file saving, database operations)
        return invoiceFlow.processInvoiceGeneration(orderId, base64Pdf, orderData);
    }

    /**
     * Get invoice file path for order
     * Handles external validation and delegates to Flow layer
     * 
     * @param orderId The ID of the order
     * @return The file path where the invoice PDF is stored
     * @throws ApiException if external validation fails
     */
    public String getInvoicePath(Integer orderId) {
        // External validation: Check for null and data type validation
        validateOrderId(orderId);
        
        // Call Flow layer for business logic execution
        return invoiceFlow.getInvoicePath(orderId);
    }

    /**
     * External validation for order ID parameter
     * Performs basic structural validation (null checks, data type validation)
     * This is NOT business validation - that happens in Flow layer
     */
    private void validateOrderId(Integer orderId) {
        if (orderId == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order ID cannot be null");
        }
        if (orderId <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order ID must be positive");
        }
    }

    /**
     * Call external invoice-app service to generate PDF
     * This is a DTO responsibility - external service communication
     * 
     * @param orderData Order data needed for invoice generation
     * @return Base64 encoded PDF string
     * @throws ApiException if external service call fails
     */
    private String callExternalInvoiceService(OrderWithInvoiceResponse orderData) {
        try {
            // Convert order data to external service format using manual conversion
            InvoiceGenerationForm request = createInvoiceGenerationForm(orderData);

            // Set HTTP headers for JSON communication
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make HTTP call to external invoice service
            HttpEntity<InvoiceGenerationForm> entity = new HttpEntity<>(request, headers);
            InvoiceGenerationResponse response = restTemplate.postForObject(
                invoiceAppUrl + "/invoice/generate", 
                entity, 
                InvoiceGenerationResponse.class
            );

            // Validate response from external service
            if (response == null) {
                throw new ApiException(ApiException.ErrorType.EXTERNAL_SERVICE_ERROR, 
                    "No response received from invoice service");
            }

            if (!response.isSuccess()) {
                throw new ApiException(ApiException.ErrorType.EXTERNAL_SERVICE_ERROR, 
                    "Failed to generate invoice: " + response.getMessage());
            }

            if (response.getBase64Pdf() == null || response.getBase64Pdf().trim().isEmpty()) {
                throw new ApiException(ApiException.ErrorType.EXTERNAL_SERVICE_ERROR, 
                    "Invoice service returned empty PDF data");
            }

            return response.getBase64Pdf();

        } catch (Exception e) {
            // Handle different types of exceptions
            if (e instanceof ApiException) {
                throw e; // Re-throw our custom exceptions
            }
            
            // Handle external service communication errors
            throw new ApiException(ApiException.ErrorType.EXTERNAL_SERVICE_ERROR, 
                "Error calling invoice service: " + e.getMessage());
        }
    }

    /**
     * Create request object for external invoice service
     * Converts internal order data to external service format
     */
    private InvoiceGenerationForm createInvoiceGenerationForm(OrderWithInvoiceResponse orderData) {
        InvoiceGenerationForm request = new InvoiceGenerationForm();
        request.setOrderId(orderData.getId());
        request.setOrderTime(orderData.getTime().toString());
        request.setClientName("Customer"); // Default client name
        request.setTotalRevenue(orderData.getTotalRevenue());

        // Convert order items to external service format
        List<InvoiceItemForm> items = orderData.getOrderItems().stream()
                .map(this::convertToInvoiceItemForm)
                .collect(java.util.stream.Collectors.toList());
        request.setOrderItems(items);

        return request;
    }

    /**
     * Convert internal order item to external service format
     * Maps internal data structure to external API requirements
     */
    private InvoiceItemForm convertToInvoiceItemForm(OrderItemInvoiceResponse item) {
        InvoiceItemForm request = new InvoiceItemForm();
        
        request.setProductName(item.getProductName());
        request.setBarcode(item.getBarcode());
        request.setQuantity(item.getQuantity());
        request.setMrp(item.getSellingPrice());
        request.setTotalAmount(item.getQuantity() * item.getSellingPrice());
        
        return request;
    }
} 