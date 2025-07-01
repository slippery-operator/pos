package com.increff.pos.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//TODO: to remove
public class ProductSearchData {

    private String barcode;
    private Integer clientId;
    private String productName;
}
