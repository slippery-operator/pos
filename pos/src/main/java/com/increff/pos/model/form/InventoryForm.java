package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
public class InventoryForm {

    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;

    @NotNull(message = "Quantity cannot be null")
    @PositiveOrZero(message = "Quantity must be greater than zero")
    private Integer quantity;
}
