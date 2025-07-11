package com.increff.pos.dto;

import com.increff.pos.api.AuthApi;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.model.form.SignupForm;
import com.increff.pos.model.response.LoginResponse;
import com.increff.pos.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.increff.pos.util.StringUtil.normalize;
import static com.increff.pos.util.StringUtil.toLowerCase;

/**
 * Data Transfer Object for authentication operations
 * Handles business logic for signup and login
 */
@Service
public class AuthDto {

    @Autowired
    private AuthApi api;

    /**
     * Handle user signup with role assignment based on email
     * @param signupForm signup form data
     * @return UserResponse with user details
     */

    public UserResponse signup(SignupForm signupForm) {
        return api.signup(normalize(signupForm.getName()), toLowerCase(signupForm.getEmail()), signupForm.getPassword());
    }

    /**
     * Handle user login with session management
     * @param loginForm login form data
     * @return LoginResponse with success message and user details
     */
    public LoginResponse login(LoginForm loginForm) {
        UserResponse user = api.login(normalize(loginForm.getEmail()), loginForm.getPassword());
        return new LoginResponse("Login successful", user);
    }
}
