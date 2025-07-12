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

    @Override
    public List<ClientPojo> selectAll(int page, int size) {
        return selectAllOrderedBy(page, size, "name", SortOrder.ASC);
    }

    public List<ClientPojo> selectByNameContaining(String name, int page, int size) {
        return selectByFieldLike(page, size, "name", name, "name", SortOrder.ASC);
    }

    public List<ClientPojo> selectByIds(Set<Integer> ids) {
        return selectByFieldValues("clientId", ids, null, SortOrder.ASC);
    }
}