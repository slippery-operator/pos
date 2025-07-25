package com.increff.pos.api;

import com.increff.pos.dao.UserDao;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.enums.Role;
import com.increff.pos.entity.UserPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZonedDateTime;

@Service
@Transactional
public class AuthApi {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse signup(String name, String email, String password) {
        // Check if user already exists
        if (userDao.selectByEmail(email).isPresent()) {
            throw new ApiException(ErrorType.CONFLICT, "User with this email already exists");
        }
        // Assign role based on email pattern
        Role role = determineRole(email);
        // Create new user
        UserPojo user = new UserPojo();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setLastLogin(null);
        // Save user to database
        UserPojo savedUser = userDao.insertUser(user);
        // Return user response without password
        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.getRole()
        );
    }

    public UserResponse login(String email, String password) {
        // Find user by email
        UserPojo user = userDao.selectByEmail(email)
                .orElseThrow(() -> new ApiException(ErrorType.BAD_REQUEST, "Wrong username or password."));
        // Validate password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ApiException(ErrorType.BAD_REQUEST, "Wrong username or password.");
        }
        // Update last login time
        user.setLastLogin(ZonedDateTime.now());
        userDao.updateUser(user);
        // Return user response without password
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole()
        );
    }

    private Role determineRole(String email) {
        // Email patterns that indicate supervisor role
        if (email.contains("supervisor") || email.contains("admin")) {
            return Role.SUPERVISOR;
        }
        // Default role for all other users
        return Role.OPERATOR;
    }
}
