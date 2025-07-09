package com.increff.invoice.dto;

import com.increff.invoice.model.form.OrderRequest;
import com.increff.invoice.model.response.InvoiceResponse;
import com.increff.invoice.util.PdfGeneratorUtil;
import com.increff.invoice.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

/**
 *
 * Since this is a stateless service without persistence.
 */
@Service
public class InvoiceDto {

    @Autowired
    private PdfGeneratorUtil pdfGeneratorUtil;

    public InvoiceResponse generateInvoice(@Valid OrderRequest orderRequest) {
        try {
            String base64Pdf = pdfGeneratorUtil.generatePdfAsBase64(orderRequest);
            return new InvoiceResponse(orderRequest.getOrderId(), base64Pdf, true);
            
        } catch (Exception e) {
            throw new ApiException(ApiException.ErrorType.INTERNAL_SERVER_ERROR, "Failed to generate invoice." );
        }
    }
} 