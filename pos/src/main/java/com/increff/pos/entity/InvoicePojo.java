package com.increff.pos.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// TODO: remove redundant table
public class InvoicePojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Integer orderId;

    @Column(name = "time_stamp", nullable = false)
    private Instant timeStamp;

    @Column(name = "count_of_items", nullable = false)
    private Integer countOfItems;

    @Column(name = "invoice_path", nullable = false)
    private String invoicePath;

    @Column(name = "final_revenue", nullable = false)
    private Double finalRevenue;

    public InvoicePojo(Integer orderId, Instant timeStamp,
                      Integer countOfItems, String invoicePath, Double finalRevenue) {
        this.orderId = orderId;
        this.timeStamp = timeStamp;
        this.countOfItems = countOfItems;
        this.invoicePath = invoicePath;
        this.finalRevenue = finalRevenue;
    }
} 