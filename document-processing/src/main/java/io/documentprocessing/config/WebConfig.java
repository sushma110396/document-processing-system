package io.documentprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                .allowedOrigins(
                                "http://localhost:3000",
                                "http://localhost:5173",
                                "http://document-processing-system.s3-website-us-west-1.amazonaws.com",
                                "https://document-processing.onrender.com"
                        )
                		.allowedHeaders("Content-Type", "X-Requested-With", "Authorization")
                		.allowCredentials(true);  // Keep this if you use cookies or auth headers
            }
        };
    }
}
