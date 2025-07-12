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
        String normalizedName = toLowerCase(name);
        List<ClientPojo> pojos = clientApi.searchByName(normalizedName, page, size);
        return convertUtil.convertList(pojos, ClientResponse.class);
    }

    // ======================== PAGINATION METHODS ========================

//    /**
//     * Get all clients with pagination support
//     * @param page page number (0-based)
//     * @param size number of items per page
//     * @return paginated list of client responses ordered by name
//     */
//    public List<ClientResponse> getAllPaginated(int page, int size) {
//        // Validate pagination parameters
////        validatePaginationParams(page, size);
//
//        List<ClientPojo> pojos = clientApi.getAllPaginated(page, size);
//        return convertUtil.convertList(pojos, ClientResponse.class);
//    }

    /**
     * Search clients by name with pagination support
     *  name search term for client name (case-insensitive, prefix match)
     *  page page number (0-based)
     *  size number of items per page
     * @return paginated list of client responses matching the search criteria
     */
//    public List<ClientResponse> searchByNamePaginated(String name, int page, int size) {
//        // Validate pagination parameters
////        validatePaginationParams(page, size);
//
//        String normalizedName = toLowerCase(name);
//        List<ClientPojo> pojos = clientApi.searchByNamePaginated(normalizedName, page, size);
//        return convertUtil.convertList(pojos, ClientResponse.class);
//    }
//
//    /**
//     * Get total count of all clients (for pagination metadata)
//     * @return total count of clients
//     */
//    public long getTotalClientCount() {
//        return clientApi.getTotalClientCount();
//    }
//
//    /**
//     * Get count of clients matching name search criteria (for pagination metadata)
//     * @param name search term for client name
//     * @return count of clients matching the search criteria
//     */
//    public long getClientCountByName(String name) {
//        String normalizedName = toLowerCase(name);
//        return clientApi.getClientCountByName(normalizedName);
//    }


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