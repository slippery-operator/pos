package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class OrderItemForm {

    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "MRP cannot be null")
    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private Double mrp;

    private Integer productId;
    private Integer orderId;
}
