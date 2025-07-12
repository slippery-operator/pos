package com.increff.pos.spring;

import com.increff.pos.model.Constants;
import org.apache.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom authentication filter that validates session-based authentication
 * This filter checks if user has a valid session and sets the security context
 */
@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(CustomAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Add this to your CustomAuthenticationFilter
        logger.info("Request: " + request.getMethod() + " " + request.getRequestURI());
        logger.info("Origin: " + request.getHeader("Origin"));

        // Skip authentication for public endpoints
        String requestPath = request.getRequestURI();
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get session without creating a new one
        HttpSession session = request.getSession(false);

        if (session != null) {
            // Get user information from session
            Integer userId = (Integer) session.getAttribute(Constants.SESSION_USER_ID);
            String userRole = (String) session.getAttribute(Constants.SESSION_ROLE);
            Long lastCheckedTime = (Long) session.getAttribute(Constants.SESSION_LAST_CHECKED_TIME);

            if (userId != null && userRole != null && lastCheckedTime != null) {
                // Check if session is still valid (within 5 minutes)
                long currentTime = System.currentTimeMillis();
                long timeDifference = currentTime - lastCheckedTime;

                // 5 minutes = 300,000 milliseconds
                if (timeDifference < Constants.SESSION_REVALIDATION_INTERVAL_MS) {
                    // Update last checked time
                    session.setAttribute("lastCheckedTime", currentTime);

                    // Create authentication token
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole));

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, authorities);

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // Session expired, invalidate it
                    session.invalidate();
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the endpoint is public and doesn't require authentication
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/auth/") ||
                path.startsWith("/public/") ||
                path.startsWith("/swagger-") ||
                path.startsWith("/v2/api-docs") ||
                path.startsWith("/webjars/") ||
                path.startsWith("/configuration/") ||
                path.equals("/swagger-ui.html");
    }
}