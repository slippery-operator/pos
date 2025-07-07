package com.increff.pos.dto;

import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDto<T> {

    @Autowired
    protected ValidationUtil validationUtil;

    protected <U> void validateForm(U form) {
        validationUtil.validateForm(form);
    }


    /**
     * Common method to validate ID parameters
     */
    protected void validateId(Integer id, String fieldName) {
        validationUtil.validateId(id, fieldName);
    }

    /**
     * Common method to validate search parameters
     */
    protected void validateSearchName(String name) {
        validationUtil.validateSearchName(name);
    }
}