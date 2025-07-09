package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    public ClientPojo add(String name) {
        ClientPojo existingClient = clientDao.selectByName(name);
        if (existingClient != null) {
            throw new ApiException(ApiException.ErrorType.CONFLICT, "Client with name: " + name + " already exists.");
        }
        ClientPojo client = new ClientPojo(name);
        clientDao.insert(client);
        return client;
    }

    public ClientPojo update(Integer id, String name) {
        ClientPojo existingClient = clientDao.selectByName(name);
        if (existingClient != null && !existingClient.getClientId().equals(id)) {
            throw new ApiException(ApiException.ErrorType.CONFLICT, "Client with name: " + name + " already exists");
        }

        ClientPojo client = clientDao.selectById(id);
        if (client == null) {
            // TODO: add id info to error
            throw new ApiException(ApiException.ErrorType.NOT_FOUND, "Client not found");
        }
        client.setName(name);
        return client;
    }

    public List<ClientPojo> getAll() {
        return clientDao.selectAll();
    }

    public List<ClientPojo> searchByName(String name) {
        return clientDao.selectByNameContaining(name);
    }

    /**
     * Validates that all clients exist and returns a map of client ID to validation status.
     * This method performs batch validation for better performance.
     * 
     * @param clientIds Set of client IDs to validate
     * @return Map of client ID to boolean (true if client exists, false otherwise)
     */
    public Map<Integer, Boolean> validateClientsExistBatch(Set<Integer> clientIds) {
        Map<Integer, Boolean> result = new HashMap<>();
        
        if (clientIds == null || clientIds.isEmpty()) {
            return result;
        }
        
        // Get all existing clients in one query
        List<ClientPojo> existingClients = clientDao.selectByIds(clientIds);
        Set<Integer> existingClientIds = existingClients.stream()
                .map(ClientPojo::getClientId)
                .collect(Collectors.toSet());
        
        // Create result map
        for (Integer clientId : clientIds) {
            result.put(clientId, existingClientIds.contains(clientId));
        }
        
        return result;
    }
}