package io.documentprocessing.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
            .cors(corsCustomizer -> corsCustomizer.configurationSource(new CorsConfigurationSource() {
                @Override
                public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
                    CorsConfiguration corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowCredentials(true); // Allow cookies/session
                    corsConfiguration.setAllowedOrigins(Arrays.asList(
                        "http://localhost:5173", // Local React frontend
                        "http://document-processing-system.s3-website-us-west-1.amazonaws.com", // S3 static site
                        "https://document-processing.onrender.com" // Render deployment
                    ));
                    corsConfiguration.setAllowedMethods(Arrays.asList(
                    	    "GET",
                    	    "POST",
                    	    "PUT",
                    	    "DELETE",
                    	    "OPTIONS"
                    	));

                    corsConfiguration.setAllowedHeaders(Arrays.asList(
                    	    "Content-Type",
                    	    "Authorization",
                    	    "X-Requested-With",
                    	    "Accept",
                    	    "Origin"
                    	));

                    corsConfiguration.setMaxAge(Duration.ofMinutes(5)); // Cache pre-flight response for 5 min
                    return corsConfiguration;
                }
            }))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll() // Allow public auth routes
                .anyRequest().permitAll()
            );

        return httpSecurity.build();
    }
}
