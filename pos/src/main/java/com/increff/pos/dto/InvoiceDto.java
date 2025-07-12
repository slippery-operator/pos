package com.increff.pos.dto;

import com.increff.pos.flow.InvoiceFlow;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.form.InvoiceGenerationForm;
import com.increff.pos.model.response.OrderWithInvoiceResponse;
import com.increff.pos.util.InvoiceServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class InvoiceDto extends AbstractDto<InvoiceGenerationForm>{

    @Autowired
    private InvoiceFlow invoiceFlow;

    @Autowired
    private InvoiceServiceClient invoiceServiceClient;

    public String generateInvoice(Integer orderId) {
        validateId(orderId,"orderId");
        OrderWithInvoiceResponse orderDataWithInvoice = invoiceFlow.getOrderDataForInvoice(orderId);

        String base64Pdf = invoiceServiceClient.generateInvoicePdf(orderDataWithInvoice);

        return invoiceFlow.processInvoiceGeneration(orderId, base64Pdf, orderDataWithInvoice);
    }

    public ResponseEntity<Resource> getInvoiceFile(Integer orderId) {
        validateId(orderId,"orderId");

        String path = invoiceFlow.getInvoicePath(orderId);
        File file = new File(path);

        if (!file.exists()) {
            throw new ApiException(ErrorType.NOT_FOUND, "Invoice file not found");
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf")
                .body(resource);
    }
} 