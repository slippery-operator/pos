package com.increff.pos.setup;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import com.increff.pos.spring.SpringConfig;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Test configuration class for POS application.
 * 
 * This configuration:
 * - Scans all POS components for dependency injection
 * - Excludes the main SpringConfig to avoid conflicts
 * - Loads test-specific properties
 * - Provides isolated test environment
 * - Configures necessary beans that were excluded with SpringConfig
 */
@Configuration
@ComponentScan(
    basePackages = { "com.increff.pos" },
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, value = { SpringConfig.class })
)
@PropertySources({
    @PropertySource(value = "classpath:./com/increff/pos/test.properties", ignoreResourceNotFound = true)
})
public class QaConfig {

    /**
     * Creates and configures the validation bean for test environment.
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
     * Creates and configures the ModelMapper bean for test environment.
     * This bean provides object-to-object mapping capabilities.
     * 
     * @return Configured ModelMapper bean
     */
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return mapper;
    }
} 