package com.increff.pos.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.Instant;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoicePojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer orderId;

    @Column(nullable = false)
    private ZonedDateTime timeStamp;

    @Column(nullable = false)
    private Integer countOfItems;

    @Column(nullable = false)
    private String invoicePath;

    @Column(nullable = false)
    private Double finalRevenue;

    public InvoicePojo(Integer orderId, ZonedDateTime timeStamp,
                      Integer countOfItems, String invoicePath, Double finalRevenue) {
        this.orderId = orderId;
        this.timeStamp = timeStamp;
        this.countOfItems = countOfItems;
        this.invoicePath = invoicePath;
        this.finalRevenue = finalRevenue;
    }
} 