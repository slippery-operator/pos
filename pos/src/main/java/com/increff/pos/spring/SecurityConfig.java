//package com.increff.pos.spring;
//
//import org.apache.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.builders.WebSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import javax.servlet.http.HttpServletResponse;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//@Configuration
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(securedEnabled = true)
//public class SecurityConfig extends WebSecurityConfigurerAdapter {
//
//    private static Logger logger = Logger.getLogger(SecurityConfig.class);
//
//    @Autowired
//    private CustomAuthenticationFilter customAuthFilter;
//
//    @Autowired
//    private CorsSessionFilter corsSessionFilter;
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                .csrf().disable()
//                .cors().configurationSource(corsConfigurationSource()).and()
//                .sessionManagement()
//                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                .and()
//                .authorizeRequests()
//                .antMatchers("/auth/**", "/public/**").permitAll()
//
//                .antMatchers(HttpMethod.POST, "/clients").hasRole("SUPERVISOR")
//                .antMatchers(HttpMethod.PUT, "/clients/**").hasRole("SUPERVISOR")
//
//                .antMatchers(HttpMethod.POST, "/products", "/products/upload").hasRole("SUPERVISOR")
//                .antMatchers(HttpMethod.PUT, "/products/**").hasRole("SUPERVISOR")
//
//                .antMatchers(HttpMethod.POST, "/inventory/upload").hasRole("SUPERVISOR")
//                .antMatchers(HttpMethod.PUT, "/inventory/**").hasRole("SUPERVISOR")
//
//                .antMatchers(HttpMethod.POST, "/orders").hasRole("SUPERVISOR")
//
//                .antMatchers(HttpMethod.GET, "/clients/**").hasAnyRole("SUPERVISOR", "OPERATOR")
//                .antMatchers(HttpMethod.GET, "/products/**").hasAnyRole("SUPERVISOR", "OPERATOR")
//                .antMatchers(HttpMethod.POST, "/products/search").hasAnyRole("SUPERVISOR", "OPERATOR")
//                .antMatchers(HttpMethod.GET, "/inventory/**").hasAnyRole("SUPERVISOR", "OPERATOR")
//                .antMatchers(HttpMethod.GET, "/orders/**").hasAnyRole("SUPERVISOR", "OPERATOR")
//                .antMatchers(HttpMethod.GET, "/invoice/**").hasAnyRole("SUPERVISOR", "OPERATOR")
//                .antMatchers(HttpMethod.GET, "/reports/**").hasAnyRole("SUPERVISOR", "OPERATOR")
//
//                .anyRequest().authenticated()
//                .and()
//                .exceptionHandling()
//                .authenticationEntryPoint((request, response, authException) -> {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    response.setContentType("application/json");
//                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
//                })
//                .and()
//                .addFilterBefore(corsSessionFilter, BasicAuthenticationFilter.class)
//                .addFilterBefore(customAuthFilter, UsernamePasswordAuthenticationFilter.class);
//        logger.info("Spring Security Configuration complete - CORS enabled for localhost:4200");
//    }
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // Allow specific origin - must match exactly
//        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
//
//        // Allow specific methods including OPTIONS for preflight
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
//
//        // Allow all headers including custom ones
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//
//        // Allow credentials (cookies, authorization headers, sessions)
//        configuration.setAllowCredentials(true);
//
//        // Cache preflight response for 1 hour
//        configuration.setMaxAge(3600L);
//
//        // Enable CORS for all paths
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//
//        logger.info("CORS Configuration: Origin=http://localhost:4200, Methods=GET,POST,PUT,DELETE,OPTIONS,HEAD, Credentials=true");
//        return source;
//    }
//
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
//                "/swagger-ui.html", "/webjars/**");
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
package com.increff.pos.spring;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static Logger logger = Logger.getLogger(SecurityConfig.class);

    @Autowired
    private CustomAuthenticationFilter customAuthFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource()).and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow all OPTIONS requests
                .antMatchers("/auth/**", "/public/**").permitAll()

                .antMatchers(HttpMethod.POST, "/clients").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.PUT, "/clients/**").hasRole("SUPERVISOR")

                .antMatchers(HttpMethod.POST, "/products", "/products/upload").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.PUT, "/products/**").hasRole("SUPERVISOR")

                .antMatchers(HttpMethod.POST, "/inventory/upload").hasRole("SUPERVISOR")
                .antMatchers(HttpMethod.PUT, "/inventory/**").hasRole("SUPERVISOR")

                .antMatchers(HttpMethod.POST, "/orders").hasAnyRole("SUPERVISOR", "OPERATOR")

                .antMatchers(HttpMethod.GET, "/clients/**").hasAnyRole("SUPERVISOR", "OPERATOR")
                .antMatchers(HttpMethod.GET, "/products/**").hasAnyRole("SUPERVISOR", "OPERATOR")
                .antMatchers(HttpMethod.POST, "/products/search").hasAnyRole("SUPERVISOR", "OPERATOR")
                .antMatchers(HttpMethod.GET, "/inventory/**").hasAnyRole("SUPERVISOR", "OPERATOR")
                .antMatchers(HttpMethod.GET, "/orders/**").hasAnyRole("SUPERVISOR", "OPERATOR")
                .antMatchers(HttpMethod.GET, "/invoice/**").hasAnyRole("SUPERVISOR", "OPERATOR")
                .antMatchers(HttpMethod.GET, "/reports/**").hasAnyRole("SUPERVISOR", "OPERATOR")

                .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
                    response.setHeader("Access-Control-Allow-Credentials", "true");
                    response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,HEAD");
                    response.setHeader("Access-Control-Allow-Headers", "*");

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
                .and()
                .addFilterBefore(customAuthFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("Spring Security Configuration complete - CORS enabled for localhost:4200");
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origin - must match exactly
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));

        // Allow specific methods including OPTIONS for preflight
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        // Allow all headers including custom ones
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (cookies, authorization headers, sessions)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Enable CORS for all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        logger.info("CORS Configuration: Origin=http://localhost:4200, Methods=GET,POST,PUT,DELETE,OPTIONS,HEAD, Credentials=true");
        return source;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
                "/swagger-ui.html", "/webjars/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
