package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientApi extends AbstractApi {

    @Autowired
    private ClientDao clientDao;

    public ClientPojo add(String name) {
        checkConflict(clientDao.selectByName(name) != null,
                "Client with name: " + name + " already exists");
        ClientPojo client = new ClientPojo(name);
        clientDao.insert(client);
        return client;
    }

    public ClientPojo update(Integer id, String name) {
        ClientPojo existingClient = clientDao.selectByName(name);
        checkConflict(existingClient != null && !existingClient.getClientId().equals(id),
                "Client with name: " + name + " already exists");
        ClientPojo client = checkNotNull(clientDao.selectById(id), "Client with id: " + id + " not found");
        client.setName(name);
        return client;
    }

    public ClientPojo getClientById(Integer id) {
        return checkNotNull(clientDao.selectById(id), "Client with id: " + id + " not found");
    }

    public List<ClientPojo> getAll(int page, int size) {
        return clientDao.selectAll(page, size);
    }

    public List<ClientPojo> searchByName(String name, int page, int size) {
        return clientDao.selectByNameContaining(name, page, size);
    }

    public Map<Integer, Boolean> validateClientsExistBatch(Set<Integer> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        // Get all existing clients in one query
        List<ClientPojo> existingClients = clientDao.selectByIds(clientIds);
        Set<Integer> existingClientIds = existingClients.stream()
                .map(ClientPojo::getClientId)
                .collect(Collectors.toSet());
        // Create result map (true if client exists, false if not found)
        Map<Integer, Boolean> result = clientIds.stream()
                .collect(Collectors.toMap(
                        clientId -> clientId,
                        clientId -> existingClientIds.contains(clientId)
                ));
        return result;
    }
}