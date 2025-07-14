package com.increff.pos.dao;

import com.increff.pos.entity.UserPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDao extends AbstractDao<UserPojo> {

    public UserDao() {
        super(UserPojo.class);
    }

    private static String select_by_email = "select p from UserPojo p where email=:email";

    public Optional<UserPojo> selectByEmail(String email) {
        TypedQuery<UserPojo> query = getQuery(select_by_email, UserPojo.class);
        query.setParameter("email", email.toLowerCase().trim());
        List<UserPojo> users = query.getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    public Optional<UserPojo> selectByIdOptional(Integer id) {
        UserPojo user = super.selectById(id);
        return Optional.ofNullable(user);
    }

    public UserPojo insertUser(UserPojo user) {
        super.insert(user);
        return user;
    }

    public UserPojo updateUser(UserPojo user) {
        super.update(user);
        return user;
    }

    public void deleteUser(Integer id) {
        UserPojo user = super.selectById(id);
        if (user != null) {
            entityManager.remove(user);
        }
    }
}
