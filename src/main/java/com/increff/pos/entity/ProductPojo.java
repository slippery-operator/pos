package com.increff.pos.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "product",
        uniqueConstraints = @UniqueConstraint(columnNames = {"barcode"}, name = "uk_product_barcode"))
@Getter
@Setter
@NoArgsConstructor
public class ProductPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "barcode", nullable = false, unique = true, length = 255)
    private String barcode;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "mrp", nullable = false)
    private Double mrp;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    public ProductPojo(String barcode, Integer clientId, String name, Double mrp) {
        this.barcode = barcode;
        this.clientId = clientId;
        this.name = name;
        this.mrp = mrp;
    }
}