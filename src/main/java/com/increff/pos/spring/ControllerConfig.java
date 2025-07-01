package com.increff.pos.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
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




@Configuration
@EnableWebMvc
@EnableSwagger2
public class
ControllerConfig extends WebMvcConfigurerAdapter {

	public static final String PACKAGE_CONTROLLER = "com.increff.pos.controller";


	private ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.useDefaultResponseMessages(false)
				.select().apis(RequestHandlerSelectors.basePackage(PACKAGE_CONTROLLER))
				.paths(PathSelectors.regex("/.*"))//
				.build();
	}

	// Add configuration for Swagger
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/static/**").addResourceLocations("/static/");
	}
/*
*	@Override
*	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
*		configurer.enable();
*	}
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

	@Bean
	public HandlerMappingIntrospector mvcHandlerMappingIntrospector(ApplicationContext context) {
		return new HandlerMappingIntrospector(context);
	}

	//TODO: to move this to spring config file
	@Bean
	public Validator validator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		return factory.getValidator();
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
