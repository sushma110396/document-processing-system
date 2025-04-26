package io.documentprocessing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AWSConfig {

    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.create();  //required for backend controlled invocation
    }
}
