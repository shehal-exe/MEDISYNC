package com.medisync.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${medisync.remember-me.key:medisync-secret-key}")
    private String rememberMeKey;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public RememberMeServices rememberMeServices(UserDetailsService userDetailsService) {
        JsonRememberMeServices rememberMeServices =
                new JsonRememberMeServices(rememberMeKey, userDetailsService);

        rememberMeServices.setAlwaysRemember(false);

        return rememberMeServices;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            RememberMeServices rememberMeServices) throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disabled CSRF for academic local development simplicity.
            // In a real-world browser-based session application,
            // CSRF protection should be enabled.
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(authz -> authz

                // Allow CORS preflight requests
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Allow Spring error responses to be returned normally
                .requestMatchers("/error").permitAll()

                // Public health endpoint
                .requestMatchers(
                        "/api/v1/health",
                        "/api/v1/health/"
                ).permitAll()

                // Public authentication endpoints
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/auth/register",
                        "/api/v1/auth/register/"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/auth/login",
                        "/api/v1/auth/login/"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/forgot-password/"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.POST,
                        "/api/v1/auth/reset-password",
                        "/api/v1/auth/reset-password/"
                ).permitAll()

                // Patient-only endpoints
                .requestMatchers("/api/v1/patients/**").hasRole("PATIENT")
                .requestMatchers("/api/v1/patient/**").hasRole("PATIENT")

                // Pharmacist-only endpoints
                .requestMatchers("/api/v1/pharmacist/**").hasRole("PHARMACIST")

                // Authenticated endpoints
                .requestMatchers("/api/v1/auth/me").authenticated()
                .requestMatchers("/api/v1/auth/logout").authenticated()
                .requestMatchers("/api/v1/auth/change-password").authenticated()

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            )

            .rememberMe(remember -> remember
                .rememberMeServices(rememberMeServices)
                .key(rememberMeKey)
            )

            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json");
                    response.setStatus(
                            jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED
                    );

                    response.getWriter().write(
                            "{\"success\":false,\"message\":\"Unauthenticated\",\"errorCode\":\"UNAUTHORIZED\"}"
                    );
                })
            )

            // Custom logout is handled by AuthController
            .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow the frontend local server and file protocol
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000", "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}