package com.increff.pos.model.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
public class ProductForm {

    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;

    @NotNull(message = "Client ID cannot be null")
    @Min(value = 1, message = "Client id must be greater than 0")
    private Integer clientId;

    @NotBlank(message = "Product name cannot be empty")
    private String name;

    @NotNull(message = "MRP cannot be null")
    @Positive(message = "MRP must be positive")
    private Double mrp;

    private String imageUrl;
}