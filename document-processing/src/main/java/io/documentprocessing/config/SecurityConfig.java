package io.documentprocessing.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity  
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        System.out.println("SecurityConfig is ACTIVE"); // Add for verification in Render logs

        httpSecurity
            .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                @Override
                public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowCredentials(true);
                    corsConfiguration.setAllowedOrigins(Arrays.asList(
                        "http://localhost:5173",
                        "http://document-processing-system.s3-website-us-west-1.amazonaws.com",
                        "https://document-processing.onrender.com"
                    ));
                    corsConfiguration.setAllowedMethods(Arrays.asList(
                        "GET", "POST", "PUT", "DELETE", "OPTIONS"
                    ));
                    corsConfiguration.setAllowedHeaders(Arrays.asList(
                        "Content-Type",
                        "Authorization",
                        "X-Requested-With",
                        "Accept",
                        "Origin"
                    ));
                    corsConfiguration.setMaxAge(Duration.ofMinutes(5));
                    return corsConfiguration;
                }
            }))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().permitAll()
            );

        return httpSecurity.build();
    }
}
