package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;

public class PdfUtil {

    public static String savePdfLocally(String base64Pdf, Integer orderId, String invoiceStoragePath) {
        try {
            // Ensure directory exists
            File invoicesDir = new File(invoiceStoragePath);
            if (!invoicesDir.exists()) {
                boolean created = invoicesDir.mkdirs();
                if (!created) {
                    throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Failed to create invoices directory");
                }
            }

            // Create unique file name
            String fileName = "invoice_" + orderId + "_" + Instant.now().getEpochSecond() + ".pdf";
            String filePath = invoiceStoragePath + File.separator + fileName;

            // Decode and write to file
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);
            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
                fos.flush();
            }

            File savedFile = new File(filePath);
            if (!savedFile.exists() || savedFile.length() == 0) {
                throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Failed to save PDF file or file is empty");
            }

            return filePath;

        } catch (IOException e) {
            throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Failed to save PDF file");
        } catch (IllegalArgumentException e) {
            throw new ApiException(ErrorType.BAD_REQUEST, "Invalid base64 PDF data");
        }
    }
}
