package com.increff.pos.util;

import com.increff.pos.exception.ValidationException;
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

public class TsvParserUtil {

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
            throw new RuntimeException("Error parsing product TSV file: " + e.getMessage(), e);
        }

        return productForms;
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
            throw new RuntimeException("Error parsing inventory TSV file: " + e.getMessage(), e);
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
    public static List<OrderItemForm> parseOrderItemsTsv(MultipartFile file) {
        List<OrderItemForm> orderItems = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new ValidationException("Empty TSV file");
            }

            // Validate header
            String[] headers = headerLine.split("\t");
            if (headers.length != 3 ||
                    !headers[0].trim().equalsIgnoreCase("barcode") ||
                    !headers[1].trim().equalsIgnoreCase("mrp") ||
                    !headers[2].trim().equalsIgnoreCase("quantity")) {
                throw new ValidationException("Invalid TSV format. Expected headers: barcode, mrp, quantity");
            }

            String line;
            int lineNumber = 2;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t");
                if (values.length != 3) {
                    throw new ValidationException("Invalid data format at line " + lineNumber);
                }

                try {
                    OrderItemForm orderItem = new OrderItemForm();
                    orderItem.setBarcode(values[0].trim());
                    orderItem.setMrp(Double.parseDouble(values[1].trim()));
                    orderItem.setQuantity(Integer.parseInt(values[2].trim()));

                    if (orderItem.getQuantity() <= 0) {
                        throw new ValidationException("Quantity must be positive at line " + lineNumber);
                    }
                    if (orderItem.getMrp() <= 0) {
                        throw new ValidationException("MRP must be positive at line " + lineNumber);
                    }

                    orderItems.add(orderItem);
                } catch (NumberFormatException e) {
                    throw new ValidationException("Invalid number format at line " + lineNumber);
                }

                lineNumber++;
            }

            if (orderItems.isEmpty()) {
                throw new ValidationException("No valid order items found in TSV file");
            }

        } catch (IOException e) {
            throw new ValidationException("Error reading TSV file: " + e.getMessage());
        }

        return orderItems;
    }
}