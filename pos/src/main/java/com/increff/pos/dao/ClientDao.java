package com.increff.pos.dao;

import com.increff.pos.entity.ClientPojo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class ClientDao extends AbstractDao<ClientPojo> {

    public ClientDao() {
        super(ClientPojo.class);
    }

    // Override to sort clients by NAME using the new generic method from AbstractDao
    @Override
    public List<ClientPojo> selectAll() {
        return selectAllSortedBy("name", true); // Sort by name in ascending order
    }

    public List<ClientPojo> selectByNameContaining(String name) {
        return selectByNamePatternSortedBy(name.toLowerCase(), "name", true); // Sort by name in ascending order
    }

    /**
     * Select clients by a set of IDs using the generic method from AbstractDao.
     * This method is used for batch validation of client existence.
     * 
     * @param ids Set of client IDs to select
     * @return List of existing clients
     */
    public List<ClientPojo> selectByIds(Set<Integer> ids) {
        return super.selectByIds(ids, "clientId"); // Use the parent class method with correct field name
    }
}