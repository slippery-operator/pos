
package com.increff.pos.model.form;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class OrderForm {
    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemForm> orderItems;
}