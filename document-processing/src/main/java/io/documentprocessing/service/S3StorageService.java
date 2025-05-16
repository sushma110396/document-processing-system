package io.documentprocessing.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private final S3Client s3Client;

    // Bucket name from application.properties 
    @Value("${aws.s3.bucket}")
    private String bucket;

    public S3StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

	// Uploads a file to S3 
    public String uploadFile(MultipartFile file) throws Exception {
    	// Generate a unique key
        String key = UUID.randomUUID() + "_" + file.getOriginalFilename();

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        InputStream inputStream = file.getInputStream();

     // Upload the file using S3 client
        s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, file.getSize()));

        return key;
    }
    
    // Downloads a file from S3 using the key 
    public byte[] downloadFile(String s3Key) throws IOException {
        // Build the request to fetch the object
		GetObjectRequest getObjectRequest = GetObjectRequest.builder()
		        .bucket(bucket)
		        .key(s3Key)
		        .build();

		// Download the file from S3 as bytes
		ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);
		return objectBytes.asByteArray();
    }


}
