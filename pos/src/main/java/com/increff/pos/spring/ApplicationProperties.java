package com.increff.pos.spring;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ApplicationProperties {

    // Application properties
    @Value("${app.version:1.0}")
    private String appVersion;
    
    @Value("${app.name:Employee Application}")
    private String appName;
    
    @Value("${app.baseUrl:/}")
    private String appBaseUrl;

    // Database configuration properties
    @Value("${jdbc.driverClassName}")
    private String jdbcDriver;
    
    @Value("${jdbc.url}")
    private String jdbcUrl;
    
    @Value("${jdbc.username}")
    private String jdbcUsername;
    
    @Value("${jdbc.password}")
    private String jdbcPassword;

    // Hibernate configuration properties
    @Value("${hibernate.dialect}")
    private String hibernateDialect;
    
    @Value("${hibernate.show_sql:true}")
    private String hibernateShowSql;
    
    @Value("${hibernate.format_sql:true}")
    private String hibernateFormatSql;
    
    @Value("${hibernate.hbm2ddl.auto:update}")
    private String hibernateHbm2ddl;
    
    @Value("${hibernate.physical_naming_strategy}")
    private String namingStrategy;
    
    @Value("${hibernate.jdbc.time_zone:UTC}")
    private String hibernateTimeZone;

    // File upload configuration properties
    @Value("${spring.servlet.multipart.enabled:true}")
    private String multipartEnabled;
    
    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;
    
    @Value("${spring.servlet.multipart.max-request-size:10MB}")
    private String maxRequestSize;

    @Value("${invoice.app.url:http://localhost:9001}")
    private String invoiceAppUrl;

    @Value("${invoice.storage.path:invoices}")
    private String invoiceStoragePath;
} 