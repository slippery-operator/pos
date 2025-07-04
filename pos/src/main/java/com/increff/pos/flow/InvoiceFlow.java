package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.response.OrderItemInvoiceResponse;
import com.increff.pos.model.response.OrderWithInvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

/**
 * Flow layer for invoice operations
 * Orchestrates complex business logic spanning across multiple API classes
 * 
 * This layer handles:
 * - Orchestration between multiple APIs
 * - File system operations
 * - Cross-entity data retrieval
 * - Transaction management for complex operations
 * 
 * Note: Business logic is concentrated in API layer for reusability
 */
@Service
@Transactional(rollbackOn = Exception.class)
public class InvoiceFlow {

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    @Value("${invoice.storage.path:invoices}")
    private String invoiceStoragePath;

    /**
     * Get order data needed for invoice generation
     * Orchestrates data retrieval from multiple APIs for external service call
     * 
     * @param orderId The ID of the order
     * @return OrderWithInvoiceResponse containing order information
     * @throws ApiException if order not found or validation fails
     */
    public OrderWithInvoiceResponse getOrderDataForInvoice(Integer orderId) {
        // Check if invoice already exists (business validation in API)
        if (invoiceApi.existsByOrderId(orderId)) {
            throw new ApiException(ApiException.ErrorType.DUPLICATE_ENTRY, 
                "Invoice already exists for order ID: " + orderId);
        }

        // Get order details from Order API
        OrdersPojo order = orderApi.getOrderById(orderId);
        List<OrderItemsPojo> orderItems = orderItemApi.getOrderItemsByOrderId(orderId);

        // Business validation: Ensure order has items
        if (orderItems == null || orderItems.isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Order must have at least one item to generate invoice");
        }

        // Calculate total revenue from order items
        double totalRevenue = orderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getSellingPrice())
                .sum();

        // Convert order items to response format
        List<OrderItemInvoiceResponse> orderItemResponseList = orderItems.stream()
                .map(this::convertToOrderItemInvoiceResponse)
                .collect(java.util.stream.Collectors.toList());

        return new OrderWithInvoiceResponse(orderId, order.getTime(), orderItemResponseList, totalRevenue);
    }

    /**
     * Process invoice generation after external service call
     * Orchestrates file saving and database operations
     * 
     * @param orderId The ID of the order
     * @param base64Pdf Base64 encoded PDF from external service
     * @param orderData Order data for invoice creation
     * @return The file path where the PDF is stored locally
     * @throws ApiException if file operations or database operations fail
     */
    public String processInvoiceGeneration(Integer orderId, String base64Pdf, OrderWithInvoiceResponse orderData) {
        // Save PDF locally and get file path
        String invoicePath = savePdfLocally(base64Pdf, orderId);

        // Create invoice record in database using API
        invoiceApi.createInvoiceWithGeneratedNumber(
            orderData.getId(),
            orderData.getTime(),
            orderData.getOrderItems().size(),
            invoicePath,
            orderData.getTotalRevenue()
        );

        return invoicePath;
    }

    /**
     * Get invoice file path for order
     * Delegates to API layer for data retrieval
     * 
     * @param orderId The ID of the order
     * @return The file path where the invoice PDF is stored
     * @throws ApiException if invoice not found
     */
    public String getInvoicePath(Integer orderId) {
        return invoiceApi.getInvoicePathByOrderId(orderId);
    }

    /**
     * Save PDF locally to file system
     * Decodes base64 string and saves as PDF file
     * 
     * @param base64Pdf Base64 encoded PDF string
     * @param orderId Order ID for file naming
     * @return File path where PDF is saved
     * @throws ApiException if file operations fail
     */
    private String savePdfLocally(String base64Pdf, Integer orderId) {
        try {
            // Create invoices directory if it doesn't exist
            File invoicesDir = new File(invoiceStoragePath);
            if (!invoicesDir.exists()) {
                boolean created = invoicesDir.mkdirs();
                if (!created) {
                    throw new ApiException(ApiException.ErrorType.INTERNAL_ERROR, 
                        "Failed to create invoices directory");
                }
            }

            // Generate unique file name with timestamp
            String fileName = "invoice_" + orderId + "_" + 
                            Instant.now().getEpochSecond() + ".pdf";
            String filePath = invoiceStoragePath + File.separator + fileName;

            // Decode base64 string to PDF bytes
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);
            
            // Write PDF bytes to file
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
                fos.flush();
            }

            // Verify file was created successfully
            File savedFile = new File(filePath);
            if (!savedFile.exists() || savedFile.length() == 0) {
                throw new ApiException(ApiException.ErrorType.INTERNAL_ERROR, 
                    "Failed to save PDF file or file is empty");
            }

            return filePath;

        } catch (IOException e) {
            throw new ApiException(ApiException.ErrorType.INTERNAL_ERROR, 
                "Failed to save PDF file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Invalid base64 PDF data: " + e.getMessage());
        }
    }

    /**
     * Convert internal order item to response format
     * Maps internal data structure to response requirements
     */
    private OrderItemInvoiceResponse convertToOrderItemInvoiceResponse(OrderItemsPojo item) {
        // Get product details from Product API
        ProductPojo product = productApi.getProductById(item.getProductId());
        
        return new OrderItemInvoiceResponse(
            product.getName(),
            product.getBarcode(),
            item.getQuantity(),
            item.getSellingPrice()
        );
    }
} 