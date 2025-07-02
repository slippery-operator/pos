package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.response.ClientResponse;
import com.increff.pos.util.ConvertUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.increff.pos.util.StringUtil.normalize;

@Service
public class ClientDto extends AbstractDto<ClientForm> {

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ConvertUtil convertUtil;

    private static final Logger logger = Logger.getLogger(ClientDto.class);

    public ClientResponse add(ClientForm form) {
        validateForm(form);
        String name = normalize(form.getName());

        ClientPojo pojo = clientApi.add(name);
        return convertUtil.convert(pojo, ClientResponse.class);
    }

    public List<ClientResponse> getAll() {
        logger.info("getAll() @ ClientDto");

        List<ClientPojo> pojos = clientApi.getAll();
        return convertUtil.convertList(pojos, ClientResponse.class);
    }

    public List<ClientResponse> searchByName(String name) {
        validateSearchName(name);
        String normalizedName = normalize(name);

        List<ClientPojo> pojos = clientApi.searchByName(normalizedName);
        return convertUtil.convertList(pojos, ClientResponse.class);
    }

    public ClientResponse update(Integer clientId, ClientForm form) {
        validateId(clientId, "client Id");
        validateForm(form);
        String name = normalize(form.getName());

        ClientPojo pojo = clientApi.update(clientId, name);
        return convertUtil.convert(pojo, ClientResponse.class);
    }

    @Override
    protected void validateForm(ClientForm form) {
        validationUtil.validateForm(form);
    }
}