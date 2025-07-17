package com.increff.pos.model.response;

import com.increff.pos.model.form.ProductForm;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class ProductResponse extends ProductForm {

    private Integer id;
}