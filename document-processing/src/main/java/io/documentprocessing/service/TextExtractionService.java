package io.documentprocessing.service;

import java.io.InputStream;
import java.time.LocalDateTime;


import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.documentprocessing.model.Document;
import io.documentprocessing.model.DocumentMetadata;
import io.documentprocessing.repository.DocumentMetadataRepository;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class TextExtractionService {


    private final DocumentMetadataRepository metadataRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public TextExtractionService(DocumentMetadataRepository metadataRepository,  S3Client s3Client) {
        this.metadataRepository = metadataRepository;
        this.s3Client = s3Client;
    }
    
    public String extractText(Document document) {
        try (InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(bucket)
                .key(document.getS3Key())
                .build())) {

            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, metadata, new ParseContext());

            return handler.toString().trim();

        } catch (Exception e) {
            throw new RuntimeException("Text extraction from S3 failed", e);
        }
    }

    
    @Transactional
    public void processDocument(Document document, DocumentMetadata metadata) {
        // Extract text from the document
        String extractedText = extractText(document);

        // Update metadata with extracted text and processing timestamp
        metadata.setExtractedText(extractedText);
        metadata.setProcessedAt(LocalDateTime.now());
        metadata.setStatus("Processed");

        // Save updated metadata to the database
        metadataRepository.save(metadata);
    }
}
