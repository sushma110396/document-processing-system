package io.documentprocessing.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.US_WEST_1) // Replace with your S3 region
                .credentialsProvider(ProfileCredentialsProvider.create()) // Uses credentials configured in environment variables
                .build();
    }
}
