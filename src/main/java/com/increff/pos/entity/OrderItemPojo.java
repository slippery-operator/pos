package com.increff.pos.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;


@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItemPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderPojo order;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "selling_price", nullable = false)
    private Integer sellingPrice;

}
