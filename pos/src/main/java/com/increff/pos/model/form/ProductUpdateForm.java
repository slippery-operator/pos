package com.increff.pos.model.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Form for updating product information.
 * Contains only the fields that can be modified during an update operation:
 * - name: Product name (required)
 * - mrp: Maximum Retail Price (required, must be positive)
 * - imageUrl: URL to product image (optional)
 */
@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateForm {

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    @NotNull(message = "MRP cannot be null")
    @Positive(message = "MRP must be greater than 0")
    private Double mrp;

    private String imageUrl;
} 