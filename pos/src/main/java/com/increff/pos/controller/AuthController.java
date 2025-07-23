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
    //TODO: move the logic to DTO
    public LoginResponse login(@Valid @RequestBody LoginForm loginForm, HttpServletRequest httpRequest) {
        logger.info("Login attempt for email: " + loginForm.getEmail());
        logger.info("Request Origin: " + httpRequest.getHeader("Origin"));
        logger.info("Request User-Agent: " + httpRequest.getHeader("User-Agent"));
        // Get user details from DTO
        UserResponse user = dto.login(loginForm).getUser();
        // Create or get existing session
        HttpSession session = httpRequest.getSession(true);
        logger.info("Session created/retrieved: " + session.getId());
        // Store user information in session
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("lastCheckedTime", System.currentTimeMillis());
        logger.info("Login successful for user: " + user.getId() + " with role: " + user.getRole());
        return new LoginResponse("Login successful", user);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "Logout successful";
    }

    @GetMapping("/session-info")
    public String getSessionInfo(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");
            String userRole = (String) session.getAttribute("userRole");
            Long lastCheckedTime = (Long) session.getAttribute("lastCheckedTime");

            if (userId != null && userRole != null && lastCheckedTime != null) {
                // Check if session is still valid (within 5 minutes)
                long currentTime = System.currentTimeMillis();
                long timeDifference = currentTime - lastCheckedTime;

                if (timeDifference < 300_000) { // 5 minutes
                    // Update last checked time
                    session.setAttribute("lastCheckedTime", currentTime);
                    return "Session active for user: " + userId + " with role: " + userRole;
                } else {
                    session.invalidate();
                    return "Session expired";
                }
            }
        }
        return "No active session";
    }
}
