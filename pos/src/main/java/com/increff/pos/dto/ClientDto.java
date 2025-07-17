package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.response.ClientResponse;
import com.increff.pos.util.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.increff.pos.util.StringUtil.normalize;
import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class ClientDto extends AbstractDto<ClientForm> {

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ConvertUtil convertUtil;

    public ClientResponse add(ClientForm form) {
        validateForm(form);
        ClientPojo pojo = clientApi.add(normalize(form.getName()));
        return convertUtil.convert(pojo, ClientResponse.class);
    }

    public List<ClientResponse> getAll(int page, int size) {
        List<ClientPojo> pojos = clientApi.getAll(page, size);
        return convertUtil.convertList(pojos, ClientResponse.class);
    }

    public List<ClientResponse> searchByName(String name, int page, int size) {
        List<ClientPojo> pojos = clientApi.searchByName(toLowerCase(name), page, size);
        return convertUtil.convertList(pojos, ClientResponse.class);
    }

    public ClientResponse update(Integer clientId, ClientForm form) {
        validateUpdateInput(clientId, form);
        ClientPojo pojo = clientApi.update(clientId, normalize(form.getName()));
        return convertUtil.convert(pojo, ClientResponse.class);
    }

    private void validateUpdateInput(Integer clientId, ClientForm form) {
        validateId(clientId, "client Id");
        validateForm(form);
    }

}