package com.increff.pos.dto;

import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.InvoiceGenerationForm;
import com.increff.pos.model.form.InvoiceItemForm;
import com.increff.pos.model.response.InvoiceGenerationResponse;
import com.increff.pos.model.response.OrderItemInvoiceResponse;
import com.increff.pos.model.response.OrderWithInvoiceResponse;
import com.increff.pos.spring.ApplicationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.util.List;

@Service
public class InvoiceDto extends AbstractDto<InvoiceGenerationForm>{

    @Autowired
    private InvoiceFlow invoiceFlow;

    @Autowired
    private ApplicationProperties applicationProperties;

    private RestTemplate restTemplate = new RestTemplate();

    public String generateInvoice(Integer orderId) {
       validateId(orderId,"orderId");

        
        // Get order data from Flow layer for external service call
        OrderWithInvoiceResponse orderDataWithInvoice = invoiceFlow.getOrderDataForInvoice(orderId);

        // there should have beeen some validation for orderDataWithInvoice here
        
        // Call external invoice service to generate PDF (DTO responsibility)
        String base64Pdf = callExternalInvoiceService(orderDataWithInvoice);
        
        // Call Flow layer for business logic execution (file saving, database operations)
        return invoiceFlow.processInvoiceGeneration(orderId, base64Pdf, orderDataWithInvoice);
    }

    public ResponseEntity<Resource> getInvoiceFile(Integer orderId) {
        validateId(orderId,"orderId");

        String path = invoiceFlow.getInvoicePath(orderId);
        File file = new File(path);

        if (!file.exists()) {
            throw new ApiException(ApiException.ErrorType.NOT_FOUND, "Invoice file not found");
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf")
                .body(resource);
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
                applicationProperties.getInvoiceAppUrl() + "/invoice/generate",
                entity, 
                InvoiceGenerationResponse.class
            );

            // Validate response from external service
            if (response == null) {
                throw new ApiException(ApiException.ErrorType.BAD_GATEWAY, "No response received from invoice service");
            }

            if (!response.isSuccess()) {
                throw new ApiException(ApiException.ErrorType.BAD_GATEWAY, "Failed to generate invoice");
            }

            if (response.getBase64Pdf() == null || response.getBase64Pdf().trim().isEmpty()) {
                throw new ApiException(ApiException.ErrorType.BAD_GATEWAY, "Invoice service returned empty PDF data");
            }

            return response.getBase64Pdf();

        } catch (Exception e) {
            // Handle different types of exceptions
            if (e instanceof ApiException) {
                throw e; // Re-throw our custom exceptions
            }
            
            // Handle external service communication errors
            throw new ApiException(ApiException.ErrorType.BAD_GATEWAY, "Error calling invoice service: " + e.getMessage());
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