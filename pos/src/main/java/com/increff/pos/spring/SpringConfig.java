package com.increff.pos.spring;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Main Spring configuration class that sets up component scanning and general beans.
 * This class consolidates general configuration beans that are not specific to web MVC.
 */
@Configuration
@ComponentScan("com.increff.pos")
@PropertySources({ //
		@PropertySource(value = "file:./employee.properties", ignoreResourceNotFound = true) //
})
public class SpringConfig {

	/**
	 * Creates and configures the validation bean.
	 * This bean provides JSR-303 validation support throughout the application.
	 * 
	 * @return Configured Validator bean
	 */
	@Bean
	public Validator validator() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		return factory.getValidator();
	}

	/**
	 * Creates and configures the multipart resolver bean for file uploads.
	 * This bean handles multipart file uploads with size limits.
	 * 
	 * @return Configured CommonsMultipartResolver bean
	 */
	@Bean
	public CommonsMultipartResolver multipartResolver() {
		CommonsMultipartResolver resolver = new CommonsMultipartResolver();
		resolver.setMaxUploadSize(10_000_000); // 10MB max file size
		resolver.setMaxInMemorySize(1_000_000); // 1MB in memory buffer
		return resolver;
	}

	/**
	 * Creates and configures the ModelMapper bean for object mapping.
	 * This bean provides automatic mapping between different object types.
	 * 
	 * @return Configured ModelMapper bean
	 */
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT)
				.setFieldMatchingEnabled(true)
				.setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
		
		// Add custom converter for Instant to ZonedDateTime
		mapper.addConverter(context -> {
			if (context.getSource() instanceof Instant) {
				Instant instant = (Instant) context.getSource();
				return instant != null ? instant.atZone(ZoneOffset.UTC) : null;
			}
			return null;
		});
		
		return mapper;
	}
}
