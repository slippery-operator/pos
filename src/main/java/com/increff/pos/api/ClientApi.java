package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.DuplicateEntityException;
import com.increff.pos.exception.EntityNotFoundException;
import com.increff.pos.exception.ValidationException;
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
        ClientPojo nameAlrExists = clientDao.selectByName(name);
        if(nameAlrExists != null) {
            throw new DuplicateEntityException("Client with name: " + name + " alr exists.");
        }

        ClientPojo pojo = new ClientPojo(name);
        clientDao.insert(pojo);
        return pojo;
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
        ClientPojo nameAlrExists = clientDao.selectByName(name);
        if(nameAlrExists != null) {
            throw new DuplicateEntityException("Client with name: " + name + " alr exists.");
        }

        ClientPojo pojo = getById(clientId);
        pojo.setName(name);
        clientDao.update(pojo);
        return pojo;

    }

    public void validateClientExists(Integer clientId)  {
        if(clientId == null || clientId <= 0) {
            throw new ValidationException("Invalid client id: " + clientId);
        } // move this to whoever calls it
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
