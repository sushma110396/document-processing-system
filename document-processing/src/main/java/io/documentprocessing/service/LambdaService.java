package io.documentprocessing.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.documentprocessing.model.DocumentMetadata;
import io.documentprocessing.repository.DocumentMetadataRepository;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import org.json.JSONObject;

@Service
public class LambdaService {

    private final LambdaClient lambdaClient;
    private final DocumentMetadataRepository documentMetadataRepository;
    
    public LambdaService(DocumentMetadataRepository documentMetadataRepository, LambdaClient lambdaClient) {
    	this.documentMetadataRepository = documentMetadataRepository;
    	this.lambdaClient = lambdaClient;
    }

    @Value("${aws.lambda.functionName}")
    private String functionName;

    public String triggerTextExtraction(String bucket, String key, DocumentMetadata metadata) {
        String payload = String.format("{\"bucket\":\"%s\", \"key\":\"%s\"}", bucket, key);
        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                .build();

        InvokeResponse response = lambdaClient.invoke(request);
        String responseJson = response.payload().asUtf8String();

        String extractedText = new JSONObject(responseJson).getString("extractedText");

        //Update the existing metadata object
        metadata.setExtractedText(extractedText);
        metadata.setProcessedAt(LocalDateTime.now());
        metadata.setStatus("Processed");

        documentMetadataRepository.save(metadata);  

        return extractedText;
    }

}
