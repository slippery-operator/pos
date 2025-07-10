package com.increff.pos.controller;

import com.increff.pos.dto.AuthDto;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.model.form.SignupForm;
import com.increff.pos.model.response.LoginResponse;
import com.increff.pos.model.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

/**
 * REST Controller for authentication operations
 * Handles signup, login, and logout endpoints
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthDto dto;

    /**
     * User signup endpoint
     * @param signupForm signup form data
     * @return UserResponse with user details
     */
    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupForm signupForm) {
        return dto.signup(signupForm);
    }

    /**
     * User login endpoint with session management
     * @param loginForm login form data
     * @param httpRequest HTTP request object for session management
     * @return LoginResponse with success message and user details
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginForm loginForm, HttpServletRequest httpRequest) {
        // Get user details from DTO
        UserResponse user = dto.login(loginForm).getUser();
        
        // Create or get existing session
        HttpSession session = httpRequest.getSession(true);
        
        // Store user information in session
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("lastCheckedTime", System.currentTimeMillis());

        return new LoginResponse("Login successful", user);
    }

    /**
     * User logout endpoint
     * @param httpRequest HTTP request object for session management
     * @return logout success message
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "Logout successful";
    }
}
