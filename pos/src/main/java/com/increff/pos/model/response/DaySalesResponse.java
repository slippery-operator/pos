package com.increff.pos.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Response class for day sales data
 * Used for day-on-day sales reporting
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
@Getter
@Setter
@NoArgsConstructor
public class DaySalesResponse {

    private LocalDate date;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private Double totalRevenue;

    // All constructors and methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
} 