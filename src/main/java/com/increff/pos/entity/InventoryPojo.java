package com.increff.pos.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class InventoryPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private Integer productId;

    @Column(nullable = false)
    private Integer quantity = 0;

    public InventoryPojo(Integer productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}