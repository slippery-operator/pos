package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    public ClientPojo add(String name) {
        // Check if client with same name already exists
        ClientPojo existingClient = clientDao.selectByName(name);
        if (existingClient != null) {
            throw new ApiException(ApiException.ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Client with name: " + name + " already exists.");
        }

        ClientPojo client = new ClientPojo();
        client.setName(name);
        clientDao.insert(client);
        return client;
    }

    public ClientPojo getById(Integer id) {
        ClientPojo client = clientDao.selectById(id);
        if (client == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND,
                    "Client with ID " + id + " not found.");
        }
        return client;
    }

    public ClientPojo update(Integer id, String name) {
        // Check if client with same name already exists (excluding current client)
        ClientPojo existingClient = clientDao.selectByName(name);
        if (existingClient != null && !existingClient.getClientId().equals(id)) {
            throw new ApiException(ApiException.ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Client with name: " + name + " already exists.");
        }

        ClientPojo client = clientDao.selectById(id);
        if (client == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND,
                    "Client with ID " + id + " not found.");
        }

        client.setName(name);
        clientDao.update(client);
        return client;
    }

    public void validateClientExists(Integer clientId) {
        if (clientId == null || clientDao.selectById(clientId) == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR,
                    "Invalid client id: " + clientId);
        }
    }

    public void validateClientsExist(Set<Integer> clientIds) {
        for (Integer clientId : clientIds) {
            validateClientExists(clientId);
        }
    }

    public List<ClientPojo> getAll() {
        return clientDao.selectAll();
    }

    public List<ClientPojo> searchByName(String name) {
        return clientDao.selectByNameContaining(name);
    }
}