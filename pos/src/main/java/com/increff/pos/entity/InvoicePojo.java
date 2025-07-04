package com.increff.pos.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

/**
 * Entity class for Invoice table in POS app
 * Tracks generated invoices and their local storage paths
 * invoice_path field determines if invoice has been generated for an order
 */
@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
public class InvoicePojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Integer orderId;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(name = "time_stamp", nullable = false)
    private Instant timeStamp;

    @Column(name = "count_of_items", nullable = false)
    private Integer countOfItems;

    @Column(name = "invoice_path", nullable = false)
    private String invoicePath;

    @Column(name = "final_revenue", nullable = false)
    private Double finalRevenue;

    /**
     * Constructor for creating invoice entity
     */
    public InvoicePojo(Integer orderId, String invoiceNumber, Instant timeStamp, 
                      Integer countOfItems, String invoicePath, Double finalRevenue) {
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.timeStamp = timeStamp;
        this.countOfItems = countOfItems;
        this.invoicePath = invoicePath;
        this.finalRevenue = finalRevenue;
    }
} 