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
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = Logger.getLogger(AuthController.class);

    @Autowired
    private AuthDto dto;

    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupForm signupForm) {
        return dto.signup(signupForm);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginForm loginForm, HttpServletRequest httpRequest) {
        logger.info("Login attempt for email: " + loginForm.getEmail());
        logger.info("Request Origin: " + httpRequest.getHeader("Origin"));
        logger.info("Request User-Agent: " + httpRequest.getHeader("User-Agent"));

        return dto.login(loginForm, httpRequest);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest httpRequest) {
        return dto.logout(httpRequest);
    }

    @GetMapping("/session-info")
    public String getSessionInfo(HttpServletRequest httpRequest) {
        return dto.getSessionInfo(httpRequest);
    }
}
