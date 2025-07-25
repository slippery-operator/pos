package com.increff.pos.spring;

import com.increff.pos.model.Constants;
import com.increff.pos.util.JwtUtil;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = Logger.getLogger(CustomAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip authentication for public endpoints
        String requestPath = request.getRequestURI();
        if (isPublicEndpoint(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        // Check for JWT token in Authorization header
        String authHeader = request.getHeader("Authorization");
        boolean isAuthenticated = false;
        
        logger.debug("Request path: " + requestPath + ", Authorization header: " + (authHeader != null ? "present" : "null"));
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            logger.debug("JWT token found: " + token.substring(0, Math.min(20, token.length())) + "...");
            
            if (jwtUtil.validateToken(token)) {
                Integer userId = jwtUtil.getUserIdFromToken(token);
                String userRole = jwtUtil.getRoleFromToken(token);
                
                logger.debug("JWT token validated - userId: " + userId + ", userRole: " + userRole);

                // Create authentication token
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + userRole));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                // Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                isAuthenticated = true;

                logger.debug("JWT token validated for user: " + userId);
            } else {
                logger.debug("JWT token validation failed");
            }
        }
        if (!isAuthenticated) {
            logger.warn("Unauthorized access attempt to: " + requestPath + " - No valid JWT token found");
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"Authentication required\"}");
            response.setContentType("application/json");
            return; // Don't continue filter chain
        }
        filterChain.doFilter(request, response);
    }

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