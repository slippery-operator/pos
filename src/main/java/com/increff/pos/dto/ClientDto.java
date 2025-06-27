package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ValidationException;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.response.ClientResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.increff.pos.util.StringUtil.normalize;

@Service
public class ClientDto {

   @Autowired
   private ClientApi clientApi;

   @Autowired
   private Validator validator;

    //Logger that prints out incoming forms & outgoing Responses

    private static final Logger logger = Logger.getLogger(ClientDto.class);

    public ClientResponse add(ClientForm form) {
        validateForm(form);
        String name = normalize(form.getName());
        ClientPojo pojo = clientApi.add(name);
        return convertToResponse(pojo);
    }

    public List<ClientResponse> getAll() {
        logger.info("getAll() @ ClientDto");
        List<ClientPojo> pojos = clientApi.getAll();
        return convertToResponseList(pojos);
    }

    public List<ClientResponse> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ApiException("Search name cannot be empty");
        }

        String normalizedName = normalize(name);
        List<ClientPojo> pojos = clientApi.searchByName(normalizedName);
        return convertToResponseList(pojos);
    }

    public ClientResponse update(Integer clientId, ClientForm form) {
        validateForm(form);
        String name = normalize(form.getName());
        ClientPojo pojo = clientApi.update(clientId, name);
        return convertToResponse(pojo);
    }

    private void validateForm(ClientForm form) {
        Set<ConstraintViolation<ClientForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<ClientForm> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new ValidationException("Validation failed: " + sb.toString());
        }
    }

    private ClientResponse convertToResponse(ClientPojo pojo) {
        ClientResponse data = new ClientResponse();
        data.setClientId(pojo.getClientId());
        data.setName(pojo.getName());
        data.setVersion(pojo.getVersion());
        data.setCreatedAt(pojo.getCreatedAt());
        data.setUpdatedAt(pojo.getUpdatedAt());
        return data;
    }

    // Reduced redundant code - single method to convert list
    private List<ClientResponse> convertToResponseList(List<ClientPojo> pojos) {
        return pojos.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
}
