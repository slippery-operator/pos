package com.increff.invoice.spring;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class for invoice-app
 * Enables component scanning for stateless service
 */
@Configuration
@ComponentScan("com.increff.invoice")
public class SpringConfig {
    // Configuration will be handled by component scanning
} 