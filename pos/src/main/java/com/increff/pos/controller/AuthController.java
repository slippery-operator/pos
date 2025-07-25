package com.increff.pos.controller;

import com.increff.pos.dto.AuthDto;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.model.form.SignupForm;
import com.increff.pos.model.response.LoginResponse;
import com.increff.pos.model.response.UserResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class);

    @Autowired
    private AuthDto dto;

    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupForm signupForm, HttpServletResponse response) {
        // Set CORS headers for signup response
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        response.setHeader("Access-Control-Allow-Headers", "*");
        
        return dto.signup(signupForm);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginForm loginForm, HttpServletRequest httpRequest, HttpServletResponse response) {
        logger.info("Login attempt for email: " + loginForm.getEmail());
        logger.info("Request Origin: " + httpRequest.getHeader("Origin"));
        logger.info("Request User-Agent: " + httpRequest.getHeader("User-Agent"));

        // Set CORS headers for login response
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        response.setHeader("Access-Control-Allow-Headers", "*");

        LoginResponse loginResponse = dto.login(loginForm, httpRequest);
        logger.info("Login successful, JWT token generated");
        
        return loginResponse;
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest httpRequest, HttpServletResponse response) {
        // Set CORS headers for logout response
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        response.setHeader("Access-Control-Allow-Headers", "*");
        
        return dto.logout(httpRequest);
    }

    @GetMapping("/session-info")
    public String getSessionInfo(HttpServletRequest httpRequest, HttpServletResponse response) {
        // Set CORS headers for session-info response
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
        response.setHeader("Access-Control-Allow-Headers", "*");
        return dto.getSessionInfo(httpRequest);
    }
}
