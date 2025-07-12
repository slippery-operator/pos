package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.InvoicePojo;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.response.OrderItemInvoiceResponse;
import com.increff.pos.model.response.OrderWithInvoiceResponse;
import com.increff.pos.spring.ApplicationProperties;
import com.increff.pos.util.PdfUtil;
import com.increff.pos.util.StringUtil;
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

@Service
@Transactional
public class InvoiceFlow {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    public OrderWithInvoiceResponse getOrderDataForInvoice(Integer orderId) {
        if (invoiceApi.existsByOrderId(orderId)) {
            throw new ApiException(ErrorType.CONFLICT, "Invoice already exists for order");
        }

        OrdersPojo order = orderApi.getOrderById(orderId);
        List<OrderItemsPojo> orderItems = orderItemApi.getOrderItemsByOrderId(orderId);

        if (orderItems == null || orderItems.isEmpty()) {
            throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "");
        }

        double totalRevenue = orderItems.stream()
                .mapToDouble(item -> item.getQuantity() * item.getSellingPrice())
                .sum();

        List<OrderItemInvoiceResponse> orderItemResponseList = orderItems.stream()
                .map(this::convertToOrderItemInvoiceResponse)
                .collect(java.util.stream.Collectors.toList());

        return new OrderWithInvoiceResponse(orderId, order.getTime(), totalRevenue, orderItemResponseList);
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
        String invoicePath = PdfUtil.savePdfLocally(base64Pdf, orderId, applicationProperties.getInvoiceStoragePath());

        InvoicePojo invoice = new InvoicePojo(orderId, orderData.getTime(),
                orderData.getOrderItems().size(), invoicePath, orderData.getTotalRevenue());

        // Create invoice record in database using API
        invoiceApi.createInvoice(invoice);

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