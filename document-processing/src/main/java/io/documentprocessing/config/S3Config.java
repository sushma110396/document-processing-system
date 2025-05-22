package io.documentprocessing.config;

import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
    	return S3Client.builder()
                .region(Region.of(System.getenv("AWS_REGION")))  
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build(); // Uses credentials configured in environment variables
    }
}
