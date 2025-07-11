package com.increff.pos.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DaySalesPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private ZonedDateTime date;

    @Column(nullable = false)
    private Integer invoicedOrdersCount = 0;

    @Column(nullable = false)
    private Integer invoicedItemsCount = 0;

    @Column(nullable = false)
    private Double totalRevenue = 0.0;
}