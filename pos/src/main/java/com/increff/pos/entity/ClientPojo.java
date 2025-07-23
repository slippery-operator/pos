package com.increff.pos.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ClientPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer clientId;

    @Column(nullable = false, length = 51, unique = true)
    private String name;

    public ClientPojo(String name) {
        this.name = name;
    }
}