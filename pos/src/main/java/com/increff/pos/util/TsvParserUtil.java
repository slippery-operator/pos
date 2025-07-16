package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductFormWithRow;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TsvParserUtil {

    public static List<ProductFormWithRow> parseProductTsv(MultipartFile file) {
        List<ProductFormWithRow> productForms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            String[] expectedHeaders = {"barcode", "client_id", "name", "mrp", "imageUrl"};
            int rowNumber = 0;

            while ((line = reader.readLine()) != null) {
                rowNumber++;
                
                if (isFirstLine) {
                    // Validate headers
                    validateHeaders(line, expectedHeaders);
                    isFirstLine = false;
                    continue; // Skip header
                }

                String[] fields = parseTabDelimitedLine(line);
                System.out.println("TSV line parsed: " + Arrays.toString(fields));

                if (fields.length >= 4) { // barcode, client_id, name, mrp (imageUrl optional)
                    ProductForm form = new ProductForm();
                    form.setBarcode(fields[0].trim());
                    form.setClientId(Integer.parseInt(fields[1].trim()));
                    form.setName(fields[2].trim());
                    form.setMrp(Double.parseDouble(fields[3].trim()));
                    if (fields.length > 4 && !fields[4].trim().isEmpty()) {
                        form.setImageUrl(fields[4].trim());
                    }
                    productForms.add(new ProductFormWithRow(form, rowNumber, line));
                }
            }
        } catch (Exception e) {
            throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Error parsing product TSV file");
        }
        return productForms;
    }

    private static void validateHeaders(String headerLine, String[] expectedHeaders) {
        String[] actualHeaders = parseTabDelimitedLine(headerLine);
        if (actualHeaders.length < 4) {
            throw new ApiException(ErrorType.BAD_REQUEST, 
                "Invalid TSV format: Expected at least 4 columns (barcode, client_id, name, mrp), got " + actualHeaders.length);
        }
        // Check if required headers are present (case-insensitive)
        for (int i = 0; i < Math.min(actualHeaders.length, expectedHeaders.length); i++) {
            if (!actualHeaders[i].trim().toLowerCase().equals(expectedHeaders[i].toLowerCase())) {
                throw new ApiException(ErrorType.BAD_REQUEST, 
                    "Invalid header at column " + (i + 1) + ": Expected '" + expectedHeaders[i] + "', got '" + actualHeaders[i] + "'");
            }
        }
    }

    public static List<InventoryForm> parseInventoryTsv(MultipartFile file) {
        List<InventoryForm> inventoryForms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
              String[] fields = parseTabDelimitedLine(line);
                if (fields.length >= 2) { // product_id, quantity
                    InventoryForm form = new InventoryForm();
                    form.setProductId(Integer.parseInt(fields[0].trim()));
                    form.setQuantity(Integer.parseInt(fields[1].trim()));
                    inventoryForms.add(form);
                }
            }
        } catch (Exception e) {
            throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Error parsing inventory TSV file");
        }
        return inventoryForms;
    }

    private static String[] parseTabDelimitedLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == '\t' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        // Add the last field
        fields.add(currentField.toString());

        return fields.toArray(new String[0]);
    }

    public static List<InventoryFormWithRow> parseInventoryTsvWithRow(MultipartFile file) {
        List<InventoryForm> forms = parseInventoryTsv(file);
        List<InventoryFormWithRow> result =
                IntStream.range(0, forms.size())
                        .mapToObj(i -> new InventoryFormWithRow(i + 1, forms.get(i))) // +1 for row number
                        .collect(Collectors.toList());

        return result;
    }
}