package io.documentprocessing.service;

import java.io.IOException;
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

import org.json.JSONException;
import org.json.JSONObject;

@Service
public class LambdaService {

    private final LambdaClient lambdaClient;
    private final DocumentMetadataRepository documentMetadataRepository;
    private final LuceneService luceneService;
    
    public LambdaService(DocumentMetadataRepository documentMetadataRepository, LambdaClient lambdaClient, LuceneService luceneService) {
    	this.documentMetadataRepository = documentMetadataRepository;
    	this.lambdaClient = lambdaClient;
    	this.luceneService = luceneService;
    }

    @Value("${aws.lambda.functionName}")
    private String functionName;

    public String triggerTextExtraction(String bucket, String key, DocumentMetadata metadata) {
        // Prepare payload
        String payload = String.format("{\"bucket\":\"%s\", \"key\":\"%s\"}", bucket, key);
        InvokeRequest request = InvokeRequest.builder()
                .functionName(functionName)
                .payload(SdkBytes.fromString(payload, StandardCharsets.UTF_8))
                .build();

        // Invoke the Lambda function
        InvokeResponse response = lambdaClient.invoke(request);
        String responseJson = response.payload().asUtf8String();

        System.out.println("Lambda raw response: " + responseJson);

        try {
            // Step 1: Parse outer JSON
            JSONObject root = new JSONObject(responseJson);

            // Step 2: Get and parse the nested "body" string as JSON
            String bodyString = root.getString("body");
            JSONObject body = new JSONObject(bodyString);

            // Step 3: Get the actual extractedText
            if (!body.has("extractedText")) {
                throw new RuntimeException("Lambda did not return extractedText: " + responseJson);
            }

            String extractedText = body.getString("extractedText");

            // Step 4: Save to metadata
            metadata.setExtractedText(extractedText);
            metadata.setProcessedAt(LocalDateTime.now());
            metadata.setStatus("Processed");

            documentMetadataRepository.save(metadata);
            
         // Index in Lucene now that extractedText is available
            try {
                luceneService.indexDocument(
                    metadata.getDocument().getId().toString(),
                    metadata.getDocument().getName(),
                    metadata.getDocument().getType(),
                    extractedText,
                    metadata.getDocument().getOwner().getId().toString()
                );
                System.out.println("Indexed into Lucene: " + extractedText);
            } catch (IOException e) {
                System.err.println("Failed to index in Lucene: " + e.getMessage());
            }

            return extractedText;

        } catch (JSONException e) {
            throw new RuntimeException("Failed to parse Lambda response: " + responseJson, e);
        }
    }


}
