package com.increff.pos.dto;

import com.increff.pos.api.AuthApi;
import com.increff.pos.controller.AuthController;
import com.increff.pos.model.form.LoginForm;
import com.increff.pos.model.form.SignupForm;
import com.increff.pos.model.response.LoginResponse;
import com.increff.pos.model.response.UserResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static com.increff.pos.util.StringUtil.normalize;
import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class AuthDto {

    private static final Logger logger = Logger.getLogger(AuthDto.class);

    @Autowired
    private AuthApi api;

    public UserResponse signup(SignupForm signupForm) {
        return api.signup(normalize(signupForm.getName()), toLowerCase(signupForm.getEmail()), signupForm.getPassword());
    }

    public LoginResponse login(LoginForm loginForm, HttpServletRequest httpRequest) {
        UserResponse user = api.login(normalize(loginForm.getEmail()), loginForm.getPassword());
        HttpSession session = httpRequest.getSession(true);
        logger.info("Session created/retrieved: " + session.getId());
        // Store user information in session
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole().name());
        session.setAttribute("lastCheckedTime", System.currentTimeMillis());
        logger.info("Login successful for user: " + user.getId() + " with role: " + user.getRole());
        return new LoginResponse("Login successful", user);
    }

    public String logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "Logout successful";
    }

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
