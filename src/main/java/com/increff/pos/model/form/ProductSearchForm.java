package com.increff.pos.model.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductSearchForm {

    @ApiModelProperty(value = "Product ID", required = false
    )
    private Integer id;

    @ApiModelProperty(value = "Product barcode", required = false
    )
    private String barcode;

    @ApiModelProperty(value = "Client ID", required = false
    )
    private Integer clientId;

    @ApiModelProperty(value = "Product name", required = false
    )
    private String productName;
}
