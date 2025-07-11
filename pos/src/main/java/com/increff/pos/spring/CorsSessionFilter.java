package com.increff.pos.spring;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Custom filter to handle CORS and session management for cross-origin requests.
 * This filter ensures that sessions work properly between the Angular frontend
 * and the Spring backend when running on different ports.
 */
@Component
public class CorsSessionFilter implements Filter {

    private static final Logger logger = Logger.getLogger(CorsSessionFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("CorsSessionFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String origin = httpRequest.getHeader("Origin");
        String method = httpRequest.getMethod();

        // Log request details for debugging
        logger.info("CORS Request: " + method + " " + httpRequest.getRequestURI() + " from Origin: " + origin);

        // Handle preflight OPTIONS requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            // Set CORS headers for preflight requests
            if ("http://localhost:4200".equals(origin)) {
                httpResponse.setHeader("Access-Control-Allow-Origin", origin);
                httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
                httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, Accept, Origin");
                httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                httpResponse.setHeader("Access-Control-Max-Age", "3600");
                
                // Return 200 OK for preflight requests
                httpResponse.setStatus(HttpServletResponse.SC_OK);
                logger.info("Preflight request handled successfully");
                return;
            }
        }

        // For actual requests, let Spring Security handle CORS
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        logger.info("CorsSessionFilter destroyed");
    }
} 