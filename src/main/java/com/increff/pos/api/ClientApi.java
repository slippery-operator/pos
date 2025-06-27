package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.DuplicateEntityException;
import com.increff.pos.exception.EntityNotFoundException;
import com.increff.pos.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

// TODO: define validateClientExist(clientId), validateClientsExist(set<clientId>)


@Service
@Transactional
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    public ClientPojo add(String name) {
        try {
            ClientPojo pojo = new ClientPojo(name);
            clientDao.insert(pojo);
            return pojo;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntityException("Client with name " + name + " already exists");
        }
    }

    public ClientPojo getById(Integer id) {
        ClientPojo pojo = clientDao.selectById(id);
        if(pojo == null) {
            throw new EntityNotFoundException("Client with ID " + id + " not found.");
        }
        return pojo;
    }


    public List<ClientPojo> getAll() {
        return clientDao.selectAll();
    }


    public ClientPojo update(Integer clientId, String name) {
        try {
            ClientPojo pojo = getById(clientId);
            pojo.setName(name);
            clientDao.update(pojo);
            return pojo;
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEntityException("Client with name " + name + " already exists");
        }
    }

    public void validateClientExists(Integer clientId)  {
        if(clientId == null || clientId <= 0) {
            throw new ValidationException("Invalid client id: " + clientId);
        }
        getById(clientId);
    }

    public void validateClientsExist(Set<Integer> clientIds) {
        for(Integer clientId: clientIds) {
            validateClientExists(clientId);
        }
    }

    public List<ClientPojo> searchByName(String name) {
        return clientDao.selectByNameContaining(name);
    }
}
