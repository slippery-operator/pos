package com.increff.pos.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
public class InventoryPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    public InventoryPojo(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}