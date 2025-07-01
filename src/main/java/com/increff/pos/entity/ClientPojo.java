package com.increff.pos.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_client_name")
})
//TODO: physical naming strategy
public class ClientPojo extends AbstractPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "client_id")
    private Integer clientId;

    @Column(name = "name", nullable = false, length = 255, unique = true)
    private String name;

    public ClientPojo(String name) {
        this.name = name;
    }
}