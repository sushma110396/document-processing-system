package io.documentprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig configures HTTP security settings for the application.
 * 
 * This class enables Cross-Origin Resource Sharing (CORS) to allow frontend 
 * clients (e.g., React running on http://localhost:3000) to communicate with 
 * the backend APIs during development.
 * 
 *
 */

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())  // correctly enables CORS
            .csrf(csrf -> csrf.disable())     // disable CSRF for local testing 
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll());   // allow all requests without auth

        return http.build();
    }
}
