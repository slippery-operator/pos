package com.increff.pos.model.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateForm {

    @NotNull(message = "Quantity cannot be null")
    @PositiveOrZero(message = "Quantity must be zero or positive")
    private Integer quantity;
}
