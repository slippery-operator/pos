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

    /**
     * Find user by email (case-insensitive)
     * @param email email to search for
     * @return Optional containing user if found
     */
    public Optional<UserPojo> selectByEmail(String email) {
        TypedQuery<UserPojo> query = getQuery(select_by_email, UserPojo.class);
        query.setParameter("email", email.toLowerCase().trim());
        List<UserPojo> users = query.getResultList();
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    /**
     * Find user by ID
     * @param id user ID
     * @return Optional containing user if found
     */
    public Optional<UserPojo> selectByIdOptional(Integer id) {
        UserPojo user = super.selectById(id);
        return Optional.ofNullable(user);
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<UserPojo> selectAllUsers() {
        return super.selectAll();
    }

    /**
     * Insert a new user
     * @param user user to insert
     * @return inserted user with generated ID
     */
    public UserPojo insertUser(UserPojo user) {
        super.insert(user);
        return user;
    }

    /**
     * Update an existing user
     * @param user user to update
     * @return updated user
     */
    public UserPojo updateUser(UserPojo user) {
        super.update(user);
        return user;
    }

    /**
     * Delete a user
     * @param id user ID to delete
     */
    public void deleteUser(Integer id) {
        UserPojo user = super.selectById(id);
        if (user != null) {
            entityManager.remove(user);
        }
    }
}
