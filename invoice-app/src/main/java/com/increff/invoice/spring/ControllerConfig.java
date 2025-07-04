package com.increff.invoice.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Web MVC configuration for invoice-app
 * Configures JSON processing, Swagger, CORS, and web components
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
public class ControllerConfig extends WebMvcConfigurerAdapter {

    public static final String PACKAGE_CONTROLLER = "com.increff.invoice.controller";

    /**
     * Swagger API documentation bean
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .select().apis(RequestHandlerSelectors.basePackage(PACKAGE_CONTROLLER))
                .paths(PathSelectors.regex("/.*"))
                .build();
    }

    /**
     * Resource handlers for Swagger UI and static content
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    /**
     * CORS configuration
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * Jackson ObjectMapper bean for JSON serialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(ZonedDateTime.class,
                new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));
        return Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(javaTimeModule)
                .build();
    }

    /**
     * HandlerMappingIntrospector bean for Spring MVC
     */
    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector(ApplicationContext context) {
        return new HandlerMappingIntrospector(context);
    }
} 