package com.increff.pos.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Response class for day sales data
 * Used for day-on-day sales reporting
 */
@Getter
@Setter
@NoArgsConstructor
public class DaySalesResponse {

    private ZonedDateTime date;
    private Integer invoicedOrdersCount;
    private Integer invoicedItemsCount;
    private Double totalRevenue;
} 