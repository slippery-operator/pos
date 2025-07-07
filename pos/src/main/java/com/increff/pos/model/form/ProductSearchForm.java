package com.increff.pos.model.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductSearchForm {
    private String barcode;
    private String productName;
}
