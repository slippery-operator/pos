package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.form.InvoiceGenerationForm;
import com.increff.pos.model.form.InvoiceItemForm;
import com.increff.pos.model.response.InvoiceGenerationResponse;
import com.increff.pos.model.response.OrderItemInvoiceResponse;
import com.increff.pos.model.response.OrderWithInvoiceResponse;
import com.increff.pos.spring.ApplicationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class InvoiceServiceClient {

    @Autowired
    private ApplicationProperties applicationProperties;

    private RestTemplate restTemplate = new RestTemplate();

    public String generateInvoicePdf(OrderWithInvoiceResponse orderData) {
        try {
            // Convert order data to external service format
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
            validateResponse(response);

            return response.getBase64Pdf();

        } catch (Exception e) {
            // Handle different types of exceptions
            if (e instanceof ApiException) {
                throw e; // Re-throw our custom exceptions
            }

            // Handle external service communication errors
            throw new ApiException(ErrorType.BAD_GATEWAY, "Error calling invoice service: " + e.getMessage());
        }
    }

    private void validateResponse(InvoiceGenerationResponse response) {
        if (response == null) {
            throw new ApiException(ErrorType.BAD_GATEWAY, "No response received from invoice service");
        }
        if (!response.isSuccess()) {
            throw new ApiException(ErrorType.BAD_GATEWAY, "Failed to generate invoice");
        }
        if (response.getBase64Pdf() == null || response.getBase64Pdf().trim().isEmpty()) {
            throw new ApiException(ErrorType.BAD_GATEWAY, "Invoice service returned empty PDF data");
        }
    }
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