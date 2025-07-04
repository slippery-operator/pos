package com.increff.pos.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ProductPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 255)
    private String barcode;

    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private Double mrp;

    @Column(length = 512)
    private String imageUrl;

    public ProductPojo(String barcode, Integer clientId, String name, Double mrp) {
        this.barcode = barcode;
        this.clientId = clientId;
        this.name = name;
        this.mrp = mrp;
    }
}