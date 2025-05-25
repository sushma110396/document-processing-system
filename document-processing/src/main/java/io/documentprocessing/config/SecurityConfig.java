import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors() // Enable CORS
            .and()
            .csrf().disable() // Disable CSRF for APIs; optional
            .authorizeHttpRequests()
                .requestMatchers("/auth/**").permitAll() // Allow public endpoints (e.g., register, login)
                .anyRequest().authenticated(); // Secure other endpoints
        return http.build();
    }
}
