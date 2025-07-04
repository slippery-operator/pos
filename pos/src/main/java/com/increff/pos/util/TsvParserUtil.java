package com.increff.pos.util;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.form.ProductForm;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for parsing TSV (Tab-Separated Values) files.
 * This class provides methods to parse different types of TSV files and convert them
 * to appropriate form objects. Uses the new ApiException with ErrorType enum for
 * consistent error handling.
 */
public class TsvParserUtil {

    /**
     * Parses a TSV file containing product data and converts it to a list of ProductForm objects.
     * Expected format: barcode, client_id, name, mrp, imageUrl (optional)
     * 
     * @param file The TSV file to parse
     * @return List of ProductForm objects
     * @throws ApiException with INTERNAL_SERVER_ERROR type if parsing fails
     */
    public static List<ProductForm> parseProductTsv(MultipartFile file) {
        List<ProductForm> productForms = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
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
                    productForms.add(form);
                }
            }
        } catch (Exception e) {
            throw new ApiException(ApiException.ErrorType.INTERNAL_SERVER_ERROR, 
                "Error parsing product TSV file: " + e.getMessage(), e);
        }

        return productForms;
    }

    /**
     * Parses a TSV file containing inventory data and converts it to a list of InventoryForm objects.
     * Expected format: product_id, quantity
     * 
     * @param file The TSV file to parse
     * @return List of InventoryForm objects
     * @throws ApiException with INTERNAL_SERVER_ERROR type if parsing fails
     */
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
            throw new ApiException(ApiException.ErrorType.INTERNAL_SERVER_ERROR, 
                "Error parsing inventory TSV file: " + e.getMessage(), e);
        }

        return inventoryForms;
    }

    /**
     * Parses a tab-delimited line, handling quoted fields properly.
     * This method correctly handles TSV format where fields may contain tabs within quotes.
     * 
     * @param line The line to parse
     * @return Array of field values
     */
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

    /**
     * Parses a TSV file containing order item data and converts it to a list of OrderItemForm objects.
     * Expected format: barcode, mrp, quantity
     * 
     * @param file The TSV file to parse
     * @return List of OrderItemForm objects
     * @throws ApiException with VALIDATION_ERROR type if file format is invalid
     * @throws ApiException with INTERNAL_SERVER_ERROR type if parsing fails
     */
    public static List<OrderItemForm> parseOrderItemsTsv(MultipartFile file) {
        List<OrderItemForm> orderItems = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Empty TSV file");
            }

            // Validate header
            String[] headers = headerLine.split("\t");
            if (headers.length != 3 ||
                    !headers[0].trim().equalsIgnoreCase("barcode") ||
                    !headers[1].trim().equalsIgnoreCase("mrp") ||
                    !headers[2].trim().equalsIgnoreCase("quantity")) {
                throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                    "Invalid TSV format. Expected headers: barcode, mrp, quantity");
            }

            String line;
            int lineNumber = 2;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                if (values.length != 3) {
                    throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                        "Invalid data format at line " + lineNumber);
                }

                try {
                    OrderItemForm orderItem = new OrderItemForm();
                    orderItem.setBarcode(values[0].trim());
                    orderItem.setMrp(Double.parseDouble(values[1].trim()));
                    orderItem.setQuantity(Integer.parseInt(values[2].trim()));

                    if (orderItem.getQuantity() <= 0) {
                        throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                            "Quantity must be positive at line " + lineNumber);
                    }
                    if (orderItem.getMrp() <= 0) {
                        throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                            "MRP must be positive at line " + lineNumber);
                    }

                    orderItems.add(orderItem);
                } catch (NumberFormatException e) {
                    throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                        "Invalid number format at line " + lineNumber);
                }

                lineNumber++;
            }

            if (orderItems.isEmpty()) {
                throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                    "No valid order items found in TSV file");
            }

        } catch (IOException e) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, 
                "Error reading TSV file: " + e.getMessage());
        }

        return orderItems;
    }
}