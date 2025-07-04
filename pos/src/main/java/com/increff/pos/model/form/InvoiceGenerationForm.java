package com.increff.pos.model.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * Form class for invoice generation requests
 * Used for external invoice service communication
 */
@Getter
@Setter
@NoArgsConstructor
public class InvoiceGenerationForm {

    @NotNull(message = "Order ID cannot be null")
    @Positive(message = "Order ID must be positive")
    private Integer orderId;

    @NotNull(message = "Order time cannot be null")
    private String orderTime;

    @NotNull(message = "Client name cannot be null")
    private String clientName;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<InvoiceItemForm> orderItems;

    @NotNull(message = "Total revenue cannot be null")
    @Positive(message = "Total revenue must be positive")
    private Double totalRevenue;
} 