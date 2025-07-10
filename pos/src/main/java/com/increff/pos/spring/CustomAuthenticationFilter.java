package com.increff.pos.spring;

import com.increff.pos.dao.UserDao;
import com.increff.pos.entity.UserPojo;
import com.increff.pos.util.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Custom authentication filter for session-based authentication
 * Handles role-based access control and session validation
 */
@Component
public class CustomAuthenticationFilter implements Filter {

    @Autowired
    private UserDao userDao;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();

        // Skip authentication for auth and public endpoints
        if (requestURI.startsWith("/auth/") || requestURI.startsWith("/public/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            sendUnauthorizedResponse(httpResponse, "No session found");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("userRole");
        Long lastCheckedTime = (Long) session.getAttribute("lastCheckedTime");

        if (userId == null || userRole == null) {
            sendUnauthorizedResponse(httpResponse, "Invalid session");
            return;
        }

        // Check authentication every 5 minutes (300000 milliseconds)
        long currentTime = System.currentTimeMillis();
        if (lastCheckedTime != null && (currentTime - lastCheckedTime) > 300000) {
            // Re-validate user exists in database
            Optional<UserPojo> user = userDao.selectByIdOptional(userId);
            if (!user.isPresent()) {
                session.invalidate();
                sendUnauthorizedResponse(httpResponse, "User not found");
                return;
            }
            // Update last checked time
            session.setAttribute("lastCheckedTime", currentTime);
        }

        // Role-based access control
        if (!hasPermission(requestURI, httpRequest.getMethod(), userRole)) {
            sendForbiddenResponse(httpResponse, "Insufficient permissions");
            return;
        }

        // Set security context for Spring Security
        setSecurityContext(userId, userRole);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if needed
    }

    /**
     * Check if user has permission to access the requested resource
     * @param uri request URI
     * @param method HTTP method
     * @param role user role
     * @return true if user has permission, false otherwise
     */
    private boolean hasPermission(String uri, String method, String role) {
        // SUPERVISOR has full access to all endpoints
        if ("SUPERVISOR".equals(role)) {
            return true;
        }

        // OPERATOR has restricted access
        if ("OPERATOR".equals(role)) {
            // OPERATOR cannot access upload endpoints
            if (uri.contains("/upload") ||
                    (uri.contains("/products") && "POST".equals(method)) ||
                    (uri.contains("/inventory") && "POST".equals(method))) {
                return false;
            }
            
            // OPERATOR cannot access reports endpoints
            if (uri.contains("/reports")) {
                return false;
            }
            
            return true;
        }

        return false;
    }

    /**
     * Set Spring Security context with user information
     * @param userId user ID
     * @param role user role
     */
    private void setSecurityContext(Integer userId, String role) {
        UserPrincipal principal = new UserPrincipal(userId.longValue(), role);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null,
                        Arrays.asList(new SimpleGrantedAuthority("ROLE_" + role)));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * Send unauthorized response
     * @param response HTTP response
     * @param message error message
     * @throws IOException if response writing fails
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    /**
     * Send forbidden response
     * @param response HTTP response
     * @param message error message
     * @throws IOException if response writing fails
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
}