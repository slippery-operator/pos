package com.increff.pos.api;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.entity.InvoicePojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.DaySalesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

/**
 * API layer for Invoice entity operations in POS app
 * Handles business logic and data validation
 */
@Service
@Transactional
public class InvoiceApi {

    @Autowired
    private InvoiceDao invoiceDao;

    /**
     * Create a new invoice record
     * Validates invoice data and persists to database
     */
    public InvoicePojo createInvoice(InvoicePojo invoice) {
        validateInvoice(invoice);
        invoiceDao.insert(invoice);
        return invoice;
    }

    /**
     * Get invoice by order ID
     * Throws exception if invoice not found
     */
    public InvoicePojo getInvoiceByOrderId(Integer orderId) {
        InvoicePojo invoice = invoiceDao.selectByOrderId(orderId);
        if (invoice == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Invoice not found for order ID: " + orderId);
        }
        return invoice;
    }

    /**
     * Get invoice by invoice number
     * Throws exception if invoice not found
     */
    public InvoicePojo getInvoiceByInvoiceNumber(String invoiceNumber) {
        InvoicePojo invoice = invoiceDao.selectByInvoiceNumber(invoiceNumber);
        if (invoice == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Invoice not found with number: " + invoiceNumber);
        }
        return invoice;
    }

    /**
     * Get all invoices
     * Returns all invoices ordered by timestamp descending
     */
    public List<InvoicePojo> getAllInvoices() {
        return invoiceDao.selectAll();
    }

    /**
     * Get invoices by date range
     * Returns invoices within specified date range
     */
    public DaySalesModel getInvoicesDataByDateRange(Instant startDate, Instant endDate) {
        List<InvoicePojo> invoices = invoiceDao.selectByDateRange(startDate, endDate);
        int invoicedOrdersCount = invoices.size();
        int invoicedItemsCount = invoices.stream()
                .mapToInt(InvoicePojo::getCountOfItems)
                .sum();
        double totalRevenue = invoices.stream()
                .mapToDouble(InvoicePojo::getFinalRevenue)
                .sum();
        return new DaySalesModel(invoicedOrdersCount, invoicedItemsCount, totalRevenue);
    }

    /**
     * Check if invoice exists for order ID
     * Returns true if invoice exists, false otherwise
     */
    public boolean existsByOrderId(Integer orderId) {
        return invoiceDao.selectByOrderId(orderId) != null;
    }

    /**
     * Update invoice path
     * Updates the file path for an existing invoice
     */
    public void updateInvoicePath(Integer orderId, String invoicePath) {
        InvoicePojo invoice = getInvoiceByOrderId(orderId);
        invoice.setInvoicePath(invoicePath);
        invoiceDao.update(invoice);
    }

    /**
     * Create invoice with generated invoice number
     * Business logic for creating invoice with auto-generated number
     */
    public InvoicePojo createInvoiceWithGeneratedNumber(Integer orderId, Instant timeStamp, 
                                                       Integer countOfItems, String invoicePath, 
                                                       Double finalRevenue) {
        // Generate unique invoice number
        String invoiceNumber = generateInvoiceNumber(orderId);
        
        // Create invoice entity
        InvoicePojo invoice = new InvoicePojo(orderId, invoiceNumber, timeStamp, 
                                            countOfItems, invoicePath, finalRevenue);
        
        // Validate and save
        return createInvoice(invoice);
    }

    /**
     * Get invoice path by order ID
     * Returns the file path where invoice PDF is stored
     */
    public String getInvoicePathByOrderId(Integer orderId) {
        InvoicePojo invoice = getInvoiceByOrderId(orderId);
        return invoice.getInvoicePath();
    }

    /**
     * Validate invoice data
     * Performs comprehensive validation of invoice entity
     */
    private void validateInvoice(InvoicePojo invoice) {
        if (invoice.getOrderId() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order ID cannot be null");
        }
        if (invoice.getTimeStamp() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Time stamp cannot be null");
        }
        if (invoice.getCountOfItems() == null || invoice.getCountOfItems() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Count of items must be positive");
        }
        if (invoice.getFinalRevenue() == null || invoice.getFinalRevenue() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Final revenue must be positive");
        }
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().trim().isEmpty()) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Invoice number cannot be empty");
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