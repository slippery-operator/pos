package com.increff.pos.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
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

// Add these imports after your existing imports
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Web MVC configuration class that sets up controllers, Swagger documentation,
 * CORS support, and Jackson object mapping.
 * 
 * This class consolidates all web-related configuration including:
 * - Swagger API documentation
 * - CORS configuration for cross-origin requests
 * - Jackson JSON serialization configuration
 * - Resource handlers for static content
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
public class ControllerConfig extends WebMvcConfigurerAdapter {

//	TODO: create another class constants in util
	public static final String PACKAGE_CONTROLLER = "com.increff.pos.controller";

	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * Creates and configures the Swagger API documentation bean.
	 * This bean generates API documentation for all controllers in the specified package.
	 * 
	 * @return Configured Docket bean for Swagger
	 */
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.useDefaultResponseMessages(false)
				.select().apis(RequestHandlerSelectors.basePackage(PACKAGE_CONTROLLER))
				.paths(PathSelectors.regex("/.*"))//
				.build();
	}

	/**
	 * Configures resource handlers for Swagger UI and static content.
	 * This method sets up the paths for accessing Swagger documentation and static resources.
	 * 
	 * @param registry The resource handler registry
	 */
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/static/**").addResourceLocations("/static/");
	}

	/**
	 * Configures CORS (Cross-Origin Resource Sharing) support at the MVC level.
	 * This provides a fallback CORS configuration, but Spring Security CORS takes precedence.
	 * 
	 * @param registry The CORS registry
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOrigins("http://localhost:4200")
				.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
				.allowedHeaders("*")
				.allowCredentials(true)
				.maxAge(3600);
	}

	/**
	 * Creates and configures the Jackson ObjectMapper bean for JSON serialization.
	 * This bean handles the conversion of Java objects to JSON and vice versa.
	 * 
	 * @return Configured ObjectMapper bean
	 */
	@Bean
	public ObjectMapper objectMapper() {
        	JavaTimeModule javaTimeModule = new JavaTimeModule();
        	javaTimeModule.addSerializer(ZonedDateTime.class,
                	new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")));
        	return Jackson2ObjectMapperBuilder.json()
					.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS) // ISODate
                	.modules(javaTimeModule)
					.build();
	}

	/**
	 * Creates and configures the HandlerMappingIntrospector bean.
	 * This bean is required for Spring MVC request mapping introspection.
	 * 
	 * @param context The application context
	 * @return Configured HandlerMappingIntrospector bean
	 */
	@Bean
	public HandlerMappingIntrospector mvcHandlerMappingIntrospector(ApplicationContext context) {
		return new HandlerMappingIntrospector(context);
	}



	//TODO: to move this to spring config file
	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setMaxUploadSize(10_000_000); // 10MB max file size
		resolver.setMaxInMemorySize(1_000_000); // 1MB in memory buffer
		return resolver;
	}
}
