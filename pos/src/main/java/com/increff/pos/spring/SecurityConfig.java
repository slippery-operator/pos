package com.increff.pos.spring;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static Logger logger = Logger.getLogger(SecurityConfig.class);

	//TODO: to check authentication
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
			// Match only these URLs
//				.requestMatchers()//
//				.antMatchers("/api/**")//
//				.antMatchers("/session/**")
//				.antMatchers("/ui/**")//
//				.and()
//				.authorizeRequests()
//				.antMatchers("/api/**").permitAll()
//				.antMatchers("/api/admin/**").hasAuthority("admin") //
//				.antMatchers("/api/**").hasAnyAuthority("admin", "standard")//
//				.antMatchers("/ui/admin/**").hasAuthority("admin")//
//				.antMatchers("/ui/**").hasAnyAuthority("admin", "standard")//
//				.antMatchers("/session/login").permitAll() // Allow login endpoint
//				.antMatchers("/session/logout").permitAll() // Allow logout endpoint
				// Ignore CSRF and enable CORS
				.cors()
				.and()
				.csrf().disable()
				.authorizeRequests()
				.anyRequest().permitAll();
		logger.info("API Configuration complete");
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
				"/swagger-ui.html", "/webjars/**");
	}

}
