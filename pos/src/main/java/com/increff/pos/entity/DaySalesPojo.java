package com.increff.pos.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity class for daily sales reporting
 * Stores aggregated daily sales data for reporting
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
@Entity
@Table(name = "pos_day_sales")
@Getter
@Setter
@NoArgsConstructor
public class DaySalesPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "invoiced_orders_count", nullable = false)
    private Integer invoicedOrdersCount = 0;

    @Column(name = "invoiced_items_count", nullable = false)
    private Integer invoicedItemsCount = 0;

    @Column(name = "total_revenue", nullable = false)
    private Double totalRevenue = 0.0;

    // All constructors and methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
} 