package com.increff.pos.model.response;

import com.increff.pos.model.form.ClientForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse extends ClientForm {

    private Integer clientId;
}
